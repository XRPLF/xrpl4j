package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.crypto.signing.bc.BcSignatureService;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.fees.FeeUtils;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.flags.AccountRootFlags;
import org.xrpl.xrpl4j.model.flags.AccountSetTransactionFlags;
import org.xrpl.xrpl4j.model.transactions.AccountSet;
import org.xrpl.xrpl4j.model.transactions.AccountSet.AccountSetFlag;

import java.util.Objects;

/**
 * An integration test that submits an AccountSet transaction for each AccountSet flag for an account, validates each
 * one is applied, and then clears each flag and ensure the clearing operation is validated in the ledger.
 *
 * @see "https://xrpl.org/accountset.html"
 */
public class AccountSetIT extends AbstractIT {

  @Test
  public void enableAllAndDisableOne() throws JsonRpcClientErrorException, JsonProcessingException {

    KeyPair keyPair = constructRandomAccount();

    ///////////////////////
    // Get validated account info and validate account state
    AccountInfoResult accountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(keyPair.publicKey().deriveAddress())
    );
    assertThat(accountInfo.status()).isNotEmpty().get().isEqualTo("success");
    assertThat(accountInfo.accountData().flags().lsfGlobalFreeze()).isEqualTo(false);

    UnsignedInteger sequence = accountInfo.accountData().sequence();
    //////////////////////
    // Set asfAccountTxnID (no corresponding ledger flag)
    FeeResult feeResult = xrplClient.fee();
    AccountSet accountSet = AccountSet.builder()
      .account(keyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(accountInfo.accountData().sequence())
      .setFlag(AccountSetFlag.ACCOUNT_TXN_ID)
      .signingPublicKey(keyPair.publicKey())
      .build();

    SingleSignedTransaction<AccountSet> signedAccountSet = signatureService.sign(
      keyPair.privateKey(), accountSet
    );
    SubmitResult<AccountSet> response = xrplClient.submit(signedAccountSet);

    assertThat(response.result()).isEqualTo("tesSUCCESS");
    assertThat(signedAccountSet.hash()).isEqualTo(response.transactionResult().hash());
    logger.info(
      "AccountSet transaction successful: https://testnet.xrpl.org/transactions/" + response.transactionResult().hash()
    );

    ///////////////////////
    // Set flags one-by-one
    sequence = sequence.plus(UnsignedInteger.ONE);
    assertSetFlag(keyPair, sequence, AccountSetFlag.DEFAULT_RIPPLE, AccountRootFlags.DEFAULT_RIPPLE);
    sequence = sequence.plus(UnsignedInteger.ONE);
    assertSetFlag(keyPair, sequence, AccountSetFlag.DEPOSIT_AUTH, AccountRootFlags.DEPOSIT_AUTH);
    sequence = sequence.plus(UnsignedInteger.ONE);
    assertSetFlag(keyPair, sequence, AccountSetFlag.DISALLOW_XRP, AccountRootFlags.DISALLOW_XRP);
    sequence = sequence.plus(UnsignedInteger.ONE);
    assertSetFlag(keyPair, sequence, AccountSetFlag.REQUIRE_AUTH, AccountRootFlags.REQUIRE_AUTH);
    sequence = sequence.plus(UnsignedInteger.ONE);
    assertSetFlag(keyPair, sequence, AccountSetFlag.REQUIRE_DEST, AccountRootFlags.REQUIRE_DEST_TAG);
    sequence = sequence.plus(UnsignedInteger.ONE);
    assertSetFlag(keyPair, sequence, AccountSetFlag.GLOBAL_FREEZE, AccountRootFlags.GLOBAL_FREEZE);
    sequence = sequence.plus(UnsignedInteger.ONE);

    AccountRootFlags flags1 = this.scanForResult(
      () -> this.getValidatedAccountInfo(keyPair.publicKey().deriveAddress())
    ).accountData().flags();

    assertClearFlag(keyPair, sequence, AccountSetFlag.GLOBAL_FREEZE, AccountRootFlags.GLOBAL_FREEZE);

    AccountRootFlags flags2 = this.scanForResult(
      () -> this.getValidatedAccountInfo(keyPair.publicKey().deriveAddress())
    ).accountData().flags();

    assertThat(flags1.getValue() - flags2.getValue())
      .isEqualTo(AccountRootFlags.GLOBAL_FREEZE.getValue());
  }

