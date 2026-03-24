package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsRequestParams.AccountObjectType;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerEntryResult;
import org.xrpl.xrpl4j.model.client.ledger.VaultLedgerEntryParams;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.client.vault.VaultInfoRequestParams;
import org.xrpl.xrpl4j.model.client.vault.VaultInfoResult;
import org.xrpl.xrpl4j.model.flags.MpTokenIssuanceCreateFlags;
import org.xrpl.xrpl4j.model.flags.VaultCreateFlags;
import org.xrpl.xrpl4j.model.flags.VaultFlags;
import org.xrpl.xrpl4j.model.ledger.IouIssue;
import org.xrpl.xrpl4j.model.ledger.Issue;
import org.xrpl.xrpl4j.model.ledger.LedgerObject;
import org.xrpl.xrpl4j.model.ledger.MptIssue;
import org.xrpl.xrpl4j.model.ledger.PermissionedDomainObject;
import org.xrpl.xrpl4j.model.ledger.VaultObject;
import org.xrpl.xrpl4j.model.transactions.AccountSet;
import org.xrpl.xrpl4j.model.transactions.AccountSet.AccountSetFlag;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.AssetAmount;
import org.xrpl.xrpl4j.model.transactions.AssetScale;
import org.xrpl.xrpl4j.model.transactions.CredentialType;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.MpTokenAuthorize;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceCreate;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.MpTokenMetadata;
import org.xrpl.xrpl4j.model.transactions.MpTokenNumericAmount;
import org.xrpl.xrpl4j.model.transactions.MptCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.VaultClawback;
import org.xrpl.xrpl4j.model.transactions.VaultCreate;
import org.xrpl.xrpl4j.model.transactions.VaultData;
import org.xrpl.xrpl4j.model.transactions.VaultDelete;
import org.xrpl.xrpl4j.model.transactions.VaultDeposit;
import org.xrpl.xrpl4j.model.transactions.VaultSet;
import org.xrpl.xrpl4j.model.transactions.VaultWithdraw;
import org.xrpl.xrpl4j.model.transactions.WithdrawalPolicy;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

/**
 * Integration tests for Single Asset Vault transactions and ledger objects.
 *
 * <p>These tests exercise all vault transactions (VaultCreate, VaultSet, VaultDelete, VaultDeposit, VaultWithdraw)
 * for different asset types (IOU, MPT, XRP) and verify vault state through multiple query methods
 * (vault_info RPC, ledger_entry, account_objects).</p>
 */
public class SingleAssetVaultIT extends AbstractIT {

