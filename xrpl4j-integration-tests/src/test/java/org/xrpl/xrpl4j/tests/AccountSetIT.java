package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.flags.Flags.AccountRootFlags;
import org.xrpl.xrpl4j.model.transactions.AccountSet;
import org.xrpl.xrpl4j.model.transactions.AccountSet.AccountSetFlag;
import org.xrpl.xrpl4j.model.transactions.TransactionResultCode;
import org.xrpl.xrpl4j.wallet.Wallet;

import java.util.Objects;

/**
 * An integration test that submits an AccountSet transaction for each AccountSet flag for an account, validates each
 * one is applied, and then clears each flag and ensure the clearing operation is validated in the ledger.
 *
 * @see "https://xrpl.org/accountset.html"
 */
public class AccountSetIT extends AbstractIT {

  @Test
  public void enableAllAndDisableOne() throws JsonRpcClientErrorException {

    Wallet wallet = createRandomAccount();

    ///////////////////////
    // Get validated account info and validate account state
    AccountInfoResult accountInfo = this.scanForResult(() -> this.getValidatedAccountInfo(wallet.classicAddress()));
    assertThat(accountInfo.status()).isNotEmpty().get().isEqualTo("success");
    assertThat(accountInfo.accountData().flags().lsfGlobalFreeze()).isEqualTo(false);

    UnsignedInteger sequence = accountInfo.accountData().sequence();
    //////////////////////
    // Set asfAccountTxnID (no corresponding ledger flag)
    FeeResult feeResult = xrplClient.fee();
    AccountSet accountSet = AccountSet.builder()
        .account(wallet.classicAddress())
        .fee(feeResult.drops().openLedgerFee())
        .sequence(accountInfo.accountData().sequence())
        .setFlag(AccountSetFlag.ACCOUNT_TXN_ID)
        .signingPublicKey(wallet.publicKey())
        .build();

    SubmitResult<AccountSet> response = xrplClient.submit(wallet, accountSet);
    assertThat(response.result()).isEqualTo(TransactionResultCode.TES_SUCCESS);
    assertThat(response.transactionResult().transaction().hash()).isNotEmpty().get()
        .isEqualTo(response.transactionResult().hash());
    logger.info(
        "AccountSet transaction successful: https://testnet.xrpl.org/transactions/" + response.transactionResult().hash()
    );

    ///////////////////////
    // Set flags one-by-one
    sequence = sequence.plus(UnsignedInteger.ONE);
    assertSetFlag(wallet, sequence, AccountSetFlag.DEFAULT_RIPPLE, AccountRootFlags.DEFAULT_RIPPLE);
    sequence = sequence.plus(UnsignedInteger.ONE);
    assertSetFlag(wallet, sequence, AccountSetFlag.DEPOSIT_AUTH, AccountRootFlags.DEPOSIT_AUTH);
    sequence = sequence.plus(UnsignedInteger.ONE);
    assertSetFlag(wallet, sequence, AccountSetFlag.DISALLOW_XRP, AccountRootFlags.DISALLOW_XRP);
    sequence = sequence.plus(UnsignedInteger.ONE);
    assertSetFlag(wallet, sequence, AccountSetFlag.REQUIRE_AUTH, AccountRootFlags.REQUIRE_AUTH);
    sequence = sequence.plus(UnsignedInteger.ONE);
    assertSetFlag(wallet, sequence, AccountSetFlag.REQUIRE_DEST, AccountRootFlags.REQUIRE_DEST_TAG);
    sequence = sequence.plus(UnsignedInteger.ONE);
    assertSetFlag(wallet, sequence, AccountSetFlag.GLOBAL_FREEZE, AccountRootFlags.GLOBAL_FREEZE);
    sequence = sequence.plus(UnsignedInteger.ONE);

    AccountRootFlags flags1 = this.scanForResult(
      () -> this.getValidatedAccountInfo(wallet.classicAddress())
    ).accountData().flags();

    assertClearFlag(wallet, sequence, AccountSetFlag.GLOBAL_FREEZE, AccountRootFlags.GLOBAL_FREEZE);

    AccountRootFlags flags2 = this.scanForResult(
      () -> this.getValidatedAccountInfo(wallet.classicAddress())
    ).accountData().flags();

    assertThat(flags1.getValue() - flags2.getValue())
        .isEqualTo(AccountRootFlags.GLOBAL_FREEZE.getValue());
  }