  @Test
  public void disableAndEnableAllFlags() throws JsonRpcClientErrorException, JsonProcessingException {

    KeyPair keyPair = constructRandomAccount();

    ///////////////////////
    // Get validated account info and validate account state
    AccountInfoResult accountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(keyPair.publicKey().deriveAddress())
    );
    assertThat(accountInfo.status()).isNotEmpty().get().isEqualTo("success");
    assertThat(accountInfo.accountData().flags().lsfGlobalFreeze()).isEqualTo(false);

    UnsignedInteger sequence = accountInfo.accountData().sequence();
    //////////////////////
    // Set asfAccountTxnID (no corresponding ledger flag)
    FeeResult feeResult = xrplClient.fee();
    AccountSet accountSet = AccountSet.builder()
      .account(keyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(accountInfo.accountData().sequence())
      .setFlag(AccountSetFlag.ACCOUNT_TXN_ID)
      .signingPublicKey(keyPair.publicKey())
      .build();

    SingleSignedTransaction<AccountSet> signedAccountSet = signatureService.sign(
      keyPair.privateKey(), accountSet
    );
    SubmitResult<AccountSet> response = xrplClient.submit(signedAccountSet);

    assertThat(response.result()).isEqualTo("tesSUCCESS");
    assertThat(response.transactionResult().hash()).isEqualTo(response.transactionResult().hash());
    logger.info(
      "AccountSet transaction successful: https://testnet.xrpl.org/transactions/" + response.transactionResult().hash()
    );

    ///////////////////////
    // Set flags one-by-one
    sequence = sequence.plus(UnsignedInteger.ONE);
    assertSetFlag(keyPair, sequence, AccountSetFlag.DEFAULT_RIPPLE, AccountRootFlags.DEFAULT_RIPPLE);
    sequence = sequence.plus(UnsignedInteger.ONE);
    assertSetFlag(keyPair, sequence, AccountSetFlag.DEPOSIT_AUTH, AccountRootFlags.DEPOSIT_AUTH);
    sequence = sequence.plus(UnsignedInteger.ONE);
    assertSetFlag(keyPair, sequence, AccountSetFlag.DISALLOW_XRP, AccountRootFlags.DISALLOW_XRP);
    sequence = sequence.plus(UnsignedInteger.ONE);
    assertSetFlag(keyPair, sequence, AccountSetFlag.REQUIRE_AUTH, AccountRootFlags.REQUIRE_AUTH);
    sequence = sequence.plus(UnsignedInteger.ONE);
    assertSetFlag(keyPair, sequence, AccountSetFlag.REQUIRE_DEST, AccountRootFlags.REQUIRE_DEST_TAG);
    sequence = sequence.plus(UnsignedInteger.ONE);
    assertSetFlag(keyPair, sequence, AccountSetFlag.GLOBAL_FREEZE, AccountRootFlags.GLOBAL_FREEZE);
    sequence = sequence.plus(UnsignedInteger.ONE);

    assertClearFlag(keyPair, sequence, AccountSetFlag.GLOBAL_FREEZE, AccountRootFlags.GLOBAL_FREEZE);
    sequence = sequence.plus(UnsignedInteger.ONE);
    assertClearFlag(keyPair, sequence, AccountSetFlag.REQUIRE_DEST, AccountRootFlags.REQUIRE_DEST_TAG);
    sequence = sequence.plus(UnsignedInteger.ONE);
    assertClearFlag(keyPair, sequence, AccountSetFlag.REQUIRE_AUTH, AccountRootFlags.REQUIRE_AUTH);
    sequence = sequence.plus(UnsignedInteger.ONE);
    assertClearFlag(keyPair, sequence, AccountSetFlag.DISALLOW_XRP, AccountRootFlags.DISALLOW_XRP);
    sequence = sequence.plus(UnsignedInteger.ONE);
    assertClearFlag(keyPair, sequence, AccountSetFlag.DEPOSIT_AUTH, AccountRootFlags.DEPOSIT_AUTH);
    sequence = sequence.plus(UnsignedInteger.ONE);
    assertClearFlag(keyPair, sequence, AccountSetFlag.DEFAULT_RIPPLE, AccountRootFlags.DEFAULT_RIPPLE);
  }

