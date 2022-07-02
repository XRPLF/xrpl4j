package org.xrpl.xrpl4j.tests.v3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.xrpl.xrpl4j.model.client.fees.FeeUtils.computeFees;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.hash.Hashing;
import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.core.signing.SingleSingedTransaction;
import org.xrpl.xrpl4j.crypto.core.wallet.Wallet;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.ledger.CheckObject;
import org.xrpl.xrpl4j.model.ledger.LedgerObject;
import org.xrpl.xrpl4j.model.transactions.CheckCancel;
import org.xrpl.xrpl4j.model.transactions.CheckCash;
import org.xrpl.xrpl4j.model.transactions.CheckCreate;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.function.Predicate;

/**
 * Integration tests to validate submission of Check transactions.
 */
@SuppressWarnings( {"UnstableApiUsage", "OptionalGetWithoutIsPresent"})
public class CheckIT extends AbstractIT {

  @Test
  public void createXrpCheckAndCash() throws JsonRpcClientErrorException, JsonProcessingException {

    //////////////////////
    // Generate and fund source and destination accounts
    Wallet sourceWallet = createRandomAccountEd25519();
    Wallet destinationWallet = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfoResult = this.scanForResult(
      () -> this.getValidatedAccountInfo(sourceWallet.address())
    );

    //////////////////////
    // Create a Check with an InvoiceID for easy identification
    Hash256 invoiceId = Hash256.of(Hashing.sha256().hashBytes("Check this out.".getBytes()).toString());
    CheckCreate checkCreate = CheckCreate.builder()
      .account(sourceWallet.address())
      .fee(computeFees(feeResult).recommendedFee())
      .sequence(accountInfoResult.accountData().sequence())
      .destination(destinationWallet.address())
      .sendMax(XrpCurrencyAmount.ofDrops(12345))
      .invoiceId(invoiceId)
      .signingPublicKey(sourceWallet.publicKey().base16Value())
      .build();

    SingleSingedTransaction<CheckCreate> signedCheckCreate = signatureService.sign(
      sourceWallet.privateKey(), checkCreate
    );
    SubmitResult<CheckCreate> response = xrplClient.submit(signedCheckCreate);
    assertThat(response.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "CheckCreate transaction successful: https://testnet.xrpl.org/transactions/{}",
      response.transactionResult().hash()
    );

    //////////////////////
    // Poll the ledger for the source wallet's account objects, and validate that the created Check makes
    // it into the ledger
    CheckObject checkObject = (CheckObject) this
      .scanForResult(
        () -> this.getValidatedAccountObjects(sourceWallet.address()),
        result -> result.accountObjects().stream().anyMatch(findCheck(sourceWallet, destinationWallet, invoiceId))
      )
      .accountObjects().stream()
      .filter(findCheck(sourceWallet, destinationWallet, invoiceId))
      .findFirst().get();

    //////////////////////
    // Destination wallet cashes the Check
    feeResult = xrplClient.fee();
    AccountInfoResult destinationAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(destinationWallet.address())
    );
    CheckCash checkCash = CheckCash.builder()
      .account(destinationWallet.address())
      .amount(checkObject.sendMax())
      .sequence(destinationAccountInfo.accountData().sequence())
      .fee(computeFees(feeResult).recommendedFee())
      .checkId(checkObject.index())
      .signingPublicKey(destinationWallet.publicKey().base16Value())
      .build();
    SingleSingedTransaction<CheckCash> signedCheckCash = signatureService.sign(
      destinationWallet.privateKey(), checkCash
    );
    SubmitResult<CheckCash> cashResponse = xrplClient.submit(signedCheckCash);
    assertThat(cashResponse.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "CheckCash transaction successful: https://testnet.xrpl.org/transactions/{}",
      cashResponse.transactionResult().hash()
    );

    //////////////////////
    // Validate that the destination account balance increases by the check amount minus fees
    this.scanForResult(
      () -> this.getValidatedAccountInfo(destinationWallet.address()),
      result -> {
        logger.info("AccountInfoResult after CheckCash balance: {}", result.accountData().balance().value());
        return result.accountData().balance().equals(
          destinationAccountInfo.accountData().balance()
            .plus((XrpCurrencyAmount) checkObject.sendMax())
            .minus(checkCash.fee()));
      });

