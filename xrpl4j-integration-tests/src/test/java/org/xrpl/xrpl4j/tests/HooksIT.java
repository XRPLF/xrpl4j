package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;
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

public class HooksIT extends AbstractHookIT {

  // account that hook is configured to send an offset payment to
  private static final Address CARBON_ACCOUNT = Address.of("rfCarbonVNTuXckX6x2qTMFmFSnm6dEWGX");

  @Test
  public void carbonHook() throws JsonRpcClientErrorException, IOException {
    Wallet hookedWallet = this.createRandomAccount();
    Wallet randomWallet = this.createRandomAccount();
    fundAddress(CARBON_ACCOUNT);
    XrpCurrencyAmount initialCarbonBalance = getAccountBalance(CARBON_ACCOUNT);
    assertThat(initialCarbonBalance).isEqualTo(XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(1000)));

    String wasmHex = readHookToHex("hooks/carbon/carbon.wasm");
    createHook(hookedWallet, wasmHex);

    sendPayment(hookedWallet, randomWallet.classicAddress(), XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(100)));

    // Carbon Hook sends 1% of payment to Carbon account. In this case 1 XRP.
    XrpCurrencyAmount expectedOffsetAmount = XrpCurrencyAmount.ofXrp(BigDecimal.ONE);
    XrpCurrencyAmount expectedBalance = initialCarbonBalance.plus(expectedOffsetAmount).minus(
      XrpCurrencyAmount.ofDrops(1)
    );

    scanForResult(() -> getAccountBalance(CARBON_ACCOUNT), (amount) -> amount.equals(expectedBalance));
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
    logger.info("SetHook successful: " + result.transactionResult().hash());

    scanForHook(hookedWallet);
  }

  private void sendPayment(Wallet sourceWallet, Address destination, XrpCurrencyAmount paymentAmount)
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
    assertThat(result.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");

    this.scanForResult(
      () -> this.getValidatedTransaction(result.transactionResult().hash(), Payment.class)
    );
  }

  private static String readHookToHex(String resourceName) throws IOException {
    byte [] hook = ByteStreams.toByteArray(Resources.getResource(resourceName).openStream());
    return BaseEncoding.base16().encode(hook);
  }

  public HookObject scanForHook(Wallet wallet) {
    return this.scanForLedgerObject(
      () -> this.getValidatedAccountObjects(wallet.classicAddress(), HookObject.class)
        .stream()
        .findFirst()
        .orElse(null));
  }

  private XrpCurrencyAmount getAccountBalance(Address address) {
    return this.scanForResult(() -> this.getValidatedAccountInfo(address)).accountData().balance();
  }

}
