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
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.flags.MpTokenAuthorizeFlags;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceCreateFlags;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceSetFlags;
import org.xrpl.xrpl4j.model.ledger.MpTokenIssuanceObject;
import org.xrpl.xrpl4j.model.ledger.MpTokenObject;
import org.xrpl.xrpl4j.model.ledger.PermissionedDomainObject;
import org.xrpl.xrpl4j.model.transactions.AssetScale;
import org.xrpl.xrpl4j.model.transactions.Clawback;
import org.xrpl.xrpl4j.model.transactions.CredentialType;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.MpTokenAuthorize;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceCreate;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceDestroy;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceSet;
import org.xrpl.xrpl4j.model.transactions.MpTokenMetadata;
import org.xrpl.xrpl4j.model.transactions.MpTokenNumericAmount;
import org.xrpl.xrpl4j.model.transactions.MptCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.TransferFee;

import java.math.BigDecimal;

public class MpTokenIT extends AbstractIT {

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

  @DisabledIf(value = "shouldNotRunPermissionedDomain",
    disabledReason = "PermissionedDomain requires a feature only available on the develop rippled image.")
  @Test
  void mptIssuanceWithPermissionedDomainSuccessAndFailure()
    throws JsonRpcClientErrorException, JsonProcessingException {

    // Step 1: Create accounts for domain owner, credential issuer, MPT issuer, and two potential holders
    KeyPair domainOwnerKeyPair = createRandomAccountEd25519();
    KeyPair credentialIssuerKeyPair = createRandomAccountEd25519();
    KeyPair mptIssuerKeyPair = createRandomAccountEd25519();
    KeyPair authorizedHolderKeyPair = createRandomAccountEd25519();
    final KeyPair unauthorizedHolderKeyPair = createRandomAccountEd25519();

    // Step 2: Create a Permissioned Domain with a specific credential type requirement
    CredentialType credentialType = CredentialType.ofPlainText("KYC");
    createPermissionedDomain(domainOwnerKeyPair, credentialIssuerKeyPair, new CredentialType[]{credentialType});

    // Step 3: Fetch the Permissioned Domain object to get its ID
    PermissionedDomainObject domainObject = getPermissionedDomainObject(domainOwnerKeyPair.publicKey().deriveAddress());
    Hash256 domainId = domainObject.index();

    // Step 4: Issue a credential to the authorized holder
    createAndAcceptCredentials(credentialIssuerKeyPair, authorizedHolderKeyPair, new CredentialType[]{credentialType});

    // Step 5: Create an MPToken issuance linked to the Permissioned Domain via DomainID
    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult mptIssuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(mptIssuerKeyPair.publicKey().deriveAddress())
    );

    MpTokenIssuanceCreateFlags flags = MpTokenIssuanceCreateFlags.builder()
      .tfMptCanLock(true)
      .tfMptCanTransfer(true)
      .tfMptRequireAuth(true)
      .build();