  @Test
  public void disableAndEnableAllFlags() throws JsonRpcClientErrorException {

    Wallet wallet = createRandomAccount();

    ///////////////////////
    // Get validated account info and validate account state
    AccountInfoResult accountInfo = this.scanForResult(() -> this.getValidatedAccountInfo(wallet.classicAddress()));
    assertThat(accountInfo.status()).isNotEmpty().get().isEqualTo("success");
    assertThat(accountInfo.accountData().flags().lsfGlobalFreeze()).isEqualTo(false);

    UnsignedInteger sequence = accountInfo.accountData().sequence();
    //////////////////////
    // Set asfAccountTxnID (no corresponding ledger flag)
    FeeResult feeResult = xrplClient.fee();
    AccountSet accountSet = AccountSet.builder()
      .account(wallet.classicAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(accountInfo.accountData().sequence())
      .setFlag(AccountSetFlag.ACCOUNT_TXN_ID)
      .signingPublicKey(wallet.publicKey())
      .build();

    SubmitResult<AccountSet> response = xrplClient.submit(wallet, accountSet);
    assertThat(response.result()).isEqualTo(TransactionResultCode.TES_SUCCESS);
    assertThat(response.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(response.transactionResult().hash());
    logger.info(
      "AccountSet transaction successful: https://testnet.xrpl.org/transactions/" + response.transactionResult().hash()
    );

    ///////////////////////
    // Set flags one-by-one
    sequence = sequence.plus(UnsignedInteger.ONE);
    assertSetFlag(wallet, sequence, AccountSetFlag.DEFAULT_RIPPLE, AccountRootFlags.DEFAULT_RIPPLE);
    sequence = sequence.plus(UnsignedInteger.ONE);
    assertSetFlag(wallet, sequence, AccountSetFlag.DEPOSIT_AUTH, AccountRootFlags.DEPOSIT_AUTH);
    sequence = sequence.plus(UnsignedInteger.ONE);
    assertSetFlag(wallet, sequence, AccountSetFlag.DISALLOW_XRP, AccountRootFlags.DISALLOW_XRP);
    sequence = sequence.plus(UnsignedInteger.ONE);
    assertSetFlag(wallet, sequence, AccountSetFlag.REQUIRE_AUTH, AccountRootFlags.REQUIRE_AUTH);
    sequence = sequence.plus(UnsignedInteger.ONE);
    assertSetFlag(wallet, sequence, AccountSetFlag.REQUIRE_DEST, AccountRootFlags.REQUIRE_DEST_TAG);
    sequence = sequence.plus(UnsignedInteger.ONE);
    assertSetFlag(wallet, sequence, AccountSetFlag.GLOBAL_FREEZE, AccountRootFlags.GLOBAL_FREEZE);
    sequence = sequence.plus(UnsignedInteger.ONE);

    assertClearFlag(wallet, sequence, AccountSetFlag.GLOBAL_FREEZE, AccountRootFlags.GLOBAL_FREEZE);
    sequence = sequence.plus(UnsignedInteger.ONE);
    assertClearFlag(wallet, sequence, AccountSetFlag.REQUIRE_DEST, AccountRootFlags.REQUIRE_DEST_TAG);
    sequence = sequence.plus(UnsignedInteger.ONE);
    assertClearFlag(wallet, sequence, AccountSetFlag.REQUIRE_AUTH, AccountRootFlags.REQUIRE_AUTH);
    sequence = sequence.plus(UnsignedInteger.ONE);
    assertClearFlag(wallet, sequence, AccountSetFlag.DISALLOW_XRP, AccountRootFlags.DISALLOW_XRP);
    sequence = sequence.plus(UnsignedInteger.ONE);
    assertClearFlag(wallet, sequence, AccountSetFlag.DEPOSIT_AUTH, AccountRootFlags.DEPOSIT_AUTH);
    sequence = sequence.plus(UnsignedInteger.ONE);
    assertClearFlag(wallet, sequence, AccountSetFlag.DEFAULT_RIPPLE, AccountRootFlags.DEFAULT_RIPPLE);
  }

  //////////////////////
  // Test Helpers
  //////////////////////

  private void assertSetFlag(
    final Wallet wallet,
    final UnsignedInteger sequence,
    final AccountSetFlag accountSetFlag,
    final AccountRootFlags accountRootFlag
  ) throws JsonRpcClientErrorException {
    Objects.requireNonNull(wallet);
    Objects.requireNonNull(accountSetFlag);

    FeeResult feeResult = xrplClient.fee();
    AccountSet accountSet = AccountSet.builder()
      .account(wallet.classicAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(sequence)
      .setFlag(accountSetFlag)
      .signingPublicKey(wallet.publicKey())
      .build();

    SubmitResult<AccountSet> response = xrplClient.submit(wallet, accountSet);
    assertThat(response.result()).isEqualTo(TransactionResultCode.TES_SUCCESS);
    assertThat(response.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(response.transactionResult().hash());
    logger.info(
      "AccountSet SetFlag transaction successful (asf={}; arf={}): https://testnet.xrpl.org/transactions/{}",
      accountSetFlag, accountRootFlag, response.transactionResult().hash()
    );

    /////////////////////////
    // Validate Account State
    this.scanForResult(
      () -> this.getValidatedAccountInfo(wallet.classicAddress()),
      accountInfoResult -> {
        logger.info("AccountInfoResponse Flags: {}", accountInfoResult.accountData().flags());
        return accountInfoResult.accountData().flags().isSet(accountRootFlag);
      });
  }

  private void assertClearFlag(
    final Wallet wallet,
    final UnsignedInteger sequence,
    final AccountSetFlag accountSetFlag,
    final AccountRootFlags accountRootFlag
  ) throws JsonRpcClientErrorException {
    Objects.requireNonNull(wallet);
    Objects.requireNonNull(accountSetFlag);

    FeeResult feeResult = xrplClient.fee();
    AccountSet accountSet = AccountSet.builder()
      .account(wallet.classicAddress())
      .fee(feeResult.drops().openLedgerFee())
      .sequence(sequence)
      .clearFlag(accountSetFlag)
      .signingPublicKey(wallet.publicKey())
      .build();
    SubmitResult<AccountSet> response = xrplClient.submit(wallet, accountSet);
    assertThat(response.result()).isEqualTo(TransactionResultCode.TES_SUCCESS);
    assertThat(response.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(response.transactionResult().hash());
    logger.info(
      "AccountSet ClearFlag transaction successful (asf={}; arf={}): https://testnet.xrpl.org/transactions/{}",
      accountSetFlag, accountRootFlag, response.transactionResult().hash()
    );

    /////////////////////////
    // Validate Account State
    this.scanForResult(
      () -> this.getValidatedAccountInfo(wallet.classicAddress()),
      accountInfoResult -> {
        logger.info("AccountInfoResponse Flags: {}", accountInfoResult.accountData().flags());
        return !accountInfoResult.accountData().flags().isSet(accountRootFlag);
      });
  }
}
