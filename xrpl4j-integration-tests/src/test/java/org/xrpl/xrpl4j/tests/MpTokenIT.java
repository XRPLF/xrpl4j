package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.FinalityStatus;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsRequestParams.AccountObjectType;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.MpTokenLedgerEntryParams;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionRequestParams;
import org.xrpl.xrpl4j.model.flags.MpTokenAuthorizeFlags;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceCreateFlags;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceSetFlags;
import org.xrpl.xrpl4j.model.ledger.MpTokenIssuanceObject;
import org.xrpl.xrpl4j.model.ledger.MpTokenObject;
import org.xrpl.xrpl4j.model.transactions.AssetScale;
import org.xrpl.xrpl4j.model.transactions.Clawback;
import org.xrpl.xrpl4j.model.transactions.MptCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.MpTokenAuthorize;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceCreate;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceDestroy;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceSet;
import org.xrpl.xrpl4j.model.transactions.MpTokenMetadata;
import org.xrpl.xrpl4j.model.transactions.MpTokenNumericAmount;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.TransferFee;

import java.math.BigDecimal;

@DisabledIf(value = "shouldNotRun", disabledReason = "MpTokenIT only runs on local rippled node or devnet.")
public class MpTokenIT extends AbstractIT {

  static boolean shouldNotRun() {
    return System.getProperty("useTestnet") != null ||
           System.getProperty("useClioTestnet") != null;
  }