  @Test
  void enableAndDisableFlagsUsingTransactionFlags() throws JsonRpcClientErrorException, JsonProcessingException {
    BcSignatureService bcSignatureService = new BcSignatureService();
    KeyPair keyPair = constructRandomAccount();

    ///////////////////////
    // Get validated account info and validate account state
    AccountInfoResult accountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(keyPair.publicKey().deriveAddress()));
    assertThat(accountInfo.status()).isNotEmpty().get().isEqualTo("success");
    assertThat(accountInfo.accountData().flags().lsfGlobalFreeze()).isEqualTo(false);

    UnsignedInteger sequence = accountInfo.accountData().sequence();

    FeeResult feeResult = xrplClient.fee();
    AccountSet enableAccountSet = AccountSet.builder()
      .account(keyPair.publicKey().deriveAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(sequence)
      .signingPublicKey(keyPair.publicKey())
      .flags(
        AccountSetTransactionFlags.builder()
          .tfRequireDestTag()
          .tfRequireAuth()
          .tfDisallowXrp()
          .build()
      )
      .build();

    SingleSignedTransaction<AccountSet> signedTransaction
      = bcSignatureService.sign(keyPair.privateKey(), enableAccountSet);
    SubmitResult<AccountSet> enableResponse = xrplClient.submit(signedTransaction);
    assertThat(enableResponse.result()).isEqualTo("tesSUCCESS");
    assertThat(signedTransaction.hash()).isEqualTo(enableResponse.transactionResult().hash());
    logger.info(
      "AccountSet SetFlag transaction successful: https://testnet.xrpl.org/transactions/{}",
      enableResponse.transactionResult().hash()
    );

    /////////////////////////
    // Validate Account State
    this.scanForResult(
      () -> this.getValidatedAccountInfo(keyPair.publicKey().deriveAddress()),
      accountInfoResult -> {
        logger.info("AccountInfoResponse Flags: {}", accountInfoResult.accountData().flags());
        return accountInfoResult.accountData().flags().isSet(AccountRootFlags.REQUIRE_DEST_TAG) &&
          accountInfoResult.accountData().flags().isSet(AccountRootFlags.REQUIRE_AUTH) &&
          accountInfoResult.accountData().flags().isSet(AccountRootFlags.DISALLOW_XRP);
      });

    AccountSet disableAccountSet = AccountSet.builder()
      .account(keyPair.publicKey().deriveAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(sequence.plus(UnsignedInteger.ONE))
      .signingPublicKey(keyPair.publicKey())
      .flags(
        AccountSetTransactionFlags.builder()
          .tfOptionalDestTag()
          .tfOptionalAuth()
          .tfAllowXrp()
          .build()
      )
      .build();

    signedTransaction = bcSignatureService.sign(keyPair.privateKey(), disableAccountSet);
    SubmitResult<AccountSet> disableResponse = xrplClient.submit(signedTransaction);
    assertThat(disableResponse.result()).isEqualTo("tesSUCCESS");
    assertThat(signedTransaction.hash()).isEqualTo(disableResponse.transactionResult().hash());
    logger.info(
      "AccountSet SetFlag transaction successful: https://testnet.xrpl.org/transactions/{}",
      disableResponse.transactionResult().hash()
    );

    /////////////////////////
    // Validate Account State
    this.scanForResult(
      () -> this.getValidatedAccountInfo(keyPair.publicKey().deriveAddress()),
      accountInfoResult -> {
        logger.info("AccountInfoResponse Flags: {}", accountInfoResult.accountData().flags());
        return !accountInfoResult.accountData().flags().isSet(AccountRootFlags.REQUIRE_DEST_TAG) &&
          !accountInfoResult.accountData().flags().isSet(AccountRootFlags.REQUIRE_AUTH) &&
          !accountInfoResult.accountData().flags().isSet(AccountRootFlags.DISALLOW_XRP);
      });
  }

  @Test
  void disableMasterFailsWithNoSignerList() throws JsonRpcClientErrorException, JsonProcessingException {
    BcSignatureService bcSignatureService = new BcSignatureService();
    KeyPair keyPair = constructRandomAccount();

    ///////////////////////
    // Get validated account info and validate account state
    AccountInfoResult accountInfo = this.scanForResult(() -> this.getValidatedAccountInfo(
      keyPair.publicKey().deriveAddress()
    ));
    assertThat(accountInfo.status()).isNotEmpty().get().isEqualTo("success");

    UnsignedInteger sequence = accountInfo.accountData().sequence();

    FeeResult feeResult = xrplClient.fee();
    AccountSet enableAccountSet = AccountSet.builder()
      .account(keyPair.publicKey().deriveAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(sequence)
      .signingPublicKey(keyPair.publicKey())
      .setFlag(AccountSetFlag.DISABLE_MASTER)
      .build();

    SingleSignedTransaction<AccountSet> signedTransaction
      = bcSignatureService.sign(keyPair.privateKey(), enableAccountSet);
    SubmitResult<AccountSet> enableResponse = xrplClient.submit(signedTransaction);
    assertThat(enableResponse.result()).isEqualTo("tecNO_ALTERNATIVE_KEY");
    assertThat(signedTransaction.hash()).isEqualTo(enableResponse.transactionResult().hash());
    logger.info("AccountSet SetFlag transaction failed successfully:");
  }

  //////////////////////
  // Test Helpers
  //////////////////////

  private void assertSetFlag(
    final KeyPair keyPair,
    final UnsignedInteger sequence,
    final AccountSetFlag accountSetFlag,
    final AccountRootFlags accountRootFlag
  ) throws JsonRpcClientErrorException, JsonProcessingException {
    Objects.requireNonNull(keyPair);
    Objects.requireNonNull(accountSetFlag);

    FeeResult feeResult = xrplClient.fee();
    AccountSet accountSet = AccountSet.builder()
      .account(keyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(sequence)
      .setFlag(accountSetFlag)
      .signingPublicKey(keyPair.publicKey())
      .build();

    SingleSignedTransaction<AccountSet> signedAccountSet = signatureService.sign(
      keyPair.privateKey(), accountSet
    );
    SubmitResult<AccountSet> response = xrplClient.submit(signedAccountSet);

    assertThat(response.result()).isEqualTo("tesSUCCESS");
    assertThat(response.transactionResult().hash()).isEqualTo(response.transactionResult().hash());
    logger.info(
      "AccountSet SetFlag transaction successful (asf={}; arf={}): https://testnet.xrpl.org/transactions/{}",
      accountSetFlag, accountRootFlag, response.transactionResult().hash()
    );

    /////////////////////////
    // Validate Account State
    this.scanForResult(
      () -> this.getValidatedAccountInfo(keyPair.publicKey().deriveAddress()),
      accountInfoResult -> {
        logger.info("AccountInfoResponse Flags: {}", accountInfoResult.accountData().flags());
        return accountInfoResult.accountData().flags().isSet(accountRootFlag);
      });
  }

  private void assertClearFlag(
    final KeyPair keyPair,
    final UnsignedInteger sequence,
    final AccountSetFlag accountSetFlag,
    final AccountRootFlags accountRootFlag
  ) throws JsonRpcClientErrorException, JsonProcessingException {
    Objects.requireNonNull(keyPair);
    Objects.requireNonNull(accountSetFlag);

    FeeResult feeResult = xrplClient.fee();
    AccountSet accountSet = AccountSet.builder()
      .account(keyPair.publicKey().deriveAddress())
      .fee(FeeUtils.computeNetworkFees(feeResult).recommendedFee())
      .sequence(sequence)
      .clearFlag(accountSetFlag)
      .signingPublicKey(keyPair.publicKey())
      .build();

    SingleSignedTransaction<AccountSet> signedAccountSet = signatureService.sign(
      keyPair.privateKey(), accountSet
    );
    SubmitResult<AccountSet> response = xrplClient.submit(signedAccountSet);

    assertThat(response.result()).isEqualTo("tesSUCCESS");
    assertThat(response.transactionResult().hash()).isEqualTo(response.transactionResult().hash());
    logger.info(
      "AccountSet ClearFlag transaction successful (asf={}; arf={}): https://testnet.xrpl.org/transactions/{}",
      accountSetFlag, accountRootFlag, response.transactionResult().hash()
    );

    /////////////////////////
    // Validate Account State
    this.scanForResult(
      () -> this.getValidatedAccountInfo(keyPair.publicKey().deriveAddress()),
      accountInfoResult -> {
        logger.info("AccountInfoResponse Flags: {}", accountInfoResult.accountData().flags());
        return !accountInfoResult.accountData().flags().isSet(accountRootFlag);
      });
  }
}
