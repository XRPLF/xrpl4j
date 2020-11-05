package com.ripple.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.hash.Hashing;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.model.transactions.CheckCancel;
import com.ripple.xrpl4j.model.transactions.CheckCash;
import com.ripple.xrpl4j.model.transactions.CheckCreate;
import com.ripple.xrpl4j.model.transactions.Hash256;
import com.ripple.xrpl4j.model.transactions.XrpCurrencyAmount;
import com.ripple.xrpl4j.wallet.Wallet;
import com.ripple.xrplj4.client.model.accounts.AccountInfoResult;
import com.ripple.xrplj4.client.model.accounts.AccountObjectsResult;
import com.ripple.xrplj4.client.model.fees.FeeResult;
import com.ripple.xrplj4.client.model.ledger.Check;
import com.ripple.xrplj4.client.model.ledger.LedgerObject;
import com.ripple.xrplj4.client.model.transactions.SubmissionResult;
import com.ripple.xrplj4.client.rippled.RippledClientErrorException;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

public class CheckIT extends AbstractIT {

  @Test
  public void createXrpCheckAndCash() throws RippledClientErrorException {

    Wallet sourceWallet = createRandomAccount();
    Wallet destinationWallet = createRandomAccount();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfoResult = this.scanForAccountInfo(sourceWallet.classicAddress());

    Hash256 invoiceId = Hash256.of(Hashing.sha256().hashBytes("Check this out.".getBytes()).toString());
    CheckCreate checkCreate = CheckCreate.builder()
      .account(sourceWallet.classicAddress())
      .fee(feeResult.drops().minimumFee())
      .sequence(accountInfoResult.accountData().sequence())
      .destination(destinationWallet.classicAddress())
      .sendMax(XrpCurrencyAmount.of("12345"))
      .invoiceId(invoiceId)
      .signingPublicKey(sourceWallet.publicKey())
      .build();

    SubmissionResult<CheckCreate> response = xrplClient.submit(sourceWallet, checkCreate, CheckCreate.class);
    assertThat(response.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info(
      "CheckCreate transaction successful: https://testnet.xrpl.org/transactions/{}",
      response.transaction().hash().orElse("n/a")
    );

    Check check = (Check) scanAccountObjectsForCondition(sourceWallet.classicAddress(), result -> {
      logger.info("AccountObjectsResult objects: {}", result.accountObjects());
      return result.accountObjects().stream().anyMatch(findCheck(sourceWallet, destinationWallet, invoiceId));
    })
      .accountObjects().stream()
      .filter(findCheck(sourceWallet, destinationWallet, invoiceId))
      .findFirst().get();

    feeResult = xrplClient.fee();
    AccountInfoResult destinationAccountInfo = this.scanForAccountInfo(destinationWallet.classicAddress());
    CheckCash checkCash = CheckCash.builder()
      .account(destinationWallet.classicAddress())
      .amount(check.sendMax())
      .sequence(destinationAccountInfo.accountData().sequence())
      .fee(feeResult.drops().minimumFee())
      .checkId(check.index())
      .signingPublicKey(destinationWallet.publicKey())
      .build();
    SubmissionResult<CheckCash> cashResponse = xrplClient.submit(destinationWallet, checkCash, CheckCash.class);
    assertThat(cashResponse.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info(
      "CheckCash transaction successful: https://testnet.xrpl.org/transactions/{}",
      cashResponse.transaction().hash().orElse("n/a")
    );

    scanAccountInfoForCondition(destinationWallet.classicAddress(), result -> {
      logger.info("AccountInfoResult after CheckCash balance: {}", result.accountData().balance());
      return result.accountData().balance().equals(
        UnsignedInteger.valueOf(destinationAccountInfo.accountData().balance())
          .plus(UnsignedInteger.valueOf(((XrpCurrencyAmount) check.sendMax()).value()))
          .minus(UnsignedInteger.valueOf(checkCash.fee().value())).toString());
    });

    AccountObjectsResult accountObjectsResult = scanForAccountObjects(sourceWallet.classicAddress());
    assertThat(accountObjectsResult.accountObjects()).matches(objects ->
      objects.stream().noneMatch(findCheck(sourceWallet, destinationWallet, invoiceId))
    );
  }

  @Test
  public void createCheckAndSourceCancels() throws RippledClientErrorException {
    Wallet sourceWallet = createRandomAccount();
    Wallet destinationWallet = createRandomAccount();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfoResult = this.scanForAccountInfo(sourceWallet.classicAddress());

    Hash256 invoiceId = Hash256.of(Hashing.sha256().hashBytes("Check this out.".getBytes()).toString());
    CheckCreate checkCreate = CheckCreate.builder()
      .account(sourceWallet.classicAddress())
      .fee(feeResult.drops().minimumFee())
      .sequence(accountInfoResult.accountData().sequence())
      .destination(destinationWallet.classicAddress())
      .sendMax(XrpCurrencyAmount.of("12345"))
      .invoiceId(invoiceId)
      .signingPublicKey(sourceWallet.publicKey())
      .build();

    SubmissionResult<CheckCreate> response = xrplClient.submit(sourceWallet, checkCreate, CheckCreate.class);
    assertThat(response.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info(
      "CheckCreate transaction successful: https://testnet.xrpl.org/transactions/{}",
      response.transaction().hash().orElse("n/a")
    );

    Check check = (Check) scanAccountObjectsForCondition(sourceWallet.classicAddress(), result -> {
      logger.info("AccountObjectsResult objects: {}", result.accountObjects());
      return result.accountObjects().stream().anyMatch(findCheck(sourceWallet, destinationWallet, invoiceId));
    })
      .accountObjects().stream()
      .filter(findCheck(sourceWallet, destinationWallet, invoiceId))
      .findFirst().get();

    feeResult = xrplClient.fee();
    CheckCancel checkCancel = CheckCancel.builder()
      .account(sourceWallet.classicAddress())
      .sequence(accountInfoResult.accountData().sequence().plus(UnsignedInteger.ONE))
      .fee(feeResult.drops().minimumFee())
      .checkId(check.index())
      .signingPublicKey(sourceWallet.publicKey())
      .build();

    SubmissionResult<CheckCancel> cancelResult = xrplClient.submit(sourceWallet, checkCancel, CheckCancel.class);
    assertThat(cancelResult.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info(
      "CheckCancel transaction successful: https://testnet.xrpl.org/transactions/{}",
      cancelResult.transaction().hash().orElse("n/a")
    );

    AccountObjectsResult accountObjectsResult = scanForAccountObjects(sourceWallet.classicAddress());
    assertThat(accountObjectsResult.accountObjects()).matches(objects ->
      objects.stream().noneMatch(findCheck(sourceWallet, destinationWallet, invoiceId))
    );
  }

  @Test
  public void createCheckAndDestinationCancels() throws RippledClientErrorException {
    Wallet sourceWallet = createRandomAccount();
    Wallet destinationWallet = createRandomAccount();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfoResult = this.scanForAccountInfo(sourceWallet.classicAddress());

    Hash256 invoiceId = Hash256.of(Hashing.sha256().hashBytes("Check this out.".getBytes()).toString());
    CheckCreate checkCreate = CheckCreate.builder()
      .account(sourceWallet.classicAddress())
      .fee(feeResult.drops().minimumFee())
      .sequence(accountInfoResult.accountData().sequence())
      .destination(destinationWallet.classicAddress())
      .sendMax(XrpCurrencyAmount.of("12345"))
      .invoiceId(invoiceId)
      .signingPublicKey(sourceWallet.publicKey())
      .build();

    SubmissionResult<CheckCreate> response = xrplClient.submit(sourceWallet, checkCreate, CheckCreate.class);
    assertThat(response.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info(
      "CheckCreate transaction successful: https://testnet.xrpl.org/transactions/{}",
      response.transaction().hash().orElse("n/a")
    );

    Check check = (Check) scanAccountObjectsForCondition(sourceWallet.classicAddress(), result -> {
      logger.info("AccountObjectsResult objects: {}", result.accountObjects());
      return result.accountObjects().stream().anyMatch(findCheck(sourceWallet, destinationWallet, invoiceId));
    })
      .accountObjects().stream()
      .filter(findCheck(sourceWallet, destinationWallet, invoiceId))
      .findFirst().get();

    feeResult = xrplClient.fee();
    AccountInfoResult destinationAccountInfo = this.scanForAccountInfo(destinationWallet.classicAddress());
    CheckCancel checkCancel = CheckCancel.builder()
      .account(destinationWallet.classicAddress())
      .sequence(destinationAccountInfo.accountData().sequence())
      .fee(feeResult.drops().minimumFee())
      .checkId(check.index())
      .signingPublicKey(destinationWallet.publicKey())
      .build();

    SubmissionResult<CheckCancel> cancelResult = xrplClient.submit(destinationWallet, checkCancel, CheckCancel.class);
    assertThat(cancelResult.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info(
      "CheckCancel transaction successful: https://testnet.xrpl.org/transactions/{}",
      cancelResult.transaction().hash().orElse("n/a")
    );

    AccountObjectsResult accountObjectsResult = scanForAccountObjects(sourceWallet.classicAddress());
    assertThat(accountObjectsResult.accountObjects()).matches(objects ->
      objects.stream().noneMatch(findCheck(sourceWallet, destinationWallet, invoiceId))
    );
  }

  public Predicate<LedgerObject> findCheck(Wallet sourceWallet, Wallet destinationWallet, Hash256 invoiceId) {
    return object ->
      Check.class.isAssignableFrom(object.getClass()) &&
        ((Check) object).invoiceId().map(id -> id.equals(invoiceId)).orElse(false) &&
        ((Check) object).account().equals(sourceWallet.classicAddress()) &&
        ((Check) object).destination().equals(destinationWallet.classicAddress());
  }

}
