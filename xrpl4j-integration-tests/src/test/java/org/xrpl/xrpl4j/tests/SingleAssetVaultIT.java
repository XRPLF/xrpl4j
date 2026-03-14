package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryResult;
import org.xrpl.xrpl4j.model.client.ledger.VaultLedgerEntryParams;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.client.vault.VaultInfoRequestParams;
import org.xrpl.xrpl4j.model.client.vault.VaultInfoResult;
import org.xrpl.xrpl4j.model.flags.VaultCreateFlags;
import org.xrpl.xrpl4j.model.ledger.IouIssue;
import org.xrpl.xrpl4j.model.ledger.Issue;
import org.xrpl.xrpl4j.model.ledger.MptIssue;
import org.xrpl.xrpl4j.model.ledger.VaultObject;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.AssetScale;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceCreate;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.MpTokenMetadata;
import org.xrpl.xrpl4j.model.transactions.MpTokenNumericAmount;
import org.xrpl.xrpl4j.model.transactions.MptCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.AssetAmount;
import org.xrpl.xrpl4j.model.transactions.VaultCreate;
import org.xrpl.xrpl4j.model.transactions.VaultData;
import org.xrpl.xrpl4j.model.transactions.VaultDelete;
import org.xrpl.xrpl4j.model.transactions.VaultDeposit;
import org.xrpl.xrpl4j.model.transactions.VaultSet;
import org.xrpl.xrpl4j.model.transactions.VaultWithdraw;
import org.xrpl.xrpl4j.model.transactions.WithdrawalPolicy;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.TrustSet;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.flags.TrustSetFlags;
import org.xrpl.xrpl4j.model.transactions.PermissionedDomainSet;
import org.xrpl.xrpl4j.model.transactions.CredentialCreate;
import org.xrpl.xrpl4j.model.transactions.CredentialAccept;
import org.xrpl.xrpl4j.model.transactions.CredentialType;
import org.xrpl.xrpl4j.model.ledger.PermissionedDomainObject;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.ledger.CredentialLedgerEntryParams;

@DisabledIf(value = "shouldNotRun", disabledReason = "SingleAssetVaultIT only runs on local rippled node or devnet.")
public class SingleAssetVaultIT extends AbstractIT {

  boolean shouldNotRun() {
    return System.getProperty("useTestnet") != null || System.getProperty("useClioTestnet") != null;
  }

  @Test
  void vaultWithXrpAsset() throws Exception {
    KeyPair vaultOwnerKeyPair = createRandomAccountEd25519();
    KeyPair depositor1KeyPair = createRandomAccountEd25519();
    KeyPair depositor2KeyPair = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult vaultOwnerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(vaultOwnerKeyPair.publicKey().deriveAddress())
    );