    MpTokenIssuanceCreate issuanceCreate = MpTokenIssuanceCreate.builder()
      .account(mptIssuerKeyPair.publicKey().deriveAddress())
      .sequence(mptIssuerAccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .lastLedgerSequence(
        mptIssuerAccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue()
      )
      .signingPublicKey(mptIssuerKeyPair.publicKey())
      .assetScale(AssetScale.of(UnsignedInteger.valueOf(2)))
      .transferFee(TransferFee.ofPercent(BigDecimal.valueOf(0.01)))
      .maximumAmount(MpTokenNumericAmount.of(Long.MAX_VALUE))
      .domainId(domainId)
      .flags(flags)
      .build();

    SingleSignedTransaction<MpTokenIssuanceCreate> signedIssuanceCreate = signatureService.sign(
      mptIssuerKeyPair.privateKey(),
      issuanceCreate
    );
    SubmitResult<MpTokenIssuanceCreate> issuanceCreateSubmitResult = xrplClient.submit(signedIssuanceCreate);
    assertThat(issuanceCreateSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    // Step 6: Wait for the transaction to be validated
    this.scanForResult(
      () -> xrplClient.isFinal(
        signedIssuanceCreate.hash(),
        issuanceCreateSubmitResult.validatedLedgerIndex(),
        issuanceCreate.lastLedgerSequence().orElseThrow(RuntimeException::new),
        issuanceCreate.sequence(),
        mptIssuerKeyPair.publicKey().deriveAddress()
      ),
      result -> result.finalityStatus() == FinalityStatus.VALIDATED_SUCCESS
    );

    // Step 7: Retrieve the MPToken issuance ID from the transaction metadata
    MpTokenIssuanceId mpTokenIssuanceId = xrplClient.transaction(
        TransactionRequestParams.of(signedIssuanceCreate.hash()),
        MpTokenIssuanceCreate.class
      ).metadata()
      .orElseThrow(RuntimeException::new)
      .mpTokenIssuanceId()
      .orElseThrow(() -> new RuntimeException("issuance create metadata did not contain issuance ID"));

    // Step 8: Verify the issuance object contains the DomainID
    MpTokenIssuanceObject issuanceFromLedgerEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.mpTokenIssuance(
        mpTokenIssuanceId,
        LedgerSpecifier.VALIDATED
      )
    ).node();

    assertThat(issuanceFromLedgerEntry.domainId()).isPresent();
    assertThat(issuanceFromLedgerEntry.domainId().get()).isEqualTo(domainId);
    assertThat(issuanceFromLedgerEntry.issuer()).isEqualTo(mptIssuerKeyPair.publicKey().deriveAddress());

    // Step 9: SUCCESS CASE - Authorized holder (with credential) can authorize and receive MPTokens
    AccountInfoResult authorizedHolderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(authorizedHolderKeyPair.publicKey().deriveAddress())
    );

