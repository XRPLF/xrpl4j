package com.ripple.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.ripple.xrpl4j.model.transactions.AccountSet;
import com.ripple.xrpl4j.model.transactions.AccountSet.AccountSetFlag;
import com.ripple.xrpl4j.model.transactions.Flags.AccountRootFlags;
import com.ripple.xrpl4j.wallet.Wallet;
import com.ripple.xrplj4.client.model.accounts.AccountInfoResult;
import com.ripple.xrplj4.client.model.fees.FeeResult;
import com.ripple.xrplj4.client.model.transactions.SubmissionResult;
import com.ripple.xrplj4.client.rippled.JsonRpcClientErrorException;
import org.junit.jupiter.api.Test;

import java.util.Objects;

/**
 * An integration test that submits an AccountSet transaction for each AccountSet flag for an account, validates each
 * one is applied, and then clears each flag and ensure the clearing operation is validated in the ledger.
 *
 * @see "https://xrpl.org/accountset.html"
 */
public class AccountSetIT extends AbstractIT {

  // TODO: Make an IT that sets all flags, and unsets only 1, and validate that only that 1 single flag was cleared.


  @Test
  public void disableAndEnableAllFlags() throws JsonRpcClientErrorException {

    Wallet wallet = createRandomAccount();

    ///////////////////////
    // Get validated account info and validate account state
    AccountInfoResult accountInfo = this.scanForValidatedAccountInfo(wallet.classicAddress());
    assertThat(accountInfo.status()).isEqualTo("success");
    assertThat(accountInfo.accountData().flags().lsfGlobalFreeze()).isEqualTo(false);

    //////////////////////
    // Set asfAccountTxnID (no corresponding ledger flag)
    FeeResult feeResult = xrplClient.fee();
    AccountSet accountSet = AccountSet.builder()
        .account(wallet.classicAddress())
        .fee(feeResult.drops().minimumFee())
        .sequence(accountInfo.accountData().sequence())
        .setFlag(AccountSetFlag.ACCOUNT_TXN_ID)
        .signingPublicKey(wallet.publicKey())
        .build();

    SubmissionResult<AccountSet> response = xrplClient.submit(wallet, accountSet, AccountSet.class);
    logger.info(
        "AccountSet transaction successful: https://testnet.xrpl.org/transactions/" + response.transaction().hash()
            .orElse("n/a")
    );
    assertThat(response.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");

    ///////////////////////
    // Set flags one-by-one
    assertSetFlag(wallet, AccountSetFlag.DEFAULT_RIPPLE, AccountRootFlags.DEFAULT_RIPPLE);
    assertSetFlag(wallet, AccountSetFlag.DEPOSIT_AUTH, AccountRootFlags.DEPOSIT_AUTH);
    assertSetFlag(wallet, AccountSetFlag.DISALLOW_XRP, AccountRootFlags.DISALLOW_XRP);
    assertSetFlag(wallet, AccountSetFlag.REQUIRE_AUTH, AccountRootFlags.REQUIRE_AUTH);
    assertSetFlag(wallet, AccountSetFlag.REQUIRE_DEST, AccountRootFlags.REQUIRE_DEST_TAG);
    assertSetFlag(wallet, AccountSetFlag.GLOBAL_FREEZE, AccountRootFlags.GLOBAL_FREEZE);

    assertClearFlag(wallet, AccountSetFlag.GLOBAL_FREEZE, AccountRootFlags.GLOBAL_FREEZE);
    assertClearFlag(wallet, AccountSetFlag.REQUIRE_DEST, AccountRootFlags.REQUIRE_DEST_TAG);
    assertClearFlag(wallet, AccountSetFlag.REQUIRE_AUTH, AccountRootFlags.REQUIRE_AUTH);
    assertClearFlag(wallet, AccountSetFlag.DISALLOW_XRP, AccountRootFlags.DISALLOW_XRP);
    assertClearFlag(wallet, AccountSetFlag.DEPOSIT_AUTH, AccountRootFlags.DEPOSIT_AUTH);
    assertClearFlag(wallet, AccountSetFlag.DEFAULT_RIPPLE, AccountRootFlags.DEFAULT_RIPPLE);
  }

  //////////////////////
  // Test Helpers
  //////////////////////

  private void assertSetFlag(
      final Wallet wallet, final AccountSetFlag accountSetFlag, final AccountRootFlags accountRootFlag
  ) throws JsonRpcClientErrorException {
    Objects.requireNonNull(wallet);
    Objects.requireNonNull(accountSetFlag);

    AccountInfoResult accountInfo = xrplClient.accountInfo(wallet.classicAddress());
    FeeResult feeResult = xrplClient.fee();
    AccountSet accountSet = AccountSet.builder()
        .account(wallet.classicAddress())
        .fee(feeResult.drops().minimumFee())
        .sequence(accountInfo.accountData().sequence())
        .setFlag(accountSetFlag)
        .signingPublicKey(wallet.publicKey())
        .build();

    SubmissionResult<AccountSet> response = xrplClient.submit(wallet, accountSet, AccountSet.class);
    assertThat(response.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info(
      "AccountSet SetFlag transaction successful (asf={}; arf={}): https://testnet.xrpl.org/transactions/{}",
      accountSetFlag, accountRootFlag, response.transaction().hash().orElse("n/a")
    );

    /////////////////////////
    // Validate Account State
    scanValidatedAccountInfoForCondition(wallet.classicAddress(), (accountInfoResult) -> {
        logger.info("AccountInfoResponse Flags: {}", accountInfoResult.accountData().flags());
        return accountInfoResult.accountData().flags().isSet(accountRootFlag);
      });

  }

  private void assertClearFlag(
      final Wallet wallet, final AccountSetFlag accountSetFlag, final AccountRootFlags accountRootFlag
  ) throws JsonRpcClientErrorException {
    Objects.requireNonNull(wallet);
    Objects.requireNonNull(accountSetFlag);

    AccountInfoResult accountInfo = xrplClient.accountInfo(wallet.classicAddress());
    FeeResult feeResult = xrplClient.fee();
    AccountSet accountSet = AccountSet.builder()
      .account(wallet.classicAddress())
      .fee(feeResult.drops().minimumFee())
      .sequence(accountInfo.accountData().sequence())
      .clearFlag(accountSetFlag)
      .signingPublicKey(wallet.publicKey())
      .build();
    SubmissionResult<AccountSet> response = xrplClient.submit(wallet, accountSet, AccountSet.class);
    assertThat(response.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info(
        "AccountSet ClearFlag transaction successful (asf={}; arf={}): https://testnet.xrpl.org/transactions/{}",
        accountSetFlag, accountRootFlag, response.transaction().hash().orElse("n/a")
    );

    /////////////////////////
    // Validate Account State
    scanValidatedAccountInfoForCondition(wallet.classicAddress(), accountInfoResult -> {
      logger.info("AccountInfoResponse Flags: {}", accountInfoResult.accountData().flags());
      return !accountInfoResult.accountData().flags().isSet(accountRootFlag);
    });
  }
}