  /**
   * Test vault with IOU as asset. Exercises all vault transactions and verifies vault state through
   * vault_info, ledger_entry, and account_objects.
   */
  @Test
  void vaultWithIouAsset() throws Exception {
    // Step 1: Create accounts
    KeyPair issuerKeyPair = createRandomAccountEd25519();
    final KeyPair vaultOwnerKeyPair = createRandomAccountEd25519();
    final KeyPair depositorKeyPair = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();

    // Step 2: Enable clawback on issuer account
    AccountInfoResult issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    AccountSet enableClawback = AccountSet.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(issuerAccountInfo.accountData().sequence())
      .setFlag(AccountSetFlag.ALLOW_TRUSTLINE_CLAWBACK)
      .signingPublicKey(issuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<AccountSet> signedEnableClawback = signatureService.sign(
      issuerKeyPair.privateKey(),
      enableClawback
    );
    SubmitResult<AccountSet> enableClawbackResult = xrplClient.submit(signedEnableClawback);
    assertThat(enableClawbackResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    this.scanForResult(
      () -> this.getValidatedTransaction(signedEnableClawback.hash(), AccountSet.class)
    );

    // Enable default ripple on issuer account
    setDefaultRipple(issuerKeyPair, feeResult);

    // Step 3: Define IOU asset
    IouIssue usdIssue = IouIssue.builder()
      .currency("USD")
      .issuer(issuerKeyPair.publicKey().deriveAddress())
      .build();

    // Step 4: Set up trust line
    createTrustLine(
      depositorKeyPair,
      IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(issuerKeyPair.publicKey().deriveAddress())
        .value("1000000")
        .build(),
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    // Step 5: Fund depositor with IOU
    sendIssuedCurrency(
      issuerKeyPair,
      depositorKeyPair,
      IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(issuerKeyPair.publicKey().deriveAddress())
        .value("50000")
        .build(),
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    // Step 6: Create vault with all optional fields including scale of 2
    AccountInfoResult vaultOwnerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(vaultOwnerKeyPair.publicKey().deriveAddress())
    );

    VaultCreate vaultCreate = VaultCreate.builder()
      .account(vaultOwnerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(vaultOwnerAccountInfo.accountData().sequence())
      .asset(usdIssue)
      .assetsMaximum(AssetAmount.of("500000"))
      .mpTokenMetadata(MpTokenMetadata.of("AABB"))
      .withdrawalPolicy(WithdrawalPolicy.FIRST_COME_FIRST_SERVE)
      .scale(AssetScale.of(UnsignedInteger.valueOf(2)))
      .data(VaultData.ofPlainText("vault data"))
      .signingPublicKey(vaultOwnerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultCreate> signedVaultCreate = signatureService.sign(
      vaultOwnerKeyPair.privateKey(),
      vaultCreate
    );
    SubmitResult<VaultCreate> vaultCreateSubmitResult = xrplClient.submit(signedVaultCreate);
    assertThat(vaultCreateSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    // Wait for transaction to be validated
    this.scanForResult(
      () -> this.getValidatedTransaction(signedVaultCreate.hash(), VaultCreate.class)
    );

    // Get vault ID from ledger entry
    LedgerEntryResult<VaultObject> vaultLedgerEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.vault(
        VaultLedgerEntryParams.builder()
          .owner(vaultOwnerKeyPair.publicKey().deriveAddress())
          .seq(vaultCreate.sequence())
          .build(),
        LedgerSpecifier.VALIDATED
      )
    );
    Hash256 vaultId = vaultLedgerEntry.node().index();

    // Step 7: Verify vault creation via vault_info (query by vaultId)
    VaultInfoResult vaultInfoByIdResult = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    VaultObject vaultFromInfoById = vaultInfoByIdResult.vault();
    assertThat(vaultFromInfoById.owner()).isEqualTo(vaultOwnerKeyPair.publicKey().deriveAddress());
    assertThat(vaultFromInfoById.asset()).isEqualTo(usdIssue);
    assertThat(vaultFromInfoById.assetsMaximum()).isEqualTo(AssetAmount.of("500000"));
    assertThat(vaultFromInfoById.scale()).isEqualTo(AssetScale.of(UnsignedInteger.valueOf(2)));
    assertThat(vaultFromInfoById.data()).isNotEmpty().get().isEqualTo(VaultData.ofPlainText("vault data"));
    assertThat(vaultFromInfoById.withdrawalPolicy()).isEqualTo(WithdrawalPolicy.FIRST_COME_FIRST_SERVE);
    assertThat(vaultFromInfoById.assetsTotal()).isEqualTo(AssetAmount.of("0"));
    assertThat(vaultFromInfoById.assetsAvailable()).isEqualTo(AssetAmount.of("0"));
    assertThat(vaultFromInfoById.shares()).isNotEmpty();

    // Step 8: Verify vault_info (query by owner and seq)
    VaultInfoResult vaultInfoByOwnerSeqResult = xrplClient.vaultInfo(
      VaultInfoRequestParams.of(vaultOwnerKeyPair.publicKey().deriveAddress(), vaultCreate.sequence())
    );
    assertThat(vaultInfoByOwnerSeqResult.vault()).isEqualTo(vaultFromInfoById);

    // Step 9: Verify ledger_entry and account_objects
    assertVaultEntryEqualsObjectFromAccountObjects(
      vaultOwnerKeyPair.publicKey().deriveAddress(),
      vaultCreate.sequence()
    );

    // Step 10: Deposit into vault
    AccountInfoResult depositorAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(depositorKeyPair.publicKey().deriveAddress())
    );

    VaultDeposit deposit = VaultDeposit.builder()
      .account(depositorKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(depositorAccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .amount(
        IssuedCurrencyAmount.builder()
          .currency("USD")
          .issuer(issuerKeyPair.publicKey().deriveAddress())
          .value("10000")
          .build()
      )
      .signingPublicKey(depositorKeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultDeposit> signedDeposit = signatureService.sign(
      depositorKeyPair.privateKey(),
      deposit
    );
    SubmitResult<VaultDeposit> depositSubmitResult = xrplClient.submit(signedDeposit);
    assertThat(depositSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    // Wait for transaction to be validated
    this.scanForResult(
      () -> this.getValidatedTransaction(signedDeposit.hash(), VaultDeposit.class)
    );

    // Step 10: Verify vault state after deposit via vault_info
    VaultInfoResult vaultInfoAfterDeposit = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    assertThat(vaultInfoAfterDeposit.vault().assetsTotal()).isEqualTo(AssetAmount.of("10000"));
    assertThat(vaultInfoAfterDeposit.vault().assetsAvailable()).isEqualTo(AssetAmount.of("10000"));
    // scale of 2, so 10000 * 100 = 1000000
    assertThat(vaultInfoAfterDeposit.vault().shares().get().outstandingAmount())
      .isEqualTo(MpTokenNumericAmount.of(1000000));

    // Step 11: Withdraw from vault
    depositorAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(depositorKeyPair.publicKey().deriveAddress())
    );

    VaultWithdraw withdraw = VaultWithdraw.builder()
      .account(depositorKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(depositorAccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .amount(
        IssuedCurrencyAmount.builder()
          .currency("USD")
          .issuer(issuerKeyPair.publicKey().deriveAddress())
          .value("4000")
          .build()
      )
      .destination(depositorKeyPair.publicKey().deriveAddress())
      .signingPublicKey(depositorKeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultWithdraw> signedWithdraw = signatureService.sign(
      depositorKeyPair.privateKey(),
      withdraw
    );
    SubmitResult<VaultWithdraw> withdrawSubmitResult = xrplClient.submit(signedWithdraw);
    assertThat(withdrawSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    // Wait for transaction to be validated
    this.scanForResult(
      () -> this.getValidatedTransaction(signedWithdraw.hash(), VaultWithdraw.class)
    );

    // Step 12: Verify vault state after withdrawal via vault_info
    VaultInfoResult vaultInfoAfterWithdraw = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    assertThat(vaultInfoAfterWithdraw.vault().assetsTotal()).isEqualTo(AssetAmount.of("6000"));
    assertThat(vaultInfoAfterWithdraw.vault().assetsAvailable()).isEqualTo(AssetAmount.of("6000"));

    // Step 13: Clawback from vault (issuer claws back the underlying IOU asset from depositor)
    AccountInfoResult issuerAccountInfoForClawback = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    VaultClawback clawback = VaultClawback.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(issuerAccountInfoForClawback.accountData().sequence())
      .vaultId(vaultId)
      .holder(depositorKeyPair.publicKey().deriveAddress())
      .amount(
        IssuedCurrencyAmount.builder()
          .currency("USD")
          .issuer(issuerKeyPair.publicKey().deriveAddress())
          .value("2000")
          .build()
      )
      .signingPublicKey(issuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultClawback> signedClawback = signatureService.sign(
      issuerKeyPair.privateKey(),
      clawback
    );
    SubmitResult<VaultClawback> clawbackSubmitResult = xrplClient.submit(signedClawback);
    assertThat(clawbackSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    this.scanForResult(
      () -> this.getValidatedTransaction(signedClawback.hash(), VaultClawback.class)
    );

    // Verify vault state after clawback
    VaultInfoResult vaultInfoAfterClawback = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    assertThat(vaultInfoAfterClawback.vault().assetsTotal()).isEqualTo(AssetAmount.of("4000"));
    assertThat(vaultInfoAfterClawback.vault().assetsAvailable()).isEqualTo(AssetAmount.of("4000"));

    // Step 14: Update vault settings with VaultSet
    vaultOwnerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(vaultOwnerKeyPair.publicKey().deriveAddress())
    );

    VaultSet vaultSet = VaultSet.builder()
      .account(vaultOwnerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(vaultOwnerAccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .assetsMaximum(AssetAmount.of("1000000"))
      .data(VaultData.of("576F726C64"))
      .signingPublicKey(vaultOwnerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultSet> signedVaultSet = signatureService.sign(
      vaultOwnerKeyPair.privateKey(),
      vaultSet
    );
    SubmitResult<VaultSet> vaultSetSubmitResult = xrplClient.submit(signedVaultSet);
    assertThat(vaultSetSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    // Wait for transaction to be validated
    this.scanForResult(
      () -> this.getValidatedTransaction(signedVaultSet.hash(), VaultSet.class)
    );

    // Step 15: Verify vault state after set (assetsMaximum and data updated)
    VaultInfoResult vaultInfoAfterSet = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    assertThat(vaultInfoAfterSet.vault().assetsMaximum()).isEqualTo(AssetAmount.of("1000000"));
    assertThat(vaultInfoAfterSet.vault().data()).isNotEmpty().get().isEqualTo(VaultData.of("576F726C64"));
    assertThat(vaultInfoAfterSet.vault().assetsTotal()).isEqualTo(AssetAmount.of("4000"));

    // Step 16: Withdraw remaining assets before delete
    depositorAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(depositorKeyPair.publicKey().deriveAddress())
    );

    VaultWithdraw withdrawAll = VaultWithdraw.builder()
      .account(depositorKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(depositorAccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .amount(
        IssuedCurrencyAmount.builder()
          .currency("USD")
          .issuer(issuerKeyPair.publicKey().deriveAddress())
          .value("4000")
          .build()
      )
      .signingPublicKey(depositorKeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultWithdraw> signedWithdrawAll = signatureService.sign(
      depositorKeyPair.privateKey(),
      withdrawAll
    );
    xrplClient.submit(signedWithdrawAll);

    // Wait for transaction to be validated
    this.scanForResult(
      () -> this.getValidatedTransaction(signedWithdrawAll.hash(), VaultWithdraw.class)
    );

    // Step 17: Delete vault
    vaultOwnerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(vaultOwnerKeyPair.publicKey().deriveAddress())
    );

    VaultDelete vaultDelete = VaultDelete.builder()
      .account(vaultOwnerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(vaultOwnerAccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .signingPublicKey(vaultOwnerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultDelete> signedVaultDelete = signatureService.sign(
      vaultOwnerKeyPair.privateKey(),
      vaultDelete
    );
    SubmitResult<VaultDelete> vaultDeleteSubmitResult = xrplClient.submit(signedVaultDelete);
    assertThat(vaultDeleteSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    // Wait for transaction to be validated
    this.scanForResult(
      () -> this.getValidatedTransaction(signedVaultDelete.hash(), VaultDelete.class)
    );
  }

  /**
   * Test vault with MPT as asset. Exercises all vault transactions and verifies vault state through
   * vault_info, ledger_entry, and account_objects.
   */
  @Test
  void vaultWithMptAsset() throws Exception {
    // Step 1: Create accounts
    KeyPair mptIssuerKeyPair = createRandomAccountEd25519();
    KeyPair vaultOwnerKeyPair = createRandomAccountEd25519();
    final KeyPair depositorKeyPair = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();

    // Step 2: Create MPT issuance
    AccountInfoResult mptIssuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(mptIssuerKeyPair.publicKey().deriveAddress())
    );

    MpTokenIssuanceCreate mpTokenIssuanceCreate = MpTokenIssuanceCreate.builder()
      .account(mptIssuerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(mptIssuerAccountInfo.accountData().sequence())
      .assetScale(AssetScale.of(UnsignedInteger.valueOf(2)))
      .maximumAmount(MpTokenNumericAmount.of(100000000L))
      .flags(MpTokenIssuanceCreateFlags.builder().tfMptCanClawback(true).tfMptCanTransfer(true).build())
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

    // Wait for transaction to be validated and get MPT issuance ID from metadata
    TransactionResult<MpTokenIssuanceCreate> validatedIssuanceCreate = this.scanForResult(
      () -> this.getValidatedTransaction(signedMpTokenIssuanceCreate.hash(), MpTokenIssuanceCreate.class)
    );

    MpTokenIssuanceId mpTokenIssuanceId = validatedIssuanceCreate.metadata()
      .orElseThrow(RuntimeException::new)
      .mpTokenIssuanceId()
      .orElseThrow(() -> new RuntimeException("issuance create metadata did not contain issuance ID"));

    MptIssue mptIssue = MptIssue.builder()
      .mptIssuanceId(mpTokenIssuanceId)
      .build();

    // Step 3: Create vault with all optional fields
    AccountInfoResult vaultOwnerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(vaultOwnerKeyPair.publicKey().deriveAddress())
    );

    VaultCreate vaultCreate = VaultCreate.builder()
      .account(vaultOwnerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(vaultOwnerAccountInfo.accountData().sequence())
      .asset(mptIssue)
      .assetsMaximum(AssetAmount.of("50000000"))
      .mpTokenMetadata(MpTokenMetadata.of("DEADBEEF"))
      .withdrawalPolicy(WithdrawalPolicy.FIRST_COME_FIRST_SERVE)
      .data(VaultData.of("4D5054"))
      .signingPublicKey(vaultOwnerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultCreate> signedVaultCreate = signatureService.sign(
      vaultOwnerKeyPair.privateKey(),
      vaultCreate
    );
    SubmitResult<VaultCreate> vaultCreateSubmitResult = xrplClient.submit(signedVaultCreate);
    assertThat(vaultCreateSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    // Wait for transaction to be validated
    this.scanForResult(
      () -> this.getValidatedTransaction(signedVaultCreate.hash(), VaultCreate.class)
    );

    // Get vault ID from ledger entry
    LedgerEntryResult<VaultObject> vaultLedgerEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.vault(
        VaultLedgerEntryParams.builder()
          .owner(vaultOwnerKeyPair.publicKey().deriveAddress())
          .seq(vaultOwnerAccountInfo.accountData().sequence())
          .build(),
        LedgerSpecifier.VALIDATED
      )
    );
    Hash256 vaultId = vaultLedgerEntry.node().index();

    // Step 4: Verify vault creation via vault_info (query by vaultId)
    VaultInfoResult vaultInfoByIdResult = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    VaultObject vaultFromInfoById = vaultInfoByIdResult.vault();
    assertThat(vaultFromInfoById.owner()).isEqualTo(vaultOwnerKeyPair.publicKey().deriveAddress());
    assertThat(vaultFromInfoById.asset()).isEqualTo(mptIssue);
    assertThat(vaultFromInfoById.assetsMaximum()).isEqualTo(AssetAmount.of("50000000"));
    assertThat(vaultFromInfoById.scale()).isEqualTo(AssetScale.of(UnsignedInteger.ZERO));
    assertThat(vaultFromInfoById.data()).isNotEmpty().get().isEqualTo(VaultData.of("4D5054"));
    assertThat(vaultFromInfoById.withdrawalPolicy()).isEqualTo(WithdrawalPolicy.FIRST_COME_FIRST_SERVE);
    assertThat(vaultFromInfoById.assetsTotal()).isEqualTo(AssetAmount.of("0"));
    assertThat(vaultFromInfoById.assetsAvailable()).isEqualTo(AssetAmount.of("0"));
    assertThat(vaultFromInfoById.shares()).isNotEmpty();

    // Verify vault_info (query by owner and seq)
    VaultInfoResult vaultInfoByOwnerSeqResult = xrplClient.vaultInfo(
      VaultInfoRequestParams.of(
        vaultOwnerKeyPair.publicKey().deriveAddress(),
        vaultOwnerAccountInfo.accountData().sequence()
      )
    );
    assertThat(vaultInfoByOwnerSeqResult.vault()).isEqualTo(vaultFromInfoById);

    // Verify ledger_entry and account_objects
    assertVaultEntryEqualsObjectFromAccountObjects(
      vaultOwnerKeyPair.publicKey().deriveAddress(),
      vaultOwnerAccountInfo.accountData().sequence()
    );

    // Step 5: Depositor authorizes themselves to hold MPT
    AccountInfoResult depositorAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(depositorKeyPair.publicKey().deriveAddress())
    );

    MpTokenAuthorize mpTokenAuthorize = MpTokenAuthorize.builder()
      .account(depositorKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(depositorAccountInfo.accountData().sequence())
      .mpTokenIssuanceId(mpTokenIssuanceId)
      .signingPublicKey(depositorKeyPair.publicKey())
      .build();

    SingleSignedTransaction<MpTokenAuthorize> signedMpTokenAuthorize = signatureService.sign(
      depositorKeyPair.privateKey(),
      mpTokenAuthorize
    );
    SubmitResult<MpTokenAuthorize> mpTokenAuthorizeSubmitResult = xrplClient.submit(signedMpTokenAuthorize);
    assertThat(mpTokenAuthorizeSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    this.scanForResult(
      () -> this.getValidatedTransaction(signedMpTokenAuthorize.hash(), MpTokenAuthorize.class)
    );

    // Step 6: Issuer sends MPT to depositor
    mptIssuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(mptIssuerKeyPair.publicKey().deriveAddress())
    );

    Payment mptPayment = Payment.builder()
      .account(mptIssuerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(mptIssuerAccountInfo.accountData().sequence())
      .destination(depositorKeyPair.publicKey().deriveAddress())
      .amount(
        MptCurrencyAmount.builder()
          .mptIssuanceId(mpTokenIssuanceId)
          .value("50000")
          .build()
      )
      .signingPublicKey(mptIssuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<Payment> signedMptPayment = signatureService.sign(
      mptIssuerKeyPair.privateKey(),
      mptPayment
    );
    SubmitResult<Payment> mptPaymentSubmitResult = xrplClient.submit(signedMptPayment);
    assertThat(mptPaymentSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    this.scanForResult(
      () -> this.getValidatedTransaction(signedMptPayment.hash(), Payment.class)
    );

    // Step 7: Deposit into vault
    depositorAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(depositorKeyPair.publicKey().deriveAddress())
    );

    VaultDeposit deposit = VaultDeposit.builder()
      .account(depositorKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(depositorAccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .amount(
        MptCurrencyAmount.builder()
          .mptIssuanceId(mpTokenIssuanceId)
          .value("10000")
          .build()
      )
      .signingPublicKey(depositorKeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultDeposit> signedDeposit = signatureService.sign(
      depositorKeyPair.privateKey(),
      deposit
    );
    SubmitResult<VaultDeposit> depositSubmitResult = xrplClient.submit(signedDeposit);
    assertThat(depositSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    // Wait for transaction to be validated
    this.scanForResult(
      () -> this.getValidatedTransaction(signedDeposit.hash(), VaultDeposit.class)
    );

    // Step 8: Verify vault state after deposit via vault_info
    VaultInfoResult vaultInfoAfterDeposit = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    assertThat(vaultInfoAfterDeposit.vault().assetsTotal()).isEqualTo(AssetAmount.of("10000"));
    assertThat(vaultInfoAfterDeposit.vault().assetsAvailable()).isEqualTo(AssetAmount.of("10000"));
    assertThat(vaultInfoAfterDeposit.vault().shares()).isNotEmpty();

    // Step 9: Clawback from vault
    AccountInfoResult mptIssuerAccountInfoForClawback = this.scanForResult(
      () -> this.getValidatedAccountInfo(mptIssuerKeyPair.publicKey().deriveAddress())
    );

    VaultClawback clawback = VaultClawback.builder()
      .account(mptIssuerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(mptIssuerAccountInfoForClawback.accountData().sequence())
      .vaultId(vaultId)
      .holder(depositorKeyPair.publicKey().deriveAddress())
      .amount(
        MptCurrencyAmount.builder()
          .mptIssuanceId(mpTokenIssuanceId)
          .value("2000")
          .build()
      )
      .signingPublicKey(mptIssuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultClawback> signedClawback = signatureService.sign(
      mptIssuerKeyPair.privateKey(),
      clawback
    );
    SubmitResult<VaultClawback> clawbackSubmitResult = xrplClient.submit(signedClawback);
    assertThat(clawbackSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    this.scanForResult(
      () -> this.getValidatedTransaction(signedClawback.hash(), VaultClawback.class)
    );

    // Verify vault state after clawback
    VaultInfoResult vaultInfoAfterClawback = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    assertThat(vaultInfoAfterClawback.vault().assetsTotal()).isEqualTo(AssetAmount.of("8000"));
    assertThat(vaultInfoAfterClawback.vault().assetsAvailable()).isEqualTo(AssetAmount.of("8000"));

    // Step 10: Withdraw from vault
    depositorAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(depositorKeyPair.publicKey().deriveAddress())
    );

    VaultWithdraw withdraw = VaultWithdraw.builder()
      .account(depositorKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(depositorAccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .amount(
        MptCurrencyAmount.builder()
          .mptIssuanceId(mpTokenIssuanceId)
          .value("4000")
          .build()
      )
      .signingPublicKey(depositorKeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultWithdraw> signedWithdraw = signatureService.sign(
      depositorKeyPair.privateKey(),
      withdraw
    );
    SubmitResult<VaultWithdraw> withdrawSubmitResult = xrplClient.submit(signedWithdraw);
    assertThat(withdrawSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    // Wait for transaction to be validated
    this.scanForResult(
      () -> this.getValidatedTransaction(signedWithdraw.hash(), VaultWithdraw.class)
    );

    // Step 11: Verify vault state after withdrawal via vault_info
    VaultInfoResult vaultInfoAfterWithdraw = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    assertThat(vaultInfoAfterWithdraw.vault().assetsTotal()).isEqualTo(AssetAmount.of("4000"));
    assertThat(vaultInfoAfterWithdraw.vault().assetsAvailable()).isEqualTo(AssetAmount.of("4000"));

    // Step 12: Update vault settings with VaultSet
    vaultOwnerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(vaultOwnerKeyPair.publicKey().deriveAddress())
    );

    VaultSet vaultSet = VaultSet.builder()
      .account(vaultOwnerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(vaultOwnerAccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .assetsMaximum(AssetAmount.of("100000000"))
      .data(VaultData.of("555044415445"))
      .signingPublicKey(vaultOwnerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultSet> signedVaultSet = signatureService.sign(
      vaultOwnerKeyPair.privateKey(),
      vaultSet
    );
    SubmitResult<VaultSet> vaultSetSubmitResult = xrplClient.submit(signedVaultSet);
    assertThat(vaultSetSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    // Wait for transaction to be validated
    this.scanForResult(
      () -> this.getValidatedTransaction(signedVaultSet.hash(), VaultSet.class)
    );

    // Step 13: Verify vault state after set (assetsMaximum and data updated)
    VaultInfoResult vaultInfoAfterSet = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    assertThat(vaultInfoAfterSet.vault().assetsMaximum()).isEqualTo(AssetAmount.of("100000000"));
    assertThat(vaultInfoAfterSet.vault().data()).isNotEmpty().get().isEqualTo(VaultData.of("555044415445"));
    assertThat(vaultInfoAfterSet.vault().assetsTotal()).isEqualTo(AssetAmount.of("4000"));

    // Step 14: Withdraw remaining assets before delete
    depositorAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(depositorKeyPair.publicKey().deriveAddress())
    );

    VaultWithdraw withdrawAll = VaultWithdraw.builder()
      .account(depositorKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(depositorAccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .amount(
        MptCurrencyAmount.builder()
          .mptIssuanceId(mpTokenIssuanceId)
          .value("4000")
          .build()
      )
      .signingPublicKey(depositorKeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultWithdraw> signedWithdrawAll = signatureService.sign(
      depositorKeyPair.privateKey(),
      withdrawAll
    );
    xrplClient.submit(signedWithdrawAll);

    // Wait for transaction to be validated
    this.scanForResult(
      () -> this.getValidatedTransaction(signedWithdrawAll.hash(), VaultWithdraw.class)
    );

    // Step 15: Delete vault
    vaultOwnerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(vaultOwnerKeyPair.publicKey().deriveAddress())
    );

    VaultDelete vaultDelete = VaultDelete.builder()
      .account(vaultOwnerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(vaultOwnerAccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .signingPublicKey(vaultOwnerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultDelete> signedVaultDelete = signatureService.sign(
      vaultOwnerKeyPair.privateKey(),
      vaultDelete
    );
    SubmitResult<VaultDelete> vaultDeleteSubmitResult = xrplClient.submit(signedVaultDelete);
    assertThat(vaultDeleteSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    // Wait for transaction to be validated
    this.scanForResult(
      () -> this.getValidatedTransaction(signedVaultDelete.hash(), VaultDelete.class)
    );
  }

  /**
   * Test vault with XRP as asset. Exercises all vault transactions and verifies vault state through
   * vault_info, ledger_entry, and account_objects.
   */
  @Test
  void vaultWithXrpAsset() throws Exception {
    // Step 1: Create accounts
    KeyPair vaultOwnerKeyPair = createRandomAccountEd25519();
    final KeyPair depositorKeyPair = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();

    // Step 2: Create vault with all optional fields
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
      .data(VaultData.of("585250"))
      .signingPublicKey(vaultOwnerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultCreate> signedVaultCreate = signatureService.sign(
      vaultOwnerKeyPair.privateKey(),
      vaultCreate
    );
    SubmitResult<VaultCreate> vaultCreateSubmitResult = xrplClient.submit(signedVaultCreate);
    assertThat(vaultCreateSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    // Wait for transaction to be validated
    this.scanForResult(
      () -> this.getValidatedTransaction(signedVaultCreate.hash(), VaultCreate.class)
    );

    // Get vault ID from ledger entry
    LedgerEntryResult<VaultObject> vaultLedgerEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.vault(
        VaultLedgerEntryParams.builder()
          .owner(vaultOwnerKeyPair.publicKey().deriveAddress())
          .seq(vaultOwnerAccountInfo.accountData().sequence())
          .build(),
        LedgerSpecifier.VALIDATED
      )
    );
    Hash256 vaultId = vaultLedgerEntry.node().index();

    // Step 3: Verify vault creation via vault_info (query by vaultId)
    VaultInfoResult vaultInfoByIdResult = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    VaultObject vaultFromInfoById = vaultInfoByIdResult.vault();
    assertThat(vaultFromInfoById.owner()).isEqualTo(vaultOwnerKeyPair.publicKey().deriveAddress());
    assertThat(vaultFromInfoById.asset()).isEqualTo(Issue.XRP);
    assertThat(vaultFromInfoById.assetsMaximum()).isEqualTo(AssetAmount.of("10000000000"));
    assertThat(vaultFromInfoById.scale()).isEqualTo(AssetScale.of(UnsignedInteger.ZERO));
    assertThat(vaultFromInfoById.data()).isNotEmpty().get().isEqualTo(VaultData.of("585250"));
    assertThat(vaultFromInfoById.withdrawalPolicy()).isEqualTo(WithdrawalPolicy.FIRST_COME_FIRST_SERVE);
    assertThat(vaultFromInfoById.assetsTotal()).isEqualTo(AssetAmount.of("0"));
    assertThat(vaultFromInfoById.assetsAvailable()).isEqualTo(AssetAmount.of("0"));
    assertThat(vaultFromInfoById.shares()).isNotEmpty();

    // Verify vault_info (query by owner and seq)
    VaultInfoResult vaultInfoByOwnerSeqResult = xrplClient.vaultInfo(
      VaultInfoRequestParams.of(
        vaultOwnerKeyPair.publicKey().deriveAddress(),
        vaultOwnerAccountInfo.accountData().sequence()
      )
    );
    assertThat(vaultInfoByOwnerSeqResult.vault()).isEqualTo(vaultFromInfoById);

    // Verify ledger_entry and account_objects
    assertVaultEntryEqualsObjectFromAccountObjects(
      vaultOwnerKeyPair.publicKey().deriveAddress(),
      vaultOwnerAccountInfo.accountData().sequence()
    );

    // Step 4: Deposit into vault
    AccountInfoResult depositorAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(depositorKeyPair.publicKey().deriveAddress())
    );

    VaultDeposit deposit = VaultDeposit.builder()
      .account(depositorKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(depositorAccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .amount(XrpCurrencyAmount.ofDrops(1000000))
      .signingPublicKey(depositorKeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultDeposit> signedDeposit = signatureService.sign(
      depositorKeyPair.privateKey(),
      deposit
    );
    SubmitResult<VaultDeposit> depositSubmitResult = xrplClient.submit(signedDeposit);
    assertThat(depositSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    // Wait for transaction to be validated
    this.scanForResult(
      () -> this.getValidatedTransaction(signedDeposit.hash(), VaultDeposit.class)
    );

    // Step 5: Verify vault state after deposit via vault_info
    VaultInfoResult vaultInfoAfterDeposit = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    assertThat(vaultInfoAfterDeposit.vault().assetsTotal()).isEqualTo(AssetAmount.of("1000000"));
    assertThat(vaultInfoAfterDeposit.vault().assetsAvailable()).isEqualTo(AssetAmount.of("1000000"));
    assertThat(vaultInfoAfterDeposit.vault().shares()).isNotEmpty();

    // Step 6: Withdraw from vault with destination field
    depositorAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(depositorKeyPair.publicKey().deriveAddress())
    );

    VaultWithdraw withdraw = VaultWithdraw.builder()
      .account(depositorKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(depositorAccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .amount(XrpCurrencyAmount.ofDrops(500000))
      .destination(vaultOwnerKeyPair.publicKey().deriveAddress())
      .signingPublicKey(depositorKeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultWithdraw> signedWithdraw = signatureService.sign(
      depositorKeyPair.privateKey(),
      withdraw
    );
    SubmitResult<VaultWithdraw> withdrawSubmitResult = xrplClient.submit(signedWithdraw);
    assertThat(withdrawSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    // Wait for transaction to be validated
    this.scanForResult(
      () -> this.getValidatedTransaction(signedWithdraw.hash(), VaultWithdraw.class)
    );

    // Step 7: Verify vault state after withdrawal via vault_info
    VaultInfoResult vaultInfoAfterWithdraw = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    assertThat(vaultInfoAfterWithdraw.vault().assetsTotal()).isEqualTo(AssetAmount.of("500000"));
    assertThat(vaultInfoAfterWithdraw.vault().assetsAvailable()).isEqualTo(AssetAmount.of("500000"));

    // Step 8: Update vault settings with VaultSet
    vaultOwnerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(vaultOwnerKeyPair.publicKey().deriveAddress())
    );

    VaultSet vaultSet = VaultSet.builder()
      .account(vaultOwnerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(vaultOwnerAccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .assetsMaximum(AssetAmount.of("20000000000"))
      .data(VaultData.of("4E4557"))
      .signingPublicKey(vaultOwnerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultSet> signedVaultSet = signatureService.sign(
      vaultOwnerKeyPair.privateKey(),
      vaultSet
    );
    SubmitResult<VaultSet> vaultSetSubmitResult = xrplClient.submit(signedVaultSet);
    assertThat(vaultSetSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    // Wait for transaction to be validated
    this.scanForResult(
      () -> this.getValidatedTransaction(signedVaultSet.hash(), VaultSet.class)
    );

    // Step 9: Verify vault state after set (assetsMaximum and data updated)
    VaultInfoResult vaultInfoAfterSet = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    assertThat(vaultInfoAfterSet.vault().assetsMaximum()).isEqualTo(AssetAmount.of("20000000000"));
    assertThat(vaultInfoAfterSet.vault().data()).isNotEmpty().get().isEqualTo(VaultData.of("4E4557"));
    assertThat(vaultInfoAfterSet.vault().assetsTotal()).isEqualTo(AssetAmount.of("500000"));

    // Step 10: Withdraw remaining assets before delete
    depositorAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(depositorKeyPair.publicKey().deriveAddress())
    );

    VaultWithdraw withdrawAll = VaultWithdraw.builder()
      .account(depositorKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(depositorAccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .amount(XrpCurrencyAmount.ofDrops(500000))
      .signingPublicKey(depositorKeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultWithdraw> signedWithdrawAll = signatureService.sign(
      depositorKeyPair.privateKey(),
      withdrawAll
    );
    xrplClient.submit(signedWithdrawAll);

    // Wait for transaction to be validated
    this.scanForResult(
      () -> this.getValidatedTransaction(signedWithdrawAll.hash(), VaultWithdraw.class)
    );

    // Step 11: Delete vault
    vaultOwnerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(vaultOwnerKeyPair.publicKey().deriveAddress())
    );

    VaultDelete vaultDelete = VaultDelete.builder()
      .account(vaultOwnerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(vaultOwnerAccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .signingPublicKey(vaultOwnerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultDelete> signedVaultDelete = signatureService.sign(
      vaultOwnerKeyPair.privateKey(),
      vaultDelete
    );
    SubmitResult<VaultDelete> vaultDeleteSubmitResult = xrplClient.submit(signedVaultDelete);
    assertThat(vaultDeleteSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    // Wait for transaction to be validated
    this.scanForResult(
      () -> this.getValidatedTransaction(signedVaultDelete.hash(), VaultDelete.class)
    );
  }

  /**
   * Test vault with IOU as asset and DomainID set. Exercises all vault transactions with permissioned domain
   * and verifies vault state through vault_info, ledger_entry, and account_objects.
   */
  @Test
  void vaultWithIouAssetAndDomainId() throws Exception {
    // Step 1: Create accounts
    KeyPair credentialIssuerKeyPair = createRandomAccountEd25519();
    KeyPair domainOwnerKeyPair = createRandomAccountEd25519();
    KeyPair issuerKeyPair = createRandomAccountEd25519();
    final KeyPair vaultOwnerKeyPair = createRandomAccountEd25519();
    KeyPair authorizedDepositorKeyPair = createRandomAccountEd25519();

    FeeResult feeResult = xrplClient.fee();

    // Step 2: Create credentials and PermissionedDomain
    CredentialType credentialType = CredentialType.ofPlainText("KYC");
    createPermissionedDomain(domainOwnerKeyPair, credentialIssuerKeyPair, new CredentialType[]{credentialType});

    // Get the domain ID from the permissioned domain object
    PermissionedDomainObject domainObject = getPermissionedDomainObject(domainOwnerKeyPair.publicKey().deriveAddress());
    final Hash256 domainId = domainObject.index();

    // Step 3: Create and accept credential for authorized depositor
    createAndAcceptCredentials(
      credentialIssuerKeyPair, authorizedDepositorKeyPair, new CredentialType[]{credentialType}
    );

    // Step 4: Enable clawback and default ripple on issuer account
    AccountInfoResult issuerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    AccountSet enableClawback = AccountSet.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(issuerAccountInfo.accountData().sequence())
      .setFlag(AccountSetFlag.ALLOW_TRUSTLINE_CLAWBACK)
      .signingPublicKey(issuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<AccountSet> signedEnableClawback = signatureService.sign(
      issuerKeyPair.privateKey(),
      enableClawback
    );
    SubmitResult<AccountSet> enableClawbackResult = xrplClient.submit(signedEnableClawback);
    assertThat(enableClawbackResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    this.scanForResult(
      () -> this.getValidatedTransaction(signedEnableClawback.hash(), AccountSet.class)
    );

    // Enable default ripple on issuer account using AbstractIT helper
    setDefaultRipple(issuerKeyPair, feeResult);

    // Step 5: Define IOU asset and set up trust lines
    IouIssue usdIssue = IouIssue.builder()
      .currency("USD")
      .issuer(issuerKeyPair.publicKey().deriveAddress())
      .build();

    // Set up trust line using AbstractIT helper
    createTrustLine(
      authorizedDepositorKeyPair,
      IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(issuerKeyPair.publicKey().deriveAddress())
        .value("1000000")
        .build(),
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    // Step 6: Fund authorized depositor with IOU using AbstractIT helper
    sendIssuedCurrency(
      issuerKeyPair,
      authorizedDepositorKeyPair,
      IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(issuerKeyPair.publicKey().deriveAddress())
        .value("50000")
        .build(),
      FeeUtils.computeNetworkFees(feeResult).recommendedFee()
    );

    // Step 7: Create vault with DomainID and VAULT_PRIVATE flag
    AccountInfoResult vaultOwnerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(vaultOwnerKeyPair.publicKey().deriveAddress())
    );

    VaultCreate vaultCreate = VaultCreate.builder()
      .account(vaultOwnerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(vaultOwnerAccountInfo.accountData().sequence())
      .asset(usdIssue)
      .assetsMaximum(AssetAmount.of("500000"))
      .mpTokenMetadata(MpTokenMetadata.of("444F4D41494E"))
      .withdrawalPolicy(WithdrawalPolicy.FIRST_COME_FIRST_SERVE)
      .scale(AssetScale.of(UnsignedInteger.valueOf(2)))
      .data(VaultData.of("505249564154"))
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

    // Wait for transaction to be validated
    this.scanForResult(
      () -> this.getValidatedTransaction(signedVaultCreate.hash(), VaultCreate.class)
    );

    // Get vault ID from ledger entry
    LedgerEntryResult<VaultObject> vaultLedgerEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.vault(
        VaultLedgerEntryParams.builder()
          .owner(vaultOwnerKeyPair.publicKey().deriveAddress())
          .seq(vaultOwnerAccountInfo.accountData().sequence())
          .build(),
        LedgerSpecifier.VALIDATED
      )
    );
    Hash256 vaultId = vaultLedgerEntry.node().index();

    // Step 8: Verify vault creation with domainId via vault_info (query by vaultId)
    VaultInfoResult vaultInfoByIdResult = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    VaultObject vaultFromInfoById = vaultInfoByIdResult.vault();
    assertThat(vaultFromInfoById.owner()).isEqualTo(vaultOwnerKeyPair.publicKey().deriveAddress());
    assertThat(vaultFromInfoById.asset()).isEqualTo(usdIssue);
    assertThat(vaultFromInfoById.assetsMaximum()).isEqualTo(AssetAmount.of("500000"));
    assertThat(vaultFromInfoById.scale()).isEqualTo(AssetScale.of(UnsignedInteger.valueOf(2)));
    assertThat(vaultFromInfoById.data()).isNotEmpty().get().isEqualTo(VaultData.of("505249564154"));
    assertThat(vaultFromInfoById.withdrawalPolicy()).isEqualTo(WithdrawalPolicy.FIRST_COME_FIRST_SERVE);
    assertThat(vaultFromInfoById.flags()).isEqualTo(VaultFlags.VAULT_PRIVATE);
    assertThat(vaultFromInfoById.assetsTotal()).isEqualTo(AssetAmount.of("0"));
    assertThat(vaultFromInfoById.assetsAvailable()).isEqualTo(AssetAmount.of("0"));
    assertThat(vaultFromInfoById.shares()).isNotEmpty();
    assertThat(vaultFromInfoById.shares().get().domainId()).isNotEmpty().get().isEqualTo(domainId);

    // Verify vault_info (query by owner and seq)
    VaultInfoResult vaultInfoByOwnerSeqResult = xrplClient.vaultInfo(
      VaultInfoRequestParams.of(
        vaultOwnerKeyPair.publicKey().deriveAddress(),
        vaultOwnerAccountInfo.accountData().sequence()
      )
    );
    assertThat(vaultInfoByOwnerSeqResult.vault()).isEqualTo(vaultFromInfoById);

    // Verify ledger_entry and account_objects
    assertVaultEntryEqualsObjectFromAccountObjects(
      vaultOwnerKeyPair.publicKey().deriveAddress(),
      vaultOwnerAccountInfo.accountData().sequence()
    );

    // Step 8: Deposit from authorized depositor (should succeed)
    AccountInfoResult authorizedDepositorAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(authorizedDepositorKeyPair.publicKey().deriveAddress())
    );

    VaultDeposit deposit = VaultDeposit.builder()
      .account(authorizedDepositorKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(authorizedDepositorAccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .amount(
        IssuedCurrencyAmount.builder()
          .currency("USD")
          .issuer(issuerKeyPair.publicKey().deriveAddress())
          .value("10000")
          .build()
      )
      .signingPublicKey(authorizedDepositorKeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultDeposit> signedDeposit = signatureService.sign(
      authorizedDepositorKeyPair.privateKey(),
      deposit
    );
    SubmitResult<VaultDeposit> depositSubmitResult = xrplClient.submit(signedDeposit);
    assertThat(depositSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    // Wait for transaction to be validated
    this.scanForResult(
      () -> this.getValidatedTransaction(signedDeposit.hash(), VaultDeposit.class)
    );

    // Step 9: Verify vault state after deposit via vault_info
    VaultInfoResult vaultInfoAfterDeposit = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    assertThat(vaultInfoAfterDeposit.vault().assetsTotal()).isEqualTo(AssetAmount.of("10000"));
    assertThat(vaultInfoAfterDeposit.vault().assetsAvailable()).isEqualTo(AssetAmount.of("10000"));
    assertThat(vaultInfoAfterDeposit.vault().shares()).isNotEmpty();

    // Step 10: Withdraw from vault (withdraw first before clawback)
    authorizedDepositorAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(authorizedDepositorKeyPair.publicKey().deriveAddress())
    );

    VaultWithdraw withdraw = VaultWithdraw.builder()
      .account(authorizedDepositorKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(authorizedDepositorAccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .amount(
        IssuedCurrencyAmount.builder()
          .currency("USD")
          .issuer(issuerKeyPair.publicKey().deriveAddress())
          .value("4000")
          .build()
      )
      .destination(authorizedDepositorKeyPair.publicKey().deriveAddress())
      .signingPublicKey(authorizedDepositorKeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultWithdraw> signedWithdraw = signatureService.sign(
      authorizedDepositorKeyPair.privateKey(),
      withdraw
    );
    SubmitResult<VaultWithdraw> withdrawSubmitResult = xrplClient.submit(signedWithdraw);
    assertThat(withdrawSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    // Wait for transaction to be validated
    this.scanForResult(
      () -> this.getValidatedTransaction(signedWithdraw.hash(), VaultWithdraw.class)
    );

    // Step 11: Verify vault state after withdrawal via vault_info
    VaultInfoResult vaultInfoAfterWithdraw = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    assertThat(vaultInfoAfterWithdraw.vault().assetsTotal()).isEqualTo(AssetAmount.of("6000"));
    assertThat(vaultInfoAfterWithdraw.vault().assetsAvailable()).isEqualTo(AssetAmount.of("6000"));

    // Step 12: Clawback from vault (issuer claws back the underlying IOU asset from depositor)
    AccountInfoResult issuerAccountInfoForClawback = this.scanForResult(
      () -> this.getValidatedAccountInfo(issuerKeyPair.publicKey().deriveAddress())
    );

    VaultClawback clawback = VaultClawback.builder()
      .account(issuerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(issuerAccountInfoForClawback.accountData().sequence())
      .vaultId(vaultId)
      .holder(authorizedDepositorKeyPair.publicKey().deriveAddress())
      .amount(
        IssuedCurrencyAmount.builder()
          .currency("USD")
          .issuer(issuerKeyPair.publicKey().deriveAddress())
          .value("2000")
          .build()
      )
      .signingPublicKey(issuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultClawback> signedClawback = signatureService.sign(
      issuerKeyPair.privateKey(),
      clawback
    );
    SubmitResult<VaultClawback> clawbackSubmitResult = xrplClient.submit(signedClawback);
    assertThat(clawbackSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    this.scanForResult(
      () -> this.getValidatedTransaction(signedClawback.hash(), VaultClawback.class)
    );

    // Verify vault state after clawback
    VaultInfoResult vaultInfoAfterClawback = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    assertThat(vaultInfoAfterClawback.vault().assetsTotal()).isEqualTo(AssetAmount.of("4000"));
    assertThat(vaultInfoAfterClawback.vault().assetsAvailable()).isEqualTo(AssetAmount.of("4000"));

    // Step 13: Update vault settings with VaultSet
    vaultOwnerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(vaultOwnerKeyPair.publicKey().deriveAddress())
    );

    VaultSet vaultSet = VaultSet.builder()
      .account(vaultOwnerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(vaultOwnerAccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .assetsMaximum(AssetAmount.of("1000000"))
      .data(VaultData.of("555044415445"))
      .signingPublicKey(vaultOwnerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultSet> signedVaultSet = signatureService.sign(
      vaultOwnerKeyPair.privateKey(),
      vaultSet
    );
    SubmitResult<VaultSet> vaultSetSubmitResult = xrplClient.submit(signedVaultSet);
    assertThat(vaultSetSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    // Wait for transaction to be validated
    this.scanForResult(
      () -> this.getValidatedTransaction(signedVaultSet.hash(), VaultSet.class)
    );

    // Step 14: Verify vault state after set (assetsMaximum and data updated)
    VaultInfoResult vaultInfoAfterSet = xrplClient.vaultInfo(VaultInfoRequestParams.of(vaultId));
    assertThat(vaultInfoAfterSet.vault().assetsMaximum()).isEqualTo(AssetAmount.of("1000000"));
    assertThat(vaultInfoAfterSet.vault().data()).isNotEmpty().get().isEqualTo(VaultData.of("555044415445"));
    assertThat(vaultInfoAfterSet.vault().assetsTotal()).isEqualTo(AssetAmount.of("4000"));

    // Step 15: Withdraw remaining assets before delete
    authorizedDepositorAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(authorizedDepositorKeyPair.publicKey().deriveAddress())
    );

    VaultWithdraw withdrawAll = VaultWithdraw.builder()
      .account(authorizedDepositorKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(authorizedDepositorAccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .amount(
        IssuedCurrencyAmount.builder()
          .currency("USD")
          .issuer(issuerKeyPair.publicKey().deriveAddress())
          .value("4000")
          .build()
      )
      .destination(authorizedDepositorKeyPair.publicKey().deriveAddress())
      .signingPublicKey(authorizedDepositorKeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultWithdraw> signedWithdrawAll = signatureService.sign(
      authorizedDepositorKeyPair.privateKey(),
      withdrawAll
    );
    xrplClient.submit(signedWithdrawAll);

    // Wait for transaction to be validated
    this.scanForResult(
      () -> this.getValidatedTransaction(signedWithdrawAll.hash(), VaultWithdraw.class)
    );

    // Step 16: Delete vault
    vaultOwnerAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(vaultOwnerKeyPair.publicKey().deriveAddress())
    );

    VaultDelete vaultDelete = VaultDelete.builder()
      .account(vaultOwnerKeyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(vaultOwnerAccountInfo.accountData().sequence())
      .vaultId(vaultId)
      .signingPublicKey(vaultOwnerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<VaultDelete> signedVaultDelete = signatureService.sign(
      vaultOwnerKeyPair.privateKey(),
      vaultDelete
    );
    SubmitResult<VaultDelete> vaultDeleteSubmitResult = xrplClient.submit(signedVaultDelete);
    assertThat(vaultDeleteSubmitResult.engineResult()).isEqualTo(SUCCESS_STATUS);

    // Wait for transaction to be validated
    this.scanForResult(
      () -> this.getValidatedTransaction(signedVaultDelete.hash(), VaultDelete.class)
    );
  }

  /**
   * Helper method to verify that the vault object from ledger_entry matches the vault object
   * from account_objects query.
   */
  private void assertVaultEntryEqualsObjectFromAccountObjects(
    Address owner,
    UnsignedInteger ownerSequence
  ) throws JsonRpcClientErrorException {
    // Query vault via ledger_entry using VaultLedgerEntryParams
    LedgerEntryResult<VaultObject> vaultLedgerEntry = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.vault(
        VaultLedgerEntryParams.builder()
          .owner(owner)
          .seq(ownerSequence)
          .build(),
        LedgerSpecifier.VALIDATED
      )
    );

    VaultObject expectedVault = vaultLedgerEntry.node();

    // Query account_objects for vault type
    VaultObject vaultFromAccountObjects = this.scanForResult(
      () -> {
        try {
          return xrplClient.accountObjects(AccountObjectsRequestParams.builder()
            .type(AccountObjectType.VAULT)
            .account(owner)
            .ledgerSpecifier(LedgerSpecifier.VALIDATED)
            .build()
          ).accountObjects();
        } catch (JsonRpcClientErrorException e) {
          throw new RuntimeException(e);
        }
      },
      result -> !result.isEmpty()
    ).stream()
      .filter(obj -> obj instanceof VaultObject)
      .map(obj -> (VaultObject) obj)
      .filter(vault -> vault.index().equals(expectedVault.index()))
      .findFirst()
      .orElseThrow(() -> new AssertionError("Vault not found in account_objects"));

    // Verify the vault from account_objects matches the expected vault
    assertThat(vaultFromAccountObjects).isEqualTo(expectedVault);

    // Also verify by querying ledger_entry using the vault's index
    LedgerEntryResult<VaultObject> entryByIndex = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(expectedVault.index(), VaultObject.class, LedgerSpecifier.VALIDATED)
    );
    assertThat(entryByIndex.node()).isEqualTo(expectedVault);

    // Also verify untyped query
    LedgerEntryResult<LedgerObject> entryByIndexUntyped = xrplClient.ledgerEntry(
      LedgerEntryRequestParams.index(expectedVault.index(), LedgerSpecifier.VALIDATED)
    );
    assertThat(entryByIndexUntyped.node()).isEqualTo(expectedVault);
  }
}