    MpTokenAuthorize authorizedHolderAuthorize = MpTokenAuthorize.builder()
      .account(authorizedHolderKeyPair.publicKey().deriveAddress())
      .sequence(authorizedHolderAccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .signingPublicKey(authorizedHolderKeyPair.publicKey())
      .lastLedgerSequence(
        authorizedHolderAccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue()
      )
      .mpTokenIssuanceId(mpTokenIssuanceId)
      .build();

    SingleSignedTransaction<MpTokenAuthorize> signedAuthorizedHolderAuthorize = signatureService.sign(
      authorizedHolderKeyPair.privateKey(),
      authorizedHolderAuthorize
    );
    SubmitResult<MpTokenAuthorize> authorizedHolderAuthorizeSubmitResult = xrplClient.submit(
      signedAuthorizedHolderAuthorize
    );
    assertThat(authorizedHolderAuthorizeSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    this.scanForResult(
      () -> xrplClient.isFinal(
        signedAuthorizedHolderAuthorize.hash(),
        authorizedHolderAuthorizeSubmitResult.validatedLedgerIndex(),
        authorizedHolderAuthorize.lastLedgerSequence().orElseThrow(RuntimeException::new),
        authorizedHolderAuthorize.sequence(),
        authorizedHolderKeyPair.publicKey().deriveAddress()
      ),
      result -> result.finalityStatus() == FinalityStatus.VALIDATED_SUCCESS
    );

    // Step 10: Send MPTokens to the authorized holder - this should succeed
    MptCurrencyAmount mintAmount = MptCurrencyAmount.builder()
      .mptIssuanceId(mpTokenIssuanceId)
      .value("100000")
      .build();

    Payment mintToAuthorizedHolder = Payment.builder()
      .account(mptIssuerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(mptIssuerAccountInfo.accountData().sequence().plus(UnsignedInteger.ONE))
      .destination(authorizedHolderKeyPair.publicKey().deriveAddress())
      .amount(mintAmount)
      .signingPublicKey(mptIssuerKeyPair.publicKey())
      .lastLedgerSequence(
        mptIssuerAccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(1000)).unsignedIntegerValue()
      )
      .build();

    SingleSignedTransaction<Payment> signedMintToAuthorizedHolder = signatureService.sign(
      mptIssuerKeyPair.privateKey(),
      mintToAuthorizedHolder
    );
    SubmitResult<Payment> mintToAuthorizedHolderSubmitResult = xrplClient.submit(signedMintToAuthorizedHolder);
    assertThat(mintToAuthorizedHolderSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    this.scanForResult(
      () -> this.getValidatedTransaction(
        signedMintToAuthorizedHolder.hash(),
        MpTokenAuthorize.class
      )
    );

    // Step 11: FAILURE CASE - Unauthorized holder (without credential) cannot authorize for the MPToken
    AccountInfoResult unauthorizedHolderAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(unauthorizedHolderKeyPair.publicKey().deriveAddress())
    );

    MpTokenAuthorize unauthorizedHolderAuthorize = MpTokenAuthorize.builder()
      .account(unauthorizedHolderKeyPair.publicKey().deriveAddress())
      .sequence(unauthorizedHolderAccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .signingPublicKey(unauthorizedHolderKeyPair.publicKey())
      .lastLedgerSequence(
        unauthorizedHolderAccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(50)).unsignedIntegerValue()
      )
      .mpTokenIssuanceId(mpTokenIssuanceId)
      .build();

    SingleSignedTransaction<MpTokenAuthorize> signedUnauthorizedHolderAuthorize = signatureService.sign(
      unauthorizedHolderKeyPair.privateKey(),
      unauthorizedHolderAuthorize
    );
    SubmitResult<MpTokenAuthorize> unauthorizedHolderAuthorizeSubmitResult = xrplClient.submit(
      signedUnauthorizedHolderAuthorize
    );
    assertThat(unauthorizedHolderAuthorizeSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    // Wait until the transaction gets committed to a validated ledger
    TransactionResult<MpTokenAuthorize> validatedUnauthorizedAuthorize = this.scanForResult(
      () -> this.getValidatedTransaction(
        signedUnauthorizedHolderAuthorize.hash(),
        MpTokenAuthorize.class
      )
    );

    // Assert the transaction result from validated ledger
    assertThat(validatedUnauthorizedAuthorize.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);

    // Step 12: Try to send MPTokens to the unauthorized holder - this should fail
    MptCurrencyAmount mintAmountUnauthorized = MptCurrencyAmount.builder()
      .mptIssuanceId(mpTokenIssuanceId)
      .value("50000")
      .build();

    Payment mintToUnauthorizedHolder = Payment.builder()
      .account(mptIssuerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(mptIssuerAccountInfo.accountData().sequence().plus(UnsignedInteger.valueOf(2)))
      .destination(unauthorizedHolderKeyPair.publicKey().deriveAddress())
      .amount(mintAmountUnauthorized)
      .signingPublicKey(mptIssuerKeyPair.publicKey())
      .lastLedgerSequence(
        mptIssuerAccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(1000)).unsignedIntegerValue()
      )
      .build();

    SingleSignedTransaction<Payment> signedMintToUnauthorizedHolder = signatureService.sign(
      mptIssuerKeyPair.privateKey(),
      mintToUnauthorizedHolder
    );
    SubmitResult<Payment> mintToUnauthorizedHolderSubmitResult = xrplClient.submit(signedMintToUnauthorizedHolder);
    assertThat(mintToUnauthorizedHolderSubmitResult.engineResult()).isEqualTo("tecNO_AUTH");
  }

  static boolean shouldNotRunPermissionedDomain() {
    return System.getProperty("useTestnet") != null ||
      System.getProperty("useClioTestnet") != null ||
      System.getProperty("useDevnet") != null;
  }
}

