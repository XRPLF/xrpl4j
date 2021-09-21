package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;
import org.awaitility.Duration;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.ledger.HookObject;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.SetHook;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.xrpl.xrpl4j.wallet.Wallet;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

public class HooksIT extends AbstractHookIT {

  // account that hook is configured to send an offset payment to
  private static final Address CARBON_ACCOUNT = Address.of("rfCarbonVNTuXckX6x2qTMFmFSnm6dEWGX");

  private static String readHookToHex(String resourceName) throws IOException {
    byte[] hook = ByteStreams.toByteArray(Resources.getResource(resourceName).openStream());
    return BaseEncoding.base16().encode(hook);
  }

  @Test
  public void carbonHook() throws JsonRpcClientErrorException, IOException {
    Wallet hookedWallet = this.createRandomAccount();
    fundAddress(CARBON_ACCOUNT);
    XrpCurrencyAmount initialCarbonBalance = getAccountBalance(CARBON_ACCOUNT);
    assertThat(initialCarbonBalance).isEqualTo(XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(1000)));

    String wasmHex = readHookToHex("hooks/carbon/carbon.wasm");
    createHook(hookedWallet, wasmHex);

    Wallet randomWallet = this.createRandomAccount();
    sendGoodPayment(hookedWallet, randomWallet.classicAddress(), XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(100)));

    // Carbon Hook sends 1% of payment to Carbon account. In this case 1 XRP.
    XrpCurrencyAmount expectedOffsetAmount = XrpCurrencyAmount.ofXrp(BigDecimal.ONE);
    XrpCurrencyAmount expectedBalance = initialCarbonBalance.plus(expectedOffsetAmount).minus(
      XrpCurrencyAmount.ofDrops(1)
    );

    scanForResult(() -> getAccountBalance(CARBON_ACCOUNT), (amount) -> amount.equals(expectedBalance));
  }

  @Test
  public void doublerEventuallyPays() throws JsonRpcClientErrorException, IOException {
    Wallet doublerWallet = this.createRandomAccount();
    fundAddress(doublerWallet.classicAddress());

    Wallet gamblerWallet = this.createRandomAccount();
    fundAddress(doublerWallet.classicAddress());

    XrpCurrencyAmount initialGamblerBalance = getAccountBalance(gamblerWallet.classicAddress());
    assertThat(initialGamblerBalance).isEqualTo(XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(1000)));

    createDoublerHook(doublerWallet);

    for (int i = 0; i < 10; i++) {
      try {
        logger.info("making bet");
        makeGoodBet(doublerWallet, gamblerWallet);
        logger.info("won bet");
        return;
      } catch (Exception e) {
        logger.info("lost bet");
      }
    }
    fail("all bets lost");
  }

  @Test
  public void underMinimumBetRejected() throws JsonRpcClientErrorException, IOException {
    Wallet doublerWallet = this.createRandomAccount();
    fundAddress(doublerWallet.classicAddress());

    Wallet gamblerWallet = this.createRandomAccount();
    fundAddress(doublerWallet.classicAddress());

    createDoublerHook(doublerWallet);
    logger.info("making bet");
    makeBadBet(doublerWallet, gamblerWallet, XrpCurrencyAmount.ofDrops(10));
  }

  @Test
  public void overMaximumBetRejected() throws JsonRpcClientErrorException, IOException {
    Wallet doublerWallet = this.createRandomAccount();
    fundAddress(doublerWallet.classicAddress());

    Wallet gamblerWallet = this.createRandomAccount();
    fundAddress(doublerWallet.classicAddress());

    createDoublerHook(doublerWallet);
    logger.info("making bet");
    makeBadBet(doublerWallet, gamblerWallet, XrpCurrencyAmount.ofXrp(new BigDecimal("100.1")));
  }

  private void createDoublerHook(Wallet doublerWallet) throws IOException, JsonRpcClientErrorException {
    String wasmHex = readHookToHex("hooks/vegas/vegas.wasm");
    createHook(doublerWallet, wasmHex);
  }

  private void makeBadBet(Wallet doublerWallet, Wallet gamblerWallet, XrpCurrencyAmount bet)
    throws JsonRpcClientErrorException {
    sendPayment(gamblerWallet, doublerWallet.classicAddress(), bet, "tecHOOK_REJECTED");
  }

  private void makeGoodBet(Wallet doublerWallet, Wallet gamblerWallet) throws JsonRpcClientErrorException {
    XrpCurrencyAmount startingBalance = getAccountBalance(gamblerWallet.classicAddress());
    logger.info("Starting balance {}", startingBalance.toXrp());
    XrpCurrencyAmount betAmount = XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(110));
    sendGoodPayment(gamblerWallet, doublerWallet.classicAddress(), betAmount);

    scanForResult(() -> getAccountBalance(gamblerWallet.classicAddress()),
      (amount) -> amount.compareTo(startingBalance) > 0,
      new Duration(2, TimeUnit.SECONDS));
  }

  /**
   * Scans for HookObject on wallet.
   *
   * @param wallet to scan.
   * @return found object or null.
   */
  public HookObject scanForHook(Wallet wallet) {
    return this.scanForLedgerObject(
      () -> this.getValidatedAccountObjects(wallet.classicAddress(), HookObject.class)
        .stream()
        .findFirst()
        .orElse(null));
  }

  private void createHook(Wallet hookedWallet, String wasmHex) throws JsonRpcClientErrorException {
    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfo =
      this.scanForResult(() -> this.getValidatedAccountInfo(hookedWallet.classicAddress()));
    SetHook hook = SetHook.builder()
      .account(hookedWallet.classicAddress())
      .sequence(accountInfo.accountData().sequence())
      .fee(feeResult.drops().openLedgerFee())
      .createCode(wasmHex)
      .signingPublicKey(hookedWallet.publicKey())
      .build();

    SubmitResult<Transaction> result = xrplClient.submit(hookedWallet, hook);
    assertThat(result.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info("SetHook successful: " + result.transactionResult().transaction());

    scanForHook(hookedWallet);
  }

  private void sendGoodPayment(Wallet sourceWallet, Address destination, XrpCurrencyAmount paymentAmount)
    throws JsonRpcClientErrorException {
    sendPayment(sourceWallet, destination, paymentAmount, "tesSUCCESS");
  }

  private void sendPayment(Wallet sourceWallet, Address destination, XrpCurrencyAmount paymentAmount, String expected)
    throws JsonRpcClientErrorException {
    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfo =
      this.scanForResult(() -> this.getValidatedAccountInfo(sourceWallet.classicAddress()));
    Payment payment = Payment.builder()
      .account(sourceWallet.classicAddress())
      .fee(feeResult.drops().minimumFee())
      .sequence(accountInfo.accountData().sequence())
      .destination(destination)
      .amount(paymentAmount)
      .signingPublicKey(sourceWallet.publicKey())
      .build();

    SubmitResult<Payment> result = xrplClient.submit(sourceWallet, payment);
    assertThat(result.engineResult()).isNotEmpty().get().isEqualTo(expected);
    if (!expected.equals("tesSUCCESS")) {
      return;
    }

    this.scanForResult(
      () -> this.getValidatedTransaction(
        result.transactionResult().transaction().hash()
          .orElseThrow(() -> new RuntimeException("Result didn't have hash.")),
        Payment.class)
    );
  }

  private XrpCurrencyAmount getAccountBalance(Address address) {
    return this.scanForResult(() -> this.getValidatedAccountInfo(address)).accountData().balance();
  }

}
