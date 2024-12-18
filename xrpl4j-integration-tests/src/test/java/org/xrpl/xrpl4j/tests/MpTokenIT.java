package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
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
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryResult;
import org.xrpl.xrpl4j.model.client.ledger.MpTokenLedgerEntryParams;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionRequestParams;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceCreateFlags;
import org.xrpl.xrpl4j.model.ledger.MpTokenIssuanceObject;
import org.xrpl.xrpl4j.model.ledger.MpTokenObject;
import org.xrpl.xrpl4j.model.transactions.AssetScale;
import org.xrpl.xrpl4j.model.transactions.ImmutableMpTokenAmount;
import org.xrpl.xrpl4j.model.transactions.ImmutableMpTokenAuthorize;
import org.xrpl.xrpl4j.model.transactions.MpTokenAmount;
import org.xrpl.xrpl4j.model.transactions.MpTokenAuthorize;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceCreate;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.MpTokenObjectAmount;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.TransferFee;

import java.math.BigDecimal;

public class MpTokenIT extends AbstractIT {

  @Test
  void createIssuanceAndPay() throws JsonRpcClientErrorException, JsonProcessingException {
    KeyPair issuerKeyPair = createRandomAccountEd25519();
    KeyPair holder1KeyPair = createRandomAccountEd25519();
    KeyPair holder2KeyPair = createRandomAccountEd25519();

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
      .lastLedgerSequence(issuerAccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(4)).unsignedIntegerValue())
      .signingPublicKey(issuerKeyPair.publicKey())
      .assetScale(AssetScale.of(UnsignedInteger.valueOf(2)))
      .transferFee(TransferFee.ofPercent(BigDecimal.valueOf(0.01)))
      .maximumAmount(MpTokenObjectAmount.of(Long.MAX_VALUE))
      .mpTokenMetadata("ABCD")
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
        issuerAccountInfo.ledgerIndexSafe(),
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
    assertThat(issuanceFromLedgerEntry.outstandingAmount()).isEqualTo(MpTokenObjectAmount.of(0));

    assertThat(xrplClient.accountObjects(
      AccountObjectsRequestParams.builder()
        .type(AccountObjectType.MPT_ISSUANCE)
        .account(issuerKeyPair.publicKey().deriveAddress())
        .ledgerSpecifier(LedgerSpecifier.VALIDATED)
        .build()
    ).accountObjects()).containsExactly(issuanceFromLedgerEntry);

    AccountInfoResult holder1AccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(holder1KeyPair.publicKey().deriveAddress())
    );
    MpTokenAuthorize authorize = MpTokenAuthorize.builder()
      .account(holder1KeyPair.publicKey().deriveAddress())
      .sequence(holder1AccountInfo.accountData().sequence())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .signingPublicKey(holder1KeyPair.publicKey())
      .lastLedgerSequence(holder1AccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(4)).unsignedIntegerValue())
      .mpTokenIssuanceId(mpTokenIssuanceId)
      .build();
    SingleSignedTransaction<MpTokenAuthorize> signedAuthorize = signatureService.sign(holder1KeyPair.privateKey(),
      authorize);
    SubmitResult<MpTokenAuthorize> authorizeSubmitResult = xrplClient.submit(signedAuthorize);
    assertThat(authorizeSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    this.scanForResult(
      () -> xrplClient.isFinal(
        signedAuthorize.hash(),
        issuerAccountInfo.ledgerIndexSafe(),
        authorize.lastLedgerSequence().orElseThrow(RuntimeException::new),
        authorize.sequence(),
        holder1KeyPair.publicKey().deriveAddress()
      ),
      result -> result.finalityStatus() == FinalityStatus.VALIDATED_SUCCESS
    );

    MpTokenAmount mintAmount = MpTokenAmount.builder()
      .mptIssuanceId(mpTokenIssuanceId)
      .value("100000")
      .build();
    Payment mint = Payment.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(issuerAccountInfo.accountData().sequence().plus(UnsignedInteger.ONE))
      .destination(holder1KeyPair.publicKey().deriveAddress())
      .amount(
        mintAmount
      )
      .signingPublicKey(issuerKeyPair.publicKey())
      .lastLedgerSequence(issuerAccountInfo.ledgerIndexSafe().plus(UnsignedInteger.valueOf(10)).unsignedIntegerValue())
      .build();

    SingleSignedTransaction<Payment> signedMint = signatureService.sign(issuerKeyPair.privateKey(), mint);
    SubmitResult<Payment> mintSubmitResult = xrplClient.submit(signedMint);
    assertThat(mintSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    this.scanForResult(
      () -> xrplClient.isFinal(
        signedMint.hash(),
        issuerAccountInfo.ledgerIndexSafe(),
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
      MpTokenObjectAmount.of(mintAmount.unsignedLongValue()));

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
    assertThat(holderMpToken.mptAmount()).isEqualTo(MpTokenObjectAmount.of(mintAmount.unsignedLongValue()));

    assertThat(xrplClient.accountObjects(
      AccountObjectsRequestParams.builder()
        .type(AccountObjectType.MP_TOKEN)
        .account(holder1KeyPair.publicKey().deriveAddress())
        .ledgerSpecifier(LedgerSpecifier.VALIDATED)
        .build()
    ).accountObjects()).containsExactly(holderMpToken);
  }

  // TODO: IT for issuance set and destroy
  //       IT for escrow
  //       IT for clawback
}