    //////////////////////
    // Validate that the Check object was deleted
    this.scanForResult(
      () -> this.getValidatedAccountObjects(sourceWallet.address()),
      result -> result.accountObjects().stream().noneMatch(findCheck(sourceWallet, destinationWallet, invoiceId))
    );
  }

  @Test
  public void createCheckAndSourceCancels() throws JsonRpcClientErrorException, JsonProcessingException {

    //////////////////////
    // Generate and fund source and destination accounts
    Wallet sourceWallet = createRandomAccountEd25519();
    Wallet destinationWallet = createRandomAccountEd25519();

    //////////////////////
    // Create a Check with an InvoiceID for easy identification
    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfoResult = this.scanForResult(
      () -> this.getValidatedAccountInfo(sourceWallet.address())
    );

    Hash256 invoiceId = Hash256.of(Hashing.sha256().hashBytes("Check this out.".getBytes()).toString());
    CheckCreate checkCreate = CheckCreate.builder()
      .account(sourceWallet.address())
      .fee(computeFees(feeResult).recommendedFee())
      .sequence(accountInfoResult.accountData().sequence())
      .destination(destinationWallet.address())
      .sendMax(XrpCurrencyAmount.ofDrops(12345))
      .invoiceId(invoiceId)
      .signingPublicKey(sourceWallet.publicKey().base16Value())
      .build();

    SingleSingedTransaction<CheckCreate> signedCheckCreate = signatureService.sign(
      sourceWallet.privateKey(), checkCreate
    );
    SubmitResult<CheckCreate> response = xrplClient.submit(signedCheckCreate);
    assertThat(response.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "CheckCreate transaction successful: https://testnet.xrpl.org/transactions/{}",
      response.transactionResult().hash()
    );

    //////////////////////
    // Poll the ledger for the source wallet's account objects, and validate that the created Check makes
    // it into the ledger
    CheckObject checkObject = (CheckObject) this
      .scanForResult(() -> this.getValidatedAccountObjects(sourceWallet.address()),
        result -> result.accountObjects().stream().anyMatch(findCheck(sourceWallet, destinationWallet, invoiceId))
      )
      .accountObjects().stream()
      .filter(findCheck(sourceWallet, destinationWallet, invoiceId))
      .findFirst().get();

    //////////////////////
    // Source account cancels the Check
    feeResult = xrplClient.fee();
    CheckCancel checkCancel = CheckCancel.builder()
      .account(sourceWallet.address())
      .sequence(accountInfoResult.accountData().sequence().plus(UnsignedInteger.ONE))
      .fee(computeFees(feeResult).recommendedFee())
      .checkId(checkObject.index())
      .signingPublicKey(sourceWallet.publicKey().base16Value())
      .build();

    SingleSingedTransaction<CheckCancel> signedCheckCancel = signatureService.sign(
      sourceWallet.privateKey(), checkCancel
    );
    SubmitResult<CheckCancel> cancelResult = xrplClient.submit(signedCheckCancel);
    assertThat(cancelResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "CheckCancel transaction successful: https://testnet.xrpl.org/transactions/{}",
      cancelResult.transactionResult().hash()
    );

    //////////////////////
    // Validate that the Check does not exist after cancelling
    this.scanForResult(
      () -> this.getValidatedAccountObjects(sourceWallet.address()),
      result -> result.accountObjects().stream().noneMatch(findCheck(sourceWallet, destinationWallet, invoiceId))
    );
  }

  @Test
  public void createCheckAndDestinationCancels() throws JsonRpcClientErrorException, JsonProcessingException {

    //////////////////////
    // Generate and fund source and destination accounts
    Wallet sourceWallet = createRandomAccountEd25519();
    Wallet destinationWallet = createRandomAccountEd25519();

    //////////////////////
    // Create a Check with an InvoiceID for easy identification
    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfoResult = this.scanForResult(
      () -> this.getValidatedAccountInfo(sourceWallet.address())
    );

    Hash256 invoiceId = Hash256.of(Hashing.sha256().hashBytes("Check this out.".getBytes()).toString());
    CheckCreate checkCreate = CheckCreate.builder()
      .account(sourceWallet.address())
      .fee(computeFees(feeResult).recommendedFee())
      .sequence(accountInfoResult.accountData().sequence())
      .destination(destinationWallet.address())
      .sendMax(XrpCurrencyAmount.ofDrops(12345))
      .invoiceId(invoiceId)
      .signingPublicKey(sourceWallet.publicKey().base16Value())
      .build();

    //////////////////////
    // Poll the ledger for the source wallet's account objects, and validate that the created Check makes
    // it into the ledger
    SingleSingedTransaction<CheckCreate> signedCheckCreate = signatureService.sign(
      sourceWallet.privateKey(), checkCreate
    );
    SubmitResult<CheckCreate> response = xrplClient.submit(signedCheckCreate);
    assertThat(response.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "CheckCreate transaction successful: https://testnet.xrpl.org/transactions/{}",
      response.transactionResult().hash()
    );

    CheckObject checkObject = (CheckObject) this
      .scanForResult(
        () -> this.getValidatedAccountObjects(sourceWallet.address()),
        result -> result.accountObjects().stream().anyMatch(findCheck(sourceWallet, destinationWallet, invoiceId))
      )
      .accountObjects().stream()
      .filter(findCheck(sourceWallet, destinationWallet, invoiceId))
      .findFirst().get();

    //////////////////////
    // Destination account cancels the Check
    feeResult = xrplClient.fee();
    AccountInfoResult destinationAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(destinationWallet.address())
    );
    CheckCancel checkCancel = CheckCancel.builder()
      .account(destinationWallet.address())
      .sequence(destinationAccountInfo.accountData().sequence())
      .fee(computeFees(feeResult).recommendedFee())
      .checkId(checkObject.index())
      .signingPublicKey(destinationWallet.publicKey().base16Value())
      .build();

    SingleSingedTransaction<CheckCancel> signedCheckCancel = signatureService.sign(
      destinationWallet.privateKey(), checkCancel
    );
    SubmitResult<CheckCancel> cancelResult = xrplClient.submit(signedCheckCancel);
    assertThat(cancelResult.result()).isEqualTo("tesSUCCESS");
    logger.info(
      "CheckCancel transaction successful: https://testnet.xrpl.org/transactions/{}",
      cancelResult.transactionResult().hash()
    );

    //////////////////////
    // Validate that the Check does not exist after cancelling
    this.scanForResult(
      () -> this.getValidatedAccountObjects(sourceWallet.address()),
      result -> result.accountObjects().stream().noneMatch(findCheck(sourceWallet, destinationWallet, invoiceId)));
  }

  private Predicate<LedgerObject> findCheck(Wallet sourceWallet, Wallet destinationWallet, Hash256 invoiceId) {
    return object ->
      CheckObject.class.isAssignableFrom(object.getClass()) &&
        ((CheckObject) object).invoiceId().map(id -> id.equals(invoiceId)).orElse(false) &&
        ((CheckObject) object).account().equals(sourceWallet.address()) &&
        ((CheckObject) object).destination().equals(destinationWallet.address());
  }

}