    VaultCreate vaultCreate = VaultCreate.builder()
      .account(vaultOwnerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(vaultOwnerAccountInfo.accountData().sequence())
      .asset(Issue.XRP)
      .assetsMaximum(AssetAmount.of("10000000000"))
      .mpTokenMetadata(MpTokenMetadata.of("AABBCCDD"))
      .withdrawalPolicy(WithdrawalPolicy.FIRST_COME_FIRST_SERVE)
      .data(VaultData.of("48656C6C6F"))
      .signingPublicKey(vaultOwnerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultCreate> signedVaultCreate = signatureService.sign(
      vaultOwnerKeyPair.privateKey(),
      vaultCreate
    );
    SubmitResult<VaultCreate> vaultCreateSubmitResult = xrplClient.submit(signedVaultCreate);
    assertThat(vaultCreateSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    TransactionResult<VaultCreate> validatedVaultCreate = this.scanForResult(
      () -> this.getValidatedTransaction(signedVaultCreate.hash(), VaultCreate.class)
    );
    assertThat(validatedVaultCreate.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);

    Hash256 vaultId = signedVaultCreate.hash();

    VaultInfoResult vaultInfoResult = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    VaultObject vaultObject = vaultInfoResult.vault();
    assertThat(vaultObject.owner()).isEqualTo(vaultOwnerKeyPair.publicKey().deriveAddress());
    assertThat(vaultObject.asset()).isEqualTo(Issue.XRP);
    assertThat(vaultObject.assetsMaximum()).isNotEmpty().get().isEqualTo(AssetAmount.of("10000000000"));
    assertThat(vaultObject.data()).isNotEmpty().get().isEqualTo(VaultData.of("48656C6C6F"));
    assertThat(vaultObject.withdrawalPolicy()).isEqualTo(WithdrawalPolicy.FIRST_COME_FIRST_SERVE);

    LedgerEntryResult<VaultObject> vaultLedgerEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.vault(
        VaultLedgerEntryParams.builder()
          .owner(vaultOwnerKeyPair.publicKey().deriveAddress())
          .seq(vaultOwnerAccountInfo.accountData().sequence())
          .build(),
        LedgerSpecifier.of(vaultInfoResult.ledgerIndexSafe())
      )
    );
    assertThat(vaultLedgerEntry.node()).isEqualTo(vaultObject);

    AccountInfoResult depositor1AccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(depositor1KeyPair.publicKey().deriveAddress())
    );

    VaultDeposit deposit1 = VaultDeposit.builder()
      .account(depositor1KeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(depositor1AccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .amount(XrpCurrencyAmount.ofDrops(1000000))
      .signingPublicKey(depositor1KeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultDeposit> signedDeposit1 = signatureService.sign(
      depositor1KeyPair.privateKey(),
      deposit1
    );
    SubmitResult<VaultDeposit> deposit1SubmitResult = xrplClient.submit(signedDeposit1);
    assertThat(deposit1SubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    TransactionResult<VaultDeposit> validatedDeposit1 = this.scanForResult(
      () -> this.getValidatedTransaction(signedDeposit1.hash(), VaultDeposit.class)
    );
    assertThat(validatedDeposit1.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);

    VaultInfoResult vaultInfoAfterDeposit1 = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    assertThat(vaultInfoAfterDeposit1.vault().assetsTotal()).isNotEmpty().get().isEqualTo(AssetAmount.of("1000000"));
    assertThat(vaultInfoAfterDeposit1.vault().assetsAvailable()).isNotEmpty().get()
      .isEqualTo(AssetAmount.of("1000000"));

    AccountInfoResult depositor2AccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(depositor2KeyPair.publicKey().deriveAddress())
    );

    VaultDeposit deposit2 = VaultDeposit.builder()
      .account(depositor2KeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(depositor2AccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .amount(XrpCurrencyAmount.ofDrops(2000000))
      .signingPublicKey(depositor2KeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultDeposit> signedDeposit2 = signatureService.sign(
      depositor2KeyPair.privateKey(),
      deposit2
    );
    SubmitResult<VaultDeposit> deposit2SubmitResult = xrplClient.submit(signedDeposit2);
    assertThat(deposit2SubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    TransactionResult<VaultDeposit> validatedDeposit2 = this.scanForResult(
      () -> this.getValidatedTransaction(signedDeposit2.hash(), VaultDeposit.class)
    );
    assertThat(validatedDeposit2.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);

    VaultInfoResult vaultInfoAfterDeposit2 = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    assertThat(vaultInfoAfterDeposit2.vault().assetsTotal()).isNotEmpty().get().isEqualTo(AssetAmount.of("3000000"));
    assertThat(vaultInfoAfterDeposit2.vault().assetsAvailable()).isNotEmpty().get()
      .isEqualTo(AssetAmount.of("3000000"));

    depositor1AccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(depositor1KeyPair.publicKey().deriveAddress())
    );

    VaultWithdraw withdraw1 = VaultWithdraw.builder()
      .account(depositor1KeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(depositor1AccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .amount(XrpCurrencyAmount.ofDrops(500000))
      .signingPublicKey(depositor1KeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultWithdraw> signedWithdraw1 = signatureService.sign(
      depositor1KeyPair.privateKey(),
      withdraw1
    );
    SubmitResult<VaultWithdraw> withdraw1SubmitResult = xrplClient.submit(signedWithdraw1);
    assertThat(withdraw1SubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    TransactionResult<VaultWithdraw> validatedWithdraw1 = this.scanForResult(
      () -> this.getValidatedTransaction(signedWithdraw1.hash(), VaultWithdraw.class)
    );
    assertThat(validatedWithdraw1.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);

    VaultInfoResult vaultInfoAfterWithdraw1 = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    assertThat(vaultInfoAfterWithdraw1.vault().assetsTotal()).isNotEmpty().get().isEqualTo(AssetAmount.of("2500000"));
    assertThat(vaultInfoAfterWithdraw1.vault().assetsAvailable()).isNotEmpty().get()
      .isEqualTo(AssetAmount.of("2500000"));

    depositor2AccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(depositor2KeyPair.publicKey().deriveAddress())
    );

    VaultWithdraw withdraw2 = VaultWithdraw.builder()
      .account(depositor2KeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(depositor2AccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .amount(XrpCurrencyAmount.ofDrops(1000000))
      .destination(depositor1KeyPair.publicKey().deriveAddress())
      .signingPublicKey(depositor2KeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultWithdraw> signedWithdraw2 = signatureService.sign(
      depositor2KeyPair.privateKey(),
      withdraw2
    );
    SubmitResult<VaultWithdraw> withdraw2SubmitResult = xrplClient.submit(signedWithdraw2);
    assertThat(withdraw2SubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    TransactionResult<VaultWithdraw> validatedWithdraw2 = this.scanForResult(
      () -> this.getValidatedTransaction(signedWithdraw2.hash(), VaultWithdraw.class)
    );
    assertThat(validatedWithdraw2.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);

    VaultInfoResult vaultInfoAfterWithdraw2 = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    assertThat(vaultInfoAfterWithdraw2.vault().assetsTotal()).isNotEmpty().get().isEqualTo(AssetAmount.of("1500000"));
    assertThat(vaultInfoAfterWithdraw2.vault().assetsAvailable()).isNotEmpty().get()
      .isEqualTo(AssetAmount.of("1500000"));

    vaultOwnerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(vaultOwnerKeyPair.publicKey().deriveAddress())
    );

    VaultSet vaultSet = VaultSet.builder()
      .account(vaultOwnerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(vaultOwnerAccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .assetsMaximum(AssetAmount.of("20000000000"))
      .data(VaultData.of("576F726C64"))
      .signingPublicKey(vaultOwnerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultSet> signedVaultSet = signatureService.sign(
      vaultOwnerKeyPair.privateKey(),
      vaultSet
    );
    SubmitResult<VaultSet> vaultSetSubmitResult = xrplClient.submit(signedVaultSet);
    assertThat(vaultSetSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    TransactionResult<VaultSet> validatedVaultSet = this.scanForResult(
      () -> this.getValidatedTransaction(signedVaultSet.hash(), VaultSet.class)
    );
    assertThat(validatedVaultSet.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);

    VaultInfoResult vaultInfoAfterSet = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    assertThat(vaultInfoAfterSet.vault().assetsMaximum()).isNotEmpty().get()
      .isEqualTo(AssetAmount.of("20000000000"));
    assertThat(vaultInfoAfterSet.vault().data()).isNotEmpty().get().isEqualTo(VaultData.of("576F726C64"));
  }

  @Test
  void vaultWithMptAsset() throws Exception {
    KeyPair mptIssuerKeyPair = createRandomAccountEd25519();
    KeyPair vaultOwnerKeyPair = createRandomAccountEd25519();
    KeyPair depositor1KeyPair = createRandomAccountEd25519();
    KeyPair depositor2KeyPair = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult mptIssuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(mptIssuerKeyPair.publicKey().deriveAddress())
    );

    MpTokenIssuanceCreate mpTokenIssuanceCreate = MpTokenIssuanceCreate.builder()
      .account(mptIssuerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(mptIssuerAccountInfo.accountData().sequence())
      .assetScale(AssetScale.of(UnsignedInteger.valueOf(2)))
      .maximumAmount(MpTokenNumericAmount.of(100000000L))
      .signingPublicKey(mptIssuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<MpTokenIssuanceCreate> signedMpTokenIssuanceCreate = signatureService.sign(
      mptIssuerKeyPair.privateKey(),
      mpTokenIssuanceCreate
    );
    SubmitResult<MpTokenIssuanceCreate> mpTokenIssuanceCreateSubmitResult = xrplClient.submit(
      signedMpTokenIssuanceCreate
    );
    assertThat(mpTokenIssuanceCreateSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    TransactionResult<MpTokenIssuanceCreate> validatedMpTokenIssuanceCreate = this.scanForResult(
      () -> this.getValidatedTransaction(signedMpTokenIssuanceCreate.hash(), MpTokenIssuanceCreate.class)
    );
    assertThat(validatedMpTokenIssuanceCreate.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);

    MpTokenIssuanceId mpTokenIssuanceId = MpTokenIssuanceId.of(
      signedMpTokenIssuanceCreate.hash().value().substring(0, 40)
    );

    AccountInfoResult vaultOwnerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(vaultOwnerKeyPair.publicKey().deriveAddress())
    );

    VaultCreate vaultCreate = VaultCreate.builder()
      .account(vaultOwnerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(vaultOwnerAccountInfo.accountData().sequence())
      .asset(
        MptIssue.builder()
          .mptIssuanceId(mpTokenIssuanceId)
          .build()
      )
      .assetsMaximum(AssetAmount.of("50000000"))
      .mpTokenMetadata(MpTokenMetadata.of("DEADBEEF"))
      .withdrawalPolicy(WithdrawalPolicy.FIRST_COME_FIRST_SERVE)
      .signingPublicKey(vaultOwnerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultCreate> signedVaultCreate = signatureService.sign(
      vaultOwnerKeyPair.privateKey(),
      vaultCreate
    );
    SubmitResult<VaultCreate> vaultCreateSubmitResult = xrplClient.submit(signedVaultCreate);
    assertThat(vaultCreateSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    TransactionResult<VaultCreate> validatedVaultCreate = this.scanForResult(
      () -> this.getValidatedTransaction(signedVaultCreate.hash(), VaultCreate.class)
    );
    assertThat(validatedVaultCreate.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);

    Hash256 vaultId = signedVaultCreate.hash();

    VaultInfoResult vaultInfoResult = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    VaultObject vaultObject = vaultInfoResult.vault();
    assertThat(vaultObject.owner()).isEqualTo(vaultOwnerKeyPair.publicKey().deriveAddress());
    assertThat(vaultObject.asset()).isEqualTo(
      MptIssue.builder()
        .mptIssuanceId(mpTokenIssuanceId)
        .build()
    );
    assertThat(vaultObject.assetsMaximum()).isNotEmpty().get().isEqualTo(AssetAmount.of("50000000"));

    LedgerEntryResult<VaultObject> vaultLedgerEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.vault(
        VaultLedgerEntryParams.builder()
          .owner(vaultOwnerKeyPair.publicKey().deriveAddress())
          .seq(vaultOwnerAccountInfo.accountData().sequence())
          .build(),
        LedgerSpecifier.of(vaultInfoResult.ledgerIndexSafe())
      )
    );
    assertThat(vaultLedgerEntry.node()).isEqualTo(vaultObject);

    AccountInfoResult depositor1AccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(depositor1KeyPair.publicKey().deriveAddress())
    );

    VaultDeposit deposit1 = VaultDeposit.builder()
      .account(depositor1KeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(depositor1AccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .amount(
        MptCurrencyAmount.builder()
          .mptIssuanceId(mpTokenIssuanceId)
          .value("10000")
          .build()
      )
      .signingPublicKey(depositor1KeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultDeposit> signedDeposit1 = signatureService.sign(
      depositor1KeyPair.privateKey(),
      deposit1
    );
    SubmitResult<VaultDeposit> deposit1SubmitResult = xrplClient.submit(signedDeposit1);
    assertThat(deposit1SubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    TransactionResult<VaultDeposit> validatedDeposit1 = this.scanForResult(
      () -> this.getValidatedTransaction(signedDeposit1.hash(), VaultDeposit.class)
    );
    assertThat(validatedDeposit1.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);

    VaultInfoResult vaultInfoAfterDeposit1 = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    assertThat(vaultInfoAfterDeposit1.vault().assetsTotal()).isNotEmpty().get().isEqualTo(AssetAmount.of("10000"));
    assertThat(vaultInfoAfterDeposit1.vault().assetsAvailable()).isNotEmpty().get()
      .isEqualTo(AssetAmount.of("10000"));

    AccountInfoResult depositor2AccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(depositor2KeyPair.publicKey().deriveAddress())
    );

    VaultDeposit deposit2 = VaultDeposit.builder()
      .account(depositor2KeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(depositor2AccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .amount(
        MptCurrencyAmount.builder()
          .mptIssuanceId(mpTokenIssuanceId)
          .value("20000")
          .build()
      )
      .signingPublicKey(depositor2KeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultDeposit> signedDeposit2 = signatureService.sign(
      depositor2KeyPair.privateKey(),
      deposit2
    );
    SubmitResult<VaultDeposit> deposit2SubmitResult = xrplClient.submit(signedDeposit2);
    assertThat(deposit2SubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    TransactionResult<VaultDeposit> validatedDeposit2 = this.scanForResult(
      () -> this.getValidatedTransaction(signedDeposit2.hash(), VaultDeposit.class)
    );
    assertThat(validatedDeposit2.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);

    VaultInfoResult vaultInfoAfterDeposit2 = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    assertThat(vaultInfoAfterDeposit2.vault().assetsTotal()).isNotEmpty().get().isEqualTo(AssetAmount.of("30000"));
    assertThat(vaultInfoAfterDeposit2.vault().assetsAvailable()).isNotEmpty().get()
      .isEqualTo(AssetAmount.of("30000"));

    depositor1AccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(depositor1KeyPair.publicKey().deriveAddress())
    );

    VaultWithdraw withdraw1 = VaultWithdraw.builder()
      .account(depositor1KeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(depositor1AccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .amount(
        MptCurrencyAmount.builder()
          .mptIssuanceId(mpTokenIssuanceId)
          .value("5000")
          .build()
      )
      .signingPublicKey(depositor1KeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultWithdraw> signedWithdraw1 = signatureService.sign(
      depositor1KeyPair.privateKey(),
      withdraw1
    );
    SubmitResult<VaultWithdraw> withdraw1SubmitResult = xrplClient.submit(signedWithdraw1);
    assertThat(withdraw1SubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    TransactionResult<VaultWithdraw> validatedWithdraw1 = this.scanForResult(
      () -> this.getValidatedTransaction(signedWithdraw1.hash(), VaultWithdraw.class)
    );
    assertThat(validatedWithdraw1.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);

    VaultInfoResult vaultInfoAfterWithdraw1 = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    assertThat(vaultInfoAfterWithdraw1.vault().assetsTotal()).isNotEmpty().get().isEqualTo(AssetAmount.of("25000"));
    assertThat(vaultInfoAfterWithdraw1.vault().assetsAvailable()).isNotEmpty().get()
      .isEqualTo(AssetAmount.of("25000"));

    depositor2AccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(depositor2KeyPair.publicKey().deriveAddress())
    );

    VaultWithdraw withdraw2 = VaultWithdraw.builder()
      .account(depositor2KeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(depositor2AccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .amount(
        MptCurrencyAmount.builder()
          .mptIssuanceId(mpTokenIssuanceId)
          .value("10000")
          .build()
      )
      .signingPublicKey(depositor2KeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultWithdraw> signedWithdraw2 = signatureService.sign(
      depositor2KeyPair.privateKey(),
      withdraw2
    );
    SubmitResult<VaultWithdraw> withdraw2SubmitResult = xrplClient.submit(signedWithdraw2);
    assertThat(withdraw2SubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    TransactionResult<VaultWithdraw> validatedWithdraw2 = this.scanForResult(
      () -> this.getValidatedTransaction(signedWithdraw2.hash(), VaultWithdraw.class)
    );
    assertThat(validatedWithdraw2.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);

    VaultInfoResult vaultInfoAfterWithdraw2 = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    assertThat(vaultInfoAfterWithdraw2.vault().assetsTotal()).isNotEmpty().get().isEqualTo(AssetAmount.of("15000"));
    assertThat(vaultInfoAfterWithdraw2.vault().assetsAvailable()).isNotEmpty().get()
      .isEqualTo(AssetAmount.of("15000"));
  }

  @Test
  void vaultWithIouAsset() throws Exception {
    KeyPair issuerKeyPair = createRandomAccountEd25519();
    KeyPair vaultOwnerKeyPair = createRandomAccountEd25519();
    KeyPair depositor1KeyPair = createRandomAccountEd25519();
    KeyPair depositor2KeyPair = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();

    IouIssue usdIssue = IouIssue.builder()
      .currency("USD")
      .issuer(issuerKeyPair.publicKey().deriveAddress())
      .build();

    AccountInfoResult vaultOwnerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(vaultOwnerKeyPair.publicKey().deriveAddress())
    );

    TrustSet vaultOwnerTrustSet = TrustSet.builder()
      .account(vaultOwnerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(vaultOwnerAccountInfo.accountData().sequence())
      .limitAmount(
        IssuedCurrencyAmount.builder()
          .currency("USD")
          .issuer(issuerKeyPair.publicKey().deriveAddress())
          .value("1000000")
          .build()
      )
      .signingPublicKey(vaultOwnerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<TrustSet> signedVaultOwnerTrustSet = signatureService.sign(
      vaultOwnerKeyPair.privateKey(),
      vaultOwnerTrustSet
    );
    SubmitResult<TrustSet> vaultOwnerTrustSetSubmitResult = xrplClient.submit(signedVaultOwnerTrustSet);
    assertThat(vaultOwnerTrustSetSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    TransactionResult<TrustSet> validatedVaultOwnerTrustSet = this.scanForResult(
      () -> this.getValidatedTransaction(signedVaultOwnerTrustSet.hash(), TrustSet.class)
    );
    assertThat(validatedVaultOwnerTrustSet.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);

    AccountInfoResult depositor1AccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(depositor1KeyPair.publicKey().deriveAddress())
    );

    TrustSet depositor1TrustSet = TrustSet.builder()
      .account(depositor1KeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(depositor1AccountInfo.accountData().sequence())
      .limitAmount(
        IssuedCurrencyAmount.builder()
          .currency("USD")
          .issuer(issuerKeyPair.publicKey().deriveAddress())
          .value("1000000")
          .build()
      )
      .signingPublicKey(depositor1KeyPair.publicKey())
      .build();

    SingleSignedTransaction<TrustSet> signedDepositor1TrustSet = signatureService.sign(
      depositor1KeyPair.privateKey(),
      depositor1TrustSet
    );
    SubmitResult<TrustSet> depositor1TrustSetSubmitResult = xrplClient.submit(signedDepositor1TrustSet);
    assertThat(depositor1TrustSetSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    TransactionResult<TrustSet> validatedDepositor1TrustSet = this.scanForResult(
      () -> this.getValidatedTransaction(signedDepositor1TrustSet.hash(), TrustSet.class)
    );
    assertThat(validatedDepositor1TrustSet.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);

    AccountInfoResult depositor2AccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(depositor2KeyPair.publicKey().deriveAddress())
    );

    TrustSet depositor2TrustSet = TrustSet.builder()
      .account(depositor2KeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(depositor2AccountInfo.accountData().sequence())
      .limitAmount(
        IssuedCurrencyAmount.builder()
          .currency("USD")
          .issuer(issuerKeyPair.publicKey().deriveAddress())
          .value("1000000")
          .build()
      )
      .signingPublicKey(depositor2KeyPair.publicKey())
      .build();

    SingleSignedTransaction<TrustSet> signedDepositor2TrustSet = signatureService.sign(
      depositor2KeyPair.privateKey(),
      depositor2TrustSet
    );
    SubmitResult<TrustSet> depositor2TrustSetSubmitResult = xrplClient.submit(signedDepositor2TrustSet);
    assertThat(depositor2TrustSetSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    TransactionResult<TrustSet> validatedDepositor2TrustSet = this.scanForResult(
      () -> this.getValidatedTransaction(signedDepositor2TrustSet.hash(), TrustSet.class)
    );
    assertThat(validatedDepositor2TrustSet.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);

    AccountInfoResult issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    Payment paymentToDepositor1 = Payment.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(issuerAccountInfo.accountData().sequence())
      .destination(depositor1KeyPair.publicKey().deriveAddress())
      .amount(
        IssuedCurrencyAmount.builder()
          .currency("USD")
          .issuer(issuerKeyPair.publicKey().deriveAddress())
          .value("50000")
          .build()
      )
      .signingPublicKey(issuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<Payment> signedPaymentToDepositor1 = signatureService.sign(
      issuerKeyPair.privateKey(),
      paymentToDepositor1
    );
    SubmitResult<Payment> paymentToDepositor1SubmitResult = xrplClient.submit(signedPaymentToDepositor1);
    assertThat(paymentToDepositor1SubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    TransactionResult<Payment> validatedPaymentToDepositor1 = this.scanForResult(
      () -> this.getValidatedTransaction(signedPaymentToDepositor1.hash(), Payment.class)
    );
    assertThat(validatedPaymentToDepositor1.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);

    issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    Payment paymentToDepositor2 = Payment.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(issuerAccountInfo.accountData().sequence())
      .destination(depositor2KeyPair.publicKey().deriveAddress())
      .amount(
        IssuedCurrencyAmount.builder()
          .currency("USD")
          .issuer(issuerKeyPair.publicKey().deriveAddress())
          .value("100000")
          .build()
      )
      .signingPublicKey(issuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<Payment> signedPaymentToDepositor2 = signatureService.sign(
      issuerKeyPair.privateKey(),
      paymentToDepositor2
    );
    SubmitResult<Payment> paymentToDepositor2SubmitResult = xrplClient.submit(signedPaymentToDepositor2);
    assertThat(paymentToDepositor2SubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    TransactionResult<Payment> validatedPaymentToDepositor2 = this.scanForResult(
      () -> this.getValidatedTransaction(signedPaymentToDepositor2.hash(), Payment.class)
    );
    assertThat(validatedPaymentToDepositor2.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);

    vaultOwnerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(vaultOwnerKeyPair.publicKey().deriveAddress())
    );

    VaultCreate vaultCreate = VaultCreate.builder()
      .account(vaultOwnerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(vaultOwnerAccountInfo.accountData().sequence())
      .asset(usdIssue)
      .assetsMaximum(AssetAmount.of("500000"))
      .mpTokenMetadata(MpTokenMetadata.of("CAFEBABE"))
      .withdrawalPolicy(WithdrawalPolicy.FIRST_COME_FIRST_SERVE)
      .scale(AssetScale.of(UnsignedInteger.valueOf(6)))
      .signingPublicKey(vaultOwnerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultCreate> signedVaultCreate = signatureService.sign(
      vaultOwnerKeyPair.privateKey(),
      vaultCreate
    );
    SubmitResult<VaultCreate> vaultCreateSubmitResult = xrplClient.submit(signedVaultCreate);
    assertThat(vaultCreateSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    TransactionResult<VaultCreate> validatedVaultCreate = this.scanForResult(
      () -> this.getValidatedTransaction(signedVaultCreate.hash(), VaultCreate.class)
    );
    assertThat(validatedVaultCreate.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);

    Hash256 vaultId = signedVaultCreate.hash();

    VaultInfoResult vaultInfoResult = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    VaultObject vaultObject = vaultInfoResult.vault();
    assertThat(vaultObject.owner()).isEqualTo(vaultOwnerKeyPair.publicKey().deriveAddress());
    assertThat(vaultObject.asset()).isEqualTo(usdIssue);
    assertThat(vaultObject.assetsMaximum()).isNotEmpty().get().isEqualTo(AssetAmount.of("500000"));
    assertThat(vaultObject.scale()).isNotEmpty().get().isEqualTo(AssetScale.of(UnsignedInteger.valueOf(6)));

    LedgerEntryResult<VaultObject> vaultLedgerEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.vault(
        VaultLedgerEntryParams.builder()
          .owner(vaultOwnerKeyPair.publicKey().deriveAddress())
          .seq(vaultOwnerAccountInfo.accountData().sequence())
          .build(),
        LedgerSpecifier.of(vaultInfoResult.ledgerIndexSafe())
      )
    );
    assertThat(vaultLedgerEntry.node()).isEqualTo(vaultObject);

    depositor1AccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(depositor1KeyPair.publicKey().deriveAddress())
    );

    VaultDeposit deposit1 = VaultDeposit.builder()
      .account(depositor1KeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(depositor1AccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .amount(
        IssuedCurrencyAmount.builder()
          .currency("USD")
          .issuer(issuerKeyPair.publicKey().deriveAddress())
          .value("10000")
          .build()
      )
      .signingPublicKey(depositor1KeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultDeposit> signedDeposit1 = signatureService.sign(
      depositor1KeyPair.privateKey(),
      deposit1
    );
    SubmitResult<VaultDeposit> deposit1SubmitResult = xrplClient.submit(signedDeposit1);
    assertThat(deposit1SubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    TransactionResult<VaultDeposit> validatedDeposit1 = this.scanForResult(
      () -> this.getValidatedTransaction(signedDeposit1.hash(), VaultDeposit.class)
    );
    assertThat(validatedDeposit1.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);

    VaultInfoResult vaultInfoAfterDeposit1 = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    assertThat(vaultInfoAfterDeposit1.vault().assetsTotal()).isNotEmpty().get().isEqualTo(AssetAmount.of("10000"));
    assertThat(vaultInfoAfterDeposit1.vault().assetsAvailable()).isNotEmpty().get()
      .isEqualTo(AssetAmount.of("10000"));

    depositor2AccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(depositor2KeyPair.publicKey().deriveAddress())
    );

    VaultDeposit deposit2 = VaultDeposit.builder()
      .account(depositor2KeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(depositor2AccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .amount(
        IssuedCurrencyAmount.builder()
          .currency("USD")
          .issuer(issuerKeyPair.publicKey().deriveAddress())
          .value("20000")
          .build()
      )
      .signingPublicKey(depositor2KeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultDeposit> signedDeposit2 = signatureService.sign(
      depositor2KeyPair.privateKey(),
      deposit2
    );
    SubmitResult<VaultDeposit> deposit2SubmitResult = xrplClient.submit(signedDeposit2);
    assertThat(deposit2SubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    TransactionResult<VaultDeposit> validatedDeposit2 = this.scanForResult(
      () -> this.getValidatedTransaction(signedDeposit2.hash(), VaultDeposit.class)
    );
    assertThat(validatedDeposit2.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);

    VaultInfoResult vaultInfoAfterDeposit2 = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    assertThat(vaultInfoAfterDeposit2.vault().assetsTotal()).isNotEmpty().get().isEqualTo(AssetAmount.of("30000"));
    assertThat(vaultInfoAfterDeposit2.vault().assetsAvailable()).isNotEmpty().get()
      .isEqualTo(AssetAmount.of("30000"));

    depositor1AccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(depositor1KeyPair.publicKey().deriveAddress())
    );

    VaultWithdraw withdraw1 = VaultWithdraw.builder()
      .account(depositor1KeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(depositor1AccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .amount(
        IssuedCurrencyAmount.builder()
          .currency("USD")
          .issuer(issuerKeyPair.publicKey().deriveAddress())
          .value("5000")
          .build()
      )
      .signingPublicKey(depositor1KeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultWithdraw> signedWithdraw1 = signatureService.sign(
      depositor1KeyPair.privateKey(),
      withdraw1
    );
    SubmitResult<VaultWithdraw> withdraw1SubmitResult = xrplClient.submit(signedWithdraw1);
    assertThat(withdraw1SubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    TransactionResult<VaultWithdraw> validatedWithdraw1 = this.scanForResult(
      () -> this.getValidatedTransaction(signedWithdraw1.hash(), VaultWithdraw.class)
    );
    assertThat(validatedWithdraw1.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);

    VaultInfoResult vaultInfoAfterWithdraw1 = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    assertThat(vaultInfoAfterWithdraw1.vault().assetsTotal()).isNotEmpty().get().isEqualTo(AssetAmount.of("25000"));
    assertThat(vaultInfoAfterWithdraw1.vault().assetsAvailable()).isNotEmpty().get()
      .isEqualTo(AssetAmount.of("25000"));

    depositor2AccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(depositor2KeyPair.publicKey().deriveAddress())
    );

    VaultWithdraw withdraw2 = VaultWithdraw.builder()
      .account(depositor2KeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(depositor2AccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .amount(
        IssuedCurrencyAmount.builder()
          .currency("USD")
          .issuer(issuerKeyPair.publicKey().deriveAddress())
          .value("10000")
          .build()
      )
      .signingPublicKey(depositor2KeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultWithdraw> signedWithdraw2 = signatureService.sign(
      depositor2KeyPair.privateKey(),
      withdraw2
    );
    SubmitResult<VaultWithdraw> withdraw2SubmitResult = xrplClient.submit(signedWithdraw2);
    assertThat(withdraw2SubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    TransactionResult<VaultWithdraw> validatedWithdraw2 = this.scanForResult(
      () -> this.getValidatedTransaction(signedWithdraw2.hash(), VaultWithdraw.class)
    );
    assertThat(validatedWithdraw2.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);

    VaultInfoResult vaultInfoAfterWithdraw2 = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    assertThat(vaultInfoAfterWithdraw2.vault().assetsTotal()).isNotEmpty().get().isEqualTo(AssetAmount.of("15000"));
    assertThat(vaultInfoAfterWithdraw2.vault().assetsAvailable()).isNotEmpty().get()
      .isEqualTo(AssetAmount.of("15000"));
  }

  @Test
  void vaultWithXrpAssetAndDomainId() throws Exception {
    KeyPair domainOwnerKeyPair = createRandomAccountEd25519();
    KeyPair vaultOwnerKeyPair = createRandomAccountEd25519();
    KeyPair authorizedDepositorKeyPair = createRandomAccountEd25519();
    KeyPair unauthorizedDepositorKeyPair = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult domainOwnerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(domainOwnerKeyPair.publicKey().deriveAddress())
    );

    PermissionedDomainSet permissionedDomainSet = PermissionedDomainSet.builder()
      .account(domainOwnerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(domainOwnerAccountInfo.accountData().sequence())
      .signingPublicKey(domainOwnerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<PermissionedDomainSet> signedPermissionedDomainSet = signatureService.sign(
      domainOwnerKeyPair.privateKey(),
      permissionedDomainSet
    );
    SubmitResult<PermissionedDomainSet> permissionedDomainSetSubmitResult = xrplClient.submit(
      signedPermissionedDomainSet
    );
    assertThat(permissionedDomainSetSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    TransactionResult<PermissionedDomainSet> validatedPermissionedDomainSet = this.scanForResult(
      () -> this.getValidatedTransaction(signedPermissionedDomainSet.hash(), PermissionedDomainSet.class)
    );
    assertThat(validatedPermissionedDomainSet.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);

    Hash256 domainId = signedPermissionedDomainSet.hash();

    domainOwnerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(domainOwnerKeyPair.publicKey().deriveAddress())
    );

    CredentialCreate credentialCreate = CredentialCreate.builder()
      .account(domainOwnerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(domainOwnerAccountInfo.accountData().sequence())
      .subject(authorizedDepositorKeyPair.publicKey().deriveAddress())
      .credentialType(CredentialType.of("credential"))
      .signingPublicKey(domainOwnerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<CredentialCreate> signedCredentialCreate = signatureService.sign(
      domainOwnerKeyPair.privateKey(),
      credentialCreate
    );
    SubmitResult<CredentialCreate> credentialCreateSubmitResult = xrplClient.submit(signedCredentialCreate);
    assertThat(credentialCreateSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    TransactionResult<CredentialCreate> validatedCredentialCreate = this.scanForResult(
      () -> this.getValidatedTransaction(signedCredentialCreate.hash(), CredentialCreate.class)
    );
    assertThat(validatedCredentialCreate.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);

    AccountInfoResult authorizedDepositorAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(authorizedDepositorKeyPair.publicKey().deriveAddress())
    );

    CredentialAccept credentialAccept = CredentialAccept.builder()
      .account(authorizedDepositorKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(authorizedDepositorAccountInfo.accountData().sequence())
      .issuer(domainOwnerKeyPair.publicKey().deriveAddress())
      .credentialType(CredentialType.of("credential"))
      .signingPublicKey(authorizedDepositorKeyPair.publicKey())
      .build();

    SingleSignedTransaction<CredentialAccept> signedCredentialAccept = signatureService.sign(
      authorizedDepositorKeyPair.privateKey(),
      credentialAccept
    );
    SubmitResult<CredentialAccept> credentialAcceptSubmitResult = xrplClient.submit(signedCredentialAccept);
    assertThat(credentialAcceptSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    TransactionResult<CredentialAccept> validatedCredentialAccept = this.scanForResult(
      () -> this.getValidatedTransaction(signedCredentialAccept.hash(), CredentialAccept.class)
    );
    assertThat(validatedCredentialAccept.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);

    AccountInfoResult vaultOwnerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(vaultOwnerKeyPair.publicKey().deriveAddress())
    );

    VaultCreate vaultCreate = VaultCreate.builder()
      .account(vaultOwnerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(vaultOwnerAccountInfo.accountData().sequence())
      .asset(Issue.XRP)
      .assetsMaximum(AssetAmount.of("10000000000"))
      .mpTokenMetadata(MpTokenMetadata.of("AABBCCDD"))
      .withdrawalPolicy(WithdrawalPolicy.FIRST_COME_FIRST_SERVE)
      .domainId(domainId)
      .flags(VaultCreateFlags.VAULT_PRIVATE)
      .signingPublicKey(vaultOwnerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultCreate> signedVaultCreate = signatureService.sign(
      vaultOwnerKeyPair.privateKey(),
      vaultCreate
    );
    SubmitResult<VaultCreate> vaultCreateSubmitResult = xrplClient.submit(signedVaultCreate);
    assertThat(vaultCreateSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    TransactionResult<VaultCreate> validatedVaultCreate = this.scanForResult(
      () -> this.getValidatedTransaction(signedVaultCreate.hash(), VaultCreate.class)
    );
    assertThat(validatedVaultCreate.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);

    Hash256 vaultId = signedVaultCreate.hash();

    VaultInfoResult vaultInfoResult = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    VaultObject vaultObject = vaultInfoResult.vault();
    assertThat(vaultObject.owner()).isEqualTo(vaultOwnerKeyPair.publicKey().deriveAddress());
    assertThat(vaultObject.asset()).isEqualTo(Issue.XRP);

    authorizedDepositorAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(authorizedDepositorKeyPair.publicKey().deriveAddress())
    );

    VaultDeposit authorizedDeposit1 = VaultDeposit.builder()
      .account(authorizedDepositorKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(authorizedDepositorAccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .amount(XrpCurrencyAmount.ofDrops(1000000))
      .signingPublicKey(authorizedDepositorKeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultDeposit> signedAuthorizedDeposit1 = signatureService.sign(
      authorizedDepositorKeyPair.privateKey(),
      authorizedDeposit1
    );
    SubmitResult<VaultDeposit> authorizedDeposit1SubmitResult = xrplClient.submit(signedAuthorizedDeposit1);
    assertThat(authorizedDeposit1SubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    TransactionResult<VaultDeposit> validatedAuthorizedDeposit1 = this.scanForResult(
      () -> this.getValidatedTransaction(signedAuthorizedDeposit1.hash(), VaultDeposit.class)
    );
    assertThat(validatedAuthorizedDeposit1.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);

    VaultInfoResult vaultInfoAfterAuthorizedDeposit1 = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    assertThat(vaultInfoAfterAuthorizedDeposit1.vault().assetsTotal()).isNotEmpty().get()
      .isEqualTo(AssetAmount.of("1000000"));
    assertThat(vaultInfoAfterAuthorizedDeposit1.vault().assetsAvailable()).isNotEmpty().get()
      .isEqualTo(AssetAmount.of("1000000"));

    authorizedDepositorAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(authorizedDepositorKeyPair.publicKey().deriveAddress())
    );

    VaultDeposit authorizedDeposit2 = VaultDeposit.builder()
      .account(authorizedDepositorKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(authorizedDepositorAccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .amount(XrpCurrencyAmount.ofDrops(2000000))
      .signingPublicKey(authorizedDepositorKeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultDeposit> signedAuthorizedDeposit2 = signatureService.sign(
      authorizedDepositorKeyPair.privateKey(),
      authorizedDeposit2
    );
    SubmitResult<VaultDeposit> authorizedDeposit2SubmitResult = xrplClient.submit(signedAuthorizedDeposit2);
    assertThat(authorizedDeposit2SubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    TransactionResult<VaultDeposit> validatedAuthorizedDeposit2 = this.scanForResult(
      () -> this.getValidatedTransaction(signedAuthorizedDeposit2.hash(), VaultDeposit.class)
    );
    assertThat(validatedAuthorizedDeposit2.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);

    VaultInfoResult vaultInfoAfterAuthorizedDeposit2 = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    assertThat(vaultInfoAfterAuthorizedDeposit2.vault().assetsTotal()).isNotEmpty().get()
      .isEqualTo(AssetAmount.of("3000000"));
    assertThat(vaultInfoAfterAuthorizedDeposit2.vault().assetsAvailable()).isNotEmpty().get()
      .isEqualTo(AssetAmount.of("3000000"));

    authorizedDepositorAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(authorizedDepositorKeyPair.publicKey().deriveAddress())
    );

    VaultWithdraw authorizedWithdraw1 = VaultWithdraw.builder()
      .account(authorizedDepositorKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(authorizedDepositorAccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .amount(XrpCurrencyAmount.ofDrops(500000))
      .signingPublicKey(authorizedDepositorKeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultWithdraw> signedAuthorizedWithdraw1 = signatureService.sign(
      authorizedDepositorKeyPair.privateKey(),
      authorizedWithdraw1
    );
    SubmitResult<VaultWithdraw> authorizedWithdraw1SubmitResult = xrplClient.submit(signedAuthorizedWithdraw1);
    assertThat(authorizedWithdraw1SubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    TransactionResult<VaultWithdraw> validatedAuthorizedWithdraw1 = this.scanForResult(
      () -> this.getValidatedTransaction(signedAuthorizedWithdraw1.hash(), VaultWithdraw.class)
    );
    assertThat(validatedAuthorizedWithdraw1.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);

    VaultInfoResult vaultInfoAfterAuthorizedWithdraw1 = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    assertThat(vaultInfoAfterAuthorizedWithdraw1.vault().assetsTotal()).isNotEmpty().get()
      .isEqualTo(AssetAmount.of("2500000"));
    assertThat(vaultInfoAfterAuthorizedWithdraw1.vault().assetsAvailable()).isNotEmpty().get()
      .isEqualTo(AssetAmount.of("2500000"));

    authorizedDepositorAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(authorizedDepositorKeyPair.publicKey().deriveAddress())
    );

    VaultWithdraw authorizedWithdraw2 = VaultWithdraw.builder()
      .account(authorizedDepositorKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(authorizedDepositorAccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .amount(XrpCurrencyAmount.ofDrops(1000000))
      .signingPublicKey(authorizedDepositorKeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultWithdraw> signedAuthorizedWithdraw2 = signatureService.sign(
      authorizedDepositorKeyPair.privateKey(),
      authorizedWithdraw2
    );
    SubmitResult<VaultWithdraw> authorizedWithdraw2SubmitResult = xrplClient.submit(signedAuthorizedWithdraw2);
    assertThat(authorizedWithdraw2SubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    TransactionResult<VaultWithdraw> validatedAuthorizedWithdraw2 = this.scanForResult(
      () -> this.getValidatedTransaction(signedAuthorizedWithdraw2.hash(), VaultWithdraw.class)
    );
    assertThat(validatedAuthorizedWithdraw2.metadata().get().transactionResult()).isEqualTo(SUCCESS_STATUS);

    VaultInfoResult vaultInfoAfterAuthorizedWithdraw2 = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    assertThat(vaultInfoAfterAuthorizedWithdraw2.vault().assetsTotal()).isNotEmpty().get()
      .isEqualTo(AssetAmount.of("1500000"));
    assertThat(vaultInfoAfterAuthorizedWithdraw2.vault().assetsAvailable()).isNotEmpty().get()
      .isEqualTo(AssetAmount.of("1500000"));

    AccountInfoResult unauthorizedDepositorAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(unauthorizedDepositorKeyPair.publicKey().deriveAddress())
    );

    VaultDeposit unauthorizedDeposit = VaultDeposit.builder()
      .account(unauthorizedDepositorKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(unauthorizedDepositorAccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .amount(XrpCurrencyAmount.ofDrops(1000000))
      .signingPublicKey(unauthorizedDepositorKeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultDeposit> signedUnauthorizedDeposit = signatureService.sign(
      unauthorizedDepositorKeyPair.privateKey(),
      unauthorizedDeposit
    );
    SubmitResult<VaultDeposit> unauthorizedDepositSubmitResult = xrplClient.submit(signedUnauthorizedDeposit);
    assertThat(unauthorizedDepositSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    TransactionResult<VaultDeposit> validatedUnauthorizedDeposit = this.scanForResult(
      () -> this.getValidatedTransaction(signedUnauthorizedDeposit.hash(), VaultDeposit.class)
    );
    assertThat(validatedUnauthorizedDeposit.metadata().get().transactionResult()).isEqualTo("tecNO_PERMISSION");
  }
}