  @Test
  void createIssuanceThenPayThenLockThenClawbackThenDestroy()
    throws JsonRpcClientErrorException, JsonProcessingException {
    KeyPair issuerKeyPair = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    MpTokenIssuanceCreateFlags flags = MpTokenIssuanceCreateFlags.builder()
      .tfMptCanLock(true)
      .tfMptCanEscrow(true)
      .tfMptCanTrade(true)
      .tfMptCanTransfer(true)
      .tfMptCanClawback(true)
      .build();
    MpTokenIssuanceCreate issuanceCreate = MpTokenIssuanceCreate.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .sequence(issuerAccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .lastLedgerSequence(issuerAccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue())
      .signingPublicKey(issuerKeyPair.publicKey())
      .assetScale(AssetScale.of(UnsignedInteger.valueOf(2)))
      .transferFee(TransferFee.ofPercent(BigDecimal.valueOf(0.01)))
      .maximumAmount(MpTokenNumericAmount.of(Long.MAX_VALUE))
      .mpTokenMetadata(MpTokenMetadata.of("ABCD"))
      .flags(
        flags
      )
      .build();

    SingleSignedTransaction<MpTokenIssuanceCreate> signedIssuanceCreate = signatureService.sign(
      issuerKeyPair.privateKey(),
      issuanceCreate
    );
    SubmitResult<MpTokenIssuanceCreate> issuanceCreateSubmitResult = xrplClient.submit(signedIssuanceCreate);
    assertThat(issuanceCreateSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    this.scanForResult(
      () -> xrplClient.isFinal(
        signedIssuanceCreate.hash(),
        issuanceCreateSubmitResult.validatedLedgerIndex(),
        issuanceCreate.lastLedgerSequence().orElseThrow(RuntimeException::new),
        issuanceCreate.sequence(),
        issuerKeyPair.publicKey().deriveAddress()
      ),
      result -> result.finalityStatus() == FinalityStatus.VALIDATED_SUCCESS
    );

    MpTokenIssuanceId mpTokenIssuanceId = xrplClient.transaction(
        TransactionRequestParams.of(signedIssuanceCreate.hash()),
        MpTokenIssuanceCreate.class
      ).metadata()
      .orElseThrow(RuntimeException::new)
      .mpTokenIssuanceId()
      .orElseThrow(() -> new RuntimeException("issuance create metadata did not contain issuance ID"));
    MpTokenIssuanceObject issuanceFromLedgerEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.mpTokenIssuance(
        mpTokenIssuanceId,
        LedgerSpecifier.VALIDATED
      )
    ).node();

    assertThat(issuanceFromLedgerEntry.flags().lsfMptLocked()).isFalse();
    assertThat(issuanceFromLedgerEntry.flags().lsfMptCanLock()).isTrue();
    assertThat(issuanceFromLedgerEntry.flags().lsfMptRequireAuth()).isFalse();
    assertThat(issuanceFromLedgerEntry.flags().lsfMptCanEscrow()).isTrue();
    assertThat(issuanceFromLedgerEntry.flags().lsfMptCanTrade()).isTrue();
    assertThat(issuanceFromLedgerEntry.flags().lsfMptCanTransfer()).isTrue();
    assertThat(issuanceFromLedgerEntry.flags().lsfMptCanClawback()).isTrue();
    assertThat(issuanceFromLedgerEntry.issuer()).isEqualTo(issuerKeyPair.publicKey().deriveAddress());
    assertThat(issuanceFromLedgerEntry.sequence()).isEqualTo(issuanceCreate.sequence());
    assertThat(issuanceFromLedgerEntry.transferFee()).isEqualTo(
      issuanceCreate.transferFee().orElseThrow(RuntimeException::new));
    assertThat(issuanceFromLedgerEntry.assetScale()).isEqualTo(
      issuanceCreate.assetScale().orElseThrow(RuntimeException::new));
    assertThat(issuanceFromLedgerEntry.maximumAmount()).isNotEmpty().get()
      .isEqualTo(issuanceCreate.maximumAmount().orElseThrow(RuntimeException::new));
    assertThat(issuanceFromLedgerEntry.outstandingAmount()).isEqualTo(MpTokenNumericAmount.of(0));

    assertThat(xrplClient.accountObjects(
      AccountObjectsRequestParams.builder()
        .type(AccountObjectType.MPT_ISSUANCE)
        .account(issuerKeyPair.publicKey().deriveAddress())
        .ledgerSpecifier(LedgerSpecifier.VALIDATED)
        .build()
    ).accountObjects()).containsExactly(issuanceFromLedgerEntry);

    KeyPair holder1KeyPair = createRandomAccountEd25519();
    AccountInfoResult holder1AccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(holder1KeyPair.publicKey().deriveAddress())
    );
    MpTokenAuthorize authorize = MpTokenAuthorize.builder()
      .account(holder1KeyPair.publicKey().deriveAddress())
      .sequence(holder1AccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .signingPublicKey(holder1KeyPair.publicKey())
      .lastLedgerSequence(holder1AccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue())
      .mpTokenIssuanceId(mpTokenIssuanceId)
      .build();
    SingleSignedTransaction<MpTokenAuthorize> signedAuthorize = signatureService.sign(holder1KeyPair.privateKey(),
      authorize);
    SubmitResult<MpTokenAuthorize> authorizeSubmitResult = xrplClient.submit(signedAuthorize);
    assertThat(authorizeSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    this.scanForResult(
      () -> xrplClient.isFinal(
        signedAuthorize.hash(),
        authorizeSubmitResult.validatedLedgerIndex(),
        authorize.lastLedgerSequence().orElseThrow(RuntimeException::new),
        authorize.sequence(),
        holder1KeyPair.publicKey().deriveAddress()
      ),
      result -> result.finalityStatus() == FinalityStatus.VALIDATED_SUCCESS
    );

    MptCurrencyAmount mintAmount = MptCurrencyAmount.builder()
      .mptIssuanceId(mpTokenIssuanceId)
      .value("100000")
      .build();
    Payment mint = Payment.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(issuerAccountInfo.accountData().sequence().plus(UnsignedInteger.ONE))
      .destination(holder1KeyPair.publicKey().deriveAddress())
      .amount(mintAmount)
      .signingPublicKey(issuerKeyPair.publicKey())
      .lastLedgerSequence(
        issuerAccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(1000)).unsignedIntegerValue()
      )
      .build();

    SingleSignedTransaction<Payment> signedMint = signatureService.sign(issuerKeyPair.privateKey(), mint);
    SubmitResult<Payment> mintSubmitResult = xrplClient.submit(signedMint);
    assertThat(mintSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    this.scanForResult(
      () -> xrplClient.isFinal(
        signedMint.hash(),
        mintSubmitResult.validatedLedgerIndex(),
        mint.lastLedgerSequence().orElseThrow(RuntimeException::new),
        mint.sequence(),
        issuerKeyPair.publicKey().deriveAddress()
      ),
      result -> result.finalityStatus() == FinalityStatus.VALIDATED_SUCCESS
    );

    MpTokenIssuanceObject issuanceAfterPayment = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.mpTokenIssuance(
        mpTokenIssuanceId,
        LedgerSpecifier.VALIDATED
      )
    ).node();

    assertThat(issuanceAfterPayment).usingRecursiveComparison().ignoringFields(
        "outstandingAmount",
        "previousTransactionLedgerSequence",
        "previousTransactionId"
      )
      .isEqualTo(issuanceFromLedgerEntry);
    assertThat(issuanceAfterPayment.outstandingAmount()).isEqualTo(
      MpTokenNumericAmount.of(mintAmount.unsignedLongValue()));

    MpTokenObject holderMpToken = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.mpToken(
        MpTokenLedgerEntryParams.builder()
          .account(holder1KeyPair.publicKey().deriveAddress())
          .mpTokenIssuanceId(mpTokenIssuanceId)
          .build(),
        LedgerSpecifier.VALIDATED
      )
    ).node();

    assertThat(holderMpToken.flags().lsfMptAuthorized()).isFalse();
    assertThat(holderMpToken.flags().lsfMptLocked()).isFalse();
    assertThat(holderMpToken.mpTokenIssuanceId()).isEqualTo(mpTokenIssuanceId);
    assertThat(holderMpToken.mptAmount()).isEqualTo(MpTokenNumericAmount.of(mintAmount.unsignedLongValue()));

    assertThat(xrplClient.accountObjects(
      AccountObjectsRequestParams.builder()
        .type(AccountObjectType.MP_TOKEN)
        .account(holder1KeyPair.publicKey().deriveAddress())
        .ledgerSpecifier(LedgerSpecifier.VALIDATED)
        .build()
    ).accountObjects()).containsExactly(holderMpToken);

    issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    MpTokenIssuanceSet lock = MpTokenIssuanceSet.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .sequence(mint.sequence().plus(UnsignedInteger.ONE))
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .lastLedgerSequence(issuerAccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue())
      .mpTokenIssuanceId(mpTokenIssuanceId)
      .flags(MpTokenIssuanceSetFlags.LOCK)
      .signingPublicKey(issuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<MpTokenIssuanceSet> signedLock = signatureService.sign(issuerKeyPair.privateKey(), lock);
    SubmitResult<MpTokenIssuanceSet> lockSubmitResult = xrplClient.submit(signedLock);
    assertThat(lockSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    this.scanForResult(
      () -> xrplClient.isFinal(
        signedLock.hash(),
        lockSubmitResult.validatedLedgerIndex(),
        lock.lastLedgerSequence().orElseThrow(RuntimeException::new),
        lock.sequence(),
        issuerKeyPair.publicKey().deriveAddress()
      ),
      result -> result.finalityStatus() == FinalityStatus.VALIDATED_SUCCESS
    );

    assertThat(xrplClient.ledgerEntry(
      LedgerEntryRequestParams.mpTokenIssuance(mpTokenIssuanceId, LedgerSpecifier.VALIDATED)
    ).node().flags().lsfMptLocked()).isTrue();

    Clawback clawback = Clawback.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(lock.fee())
      .sequence(lock.sequence().plus(UnsignedInteger.ONE))
      .signingPublicKey(issuerKeyPair.publicKey())
      .lastLedgerSequence(
        lockSubmitResult.validatedLedgerIndex().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue()
      )
      .amount(mintAmount)
      .holder(holder1KeyPair.publicKey().deriveAddress())
      .build();

    SingleSignedTransaction<Clawback> signedClawback = signatureService.sign(issuerKeyPair.privateKey(), clawback);
    SubmitResult<Clawback> clawbackSubmitResult = xrplClient.submit(signedClawback);
    assertThat(clawbackSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    this.scanForResult(
      () -> xrplClient.isFinal(
        signedClawback.hash(),
        clawbackSubmitResult.validatedLedgerIndex(),
        clawback.lastLedgerSequence().orElseThrow(RuntimeException::new),
        clawback.sequence(),
        issuerKeyPair.publicKey().deriveAddress()
      ),
      result -> result.finalityStatus() == FinalityStatus.VALIDATED_SUCCESS
    );

    assertThat(xrplClient.ledgerEntry(
      LedgerEntryRequestParams.mpTokenIssuance(mpTokenIssuanceId, LedgerSpecifier.VALIDATED)
    ).node().outstandingAmount()).isEqualTo(MpTokenNumericAmount.of(0));
    assertThat(xrplClient.ledgerEntry(
      LedgerEntryRequestParams.mpToken(MpTokenLedgerEntryParams.builder()
          .mpTokenIssuanceId(mpTokenIssuanceId)
          .account(holder1KeyPair.publicKey().deriveAddress())
        .build(), LedgerSpecifier.VALIDATED)
    ).node().mptAmount()).isEqualTo(MpTokenNumericAmount.of(0));

    MpTokenAuthorize unauthorize = MpTokenAuthorize.builder()
      .account(holder1KeyPair.publicKey().deriveAddress())
      .sequence(authorize.sequence().plus(UnsignedInteger.ONE))
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .signingPublicKey(holder1KeyPair.publicKey())
      .lastLedgerSequence(
        clawbackSubmitResult.validatedLedgerIndex().plus(UnsignedInteger.valueOf(100)).unsignedIntegerValue()
      )
      .mpTokenIssuanceId(mpTokenIssuanceId)
      .flags(MpTokenAuthorizeFlags.UNAUTHORIZE)
      .build();
    SingleSignedTransaction<MpTokenAuthorize> signedUnauthorize = signatureService.sign(holder1KeyPair.privateKey(),
      unauthorize);
    SubmitResult<MpTokenAuthorize> unAuthorizeSubmitResult = xrplClient.submit(signedUnauthorize);
    assertThat(unAuthorizeSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    this.scanForResult(
      () -> xrplClient.isFinal(
        signedUnauthorize.hash(),
        unAuthorizeSubmitResult.validatedLedgerIndex(),
        unauthorize.lastLedgerSequence().orElseThrow(RuntimeException::new),
        unauthorize.sequence(),
        holder1KeyPair.publicKey().deriveAddress()
      ),
      result -> result.finalityStatus() == FinalityStatus.VALIDATED_SUCCESS
    );

    assertThatThrownBy(
      () -> xrplClient.ledgerEntry(
        LedgerEntryRequestParams.mpToken(
          MpTokenLedgerEntryParams.builder()
            .account(holder1KeyPair.publicKey().deriveAddress())
            .mpTokenIssuanceId(mpTokenIssuanceId)
            .build(),
          LedgerSpecifier.VALIDATED
        )
      ).node()
    ).isInstanceOf(JsonRpcClientErrorException.class)
      .hasMessageContaining("entryNotFound");

    MpTokenIssuanceDestroy issuanceDestroy = MpTokenIssuanceDestroy.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(lock.fee())
      .sequence(clawback.sequence().plus(UnsignedInteger.ONE))
      .signingPublicKey(issuerKeyPair.publicKey())
      .lastLedgerSequence(
        unAuthorizeSubmitResult.validatedLedgerIndex().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue()
      )
      .mpTokenIssuanceId(mpTokenIssuanceId)
      .build();

    SingleSignedTransaction<MpTokenIssuanceDestroy> signedDestroy = signatureService.sign(issuerKeyPair.privateKey(),
      issuanceDestroy);
    SubmitResult<MpTokenIssuanceDestroy> destroySubmitResult = xrplClient.submit(signedDestroy);
    assertThat(destroySubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    this.scanForResult(
      () -> xrplClient.isFinal(
        signedDestroy.hash(),
        destroySubmitResult.validatedLedgerIndex(),
        issuanceDestroy.lastLedgerSequence().orElseThrow(RuntimeException::new),
        issuanceDestroy.sequence(),
        issuerKeyPair.publicKey().deriveAddress()
      ),
      result -> result.finalityStatus() == FinalityStatus.VALIDATED_SUCCESS
    );

    assertThatThrownBy(
      () -> xrplClient.ledgerEntry(
        LedgerEntryRequestParams.mpTokenIssuance(
          mpTokenIssuanceId,
          LedgerSpecifier.VALIDATED
        )
      ).node()
    ).isInstanceOf(JsonRpcClientErrorException.class)
      .hasMessageContaining("entryNotFound");
  }
}
