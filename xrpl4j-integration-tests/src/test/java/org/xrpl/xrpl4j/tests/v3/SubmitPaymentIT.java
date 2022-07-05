package org.xrpl.xrpl4j.tests.v3;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.core.keys.Base58EncodedSecret;
import org.xrpl.xrpl4j.crypto.core.keys.Seed;
import org.xrpl.xrpl4j.crypto.core.signing.SingleSingedTransaction;
import org.xrpl.xrpl4j.crypto.core.wallet.Wallet;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.TransactionMetadata;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * Integration test to validate submission of Payment transactions.
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
public class SubmitPaymentIT extends AbstractIT {

  public static final String SUCCESS_STATUS = "tesSUCCESS";

  @Test
  public void sendPayment() throws JsonRpcClientErrorException, JsonProcessingException {
    Wallet sourceWallet = createRandomAccountEd25519();
    Wallet destinationWallet = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(sourceWallet.address())
    );
    XrpCurrencyAmount amount = XrpCurrencyAmount.ofDrops(12345);
    Payment payment = Payment.builder()
      .account(sourceWallet.address())
      .fee(getComputedNetworkFee(feeResult))
      .sequence(accountInfo.accountData().sequence())
      .destination(destinationWallet.address())
      .amount(amount)
      .signingPublicKey(sourceWallet.publicKey().base16Value())
      .build();

    SingleSingedTransaction<Payment> signedPayment = signatureService.sign(sourceWallet.privateKey(), payment);
    SubmitResult<Payment> result = xrplClient.submit(signedPayment);
    assertThat(result.result()).isEqualTo(SUCCESS_STATUS);
    logger.info("Payment successful: https://testnet.xrpl.org/transactions/{}", result.transactionResult().hash());

    TransactionResult<Payment> validatedPayment = this.scanForResult(
      () -> this.getValidatedTransaction(result.transactionResult().hash(), Payment.class)
    );

    assertThat(validatedPayment.metadata().flatMap(TransactionMetadata::deliveredAmount)).hasValue(amount);
    assertThat(validatedPayment.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);

    assertPaymentCloseTimeMatchesLedgerCloseTime(validatedPayment);
  }

  @Test
  public void sendPaymentFromSecp256k1Wallet() throws JsonRpcClientErrorException, JsonProcessingException {
    Wallet senderWallet = walletFactory.fromSeed(Seed.fromBase58EncodedSecret(
      Base58EncodedSecret.of("sp5fghtJtpUorTwvof1NpDXAzNwf5"))
    );
    logger.info("Generated source testnet wallet with address " + senderWallet.address());

    fundAccount(senderWallet);

    Wallet destinationWallet = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(senderWallet.address())
    );

    Payment payment = Payment.builder()
      .account(senderWallet.address())
      .fee(getComputedNetworkFee(feeResult))
      .sequence(accountInfo.accountData().sequence())
      .destination(destinationWallet.address())
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .signingPublicKey(senderWallet.publicKey().base16Value())
      .build();

    SingleSingedTransaction<Payment> signedPayment = signatureService.sign(senderWallet.privateKey(), payment);
    SubmitResult<Payment> result = xrplClient.submit(signedPayment);
    assertThat(result.result()).isEqualTo("tesSUCCESS");
    logger.info("Payment successful: https://testnet.xrpl.org/transactions/{}", result.transactionResult().hash());

    this.scanForResult(() -> this.getValidatedTransaction(result.transactionResult().hash(), Payment.class));
  }

  private void assertPaymentCloseTimeMatchesLedgerCloseTime(TransactionResult<Payment> validatedPayment)
    throws JsonRpcClientErrorException {
    LedgerResult ledger = xrplClient.ledger(
      LedgerRequestParams.builder()
        .ledgerSpecifier(LedgerSpecifier.of(validatedPayment.ledgerIndex().get()))
        .build()
    );

    assertThat(validatedPayment.closeDateHuman()).isNotEmpty();
    assertThat(ledger.ledger().closeTimeHuman()).isNotEmpty();
    assertThat(validatedPayment.closeDateHuman()).isEqualTo(ledger.ledger().closeTimeHuman());
  }

}
