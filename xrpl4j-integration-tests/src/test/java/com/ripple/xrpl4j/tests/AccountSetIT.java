package com.ripple.xrpl4j.tests;

import static com.ripple.xrpl4j.tests.OptionalMatchers.isPresent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Awaitility.given;
import static org.awaitility.Awaitility.with;

import com.ripple.xrpl4j.model.transactions.AccountSet;
import com.ripple.xrpl4j.model.transactions.AccountSet.AccountSetFlag;
import com.ripple.xrpl4j.model.transactions.Flags.AccountRootFlags;
import com.ripple.xrpl4j.wallet.DefaultWalletFactory;
import com.ripple.xrpl4j.wallet.SeedWalletGenerationResult;
import com.ripple.xrpl4j.wallet.Wallet;
import com.ripple.xrpl4j.wallet.WalletFactory;
import com.ripple.xrplj4.client.XrplClient;
import com.ripple.xrplj4.client.faucet.FaucetAccountResponse;
import com.ripple.xrplj4.client.faucet.FaucetClient;
import com.ripple.xrplj4.client.faucet.FundAccountRequest;
import com.ripple.xrplj4.client.model.accounts.AccountInfoResult;
import com.ripple.xrplj4.client.model.fees.FeeResult;
import com.ripple.xrplj4.client.model.transactions.SubmissionResult;
import com.ripple.xrplj4.client.rippled.RippledClientErrorException;
import okhttp3.HttpUrl;
import org.awaitility.Duration;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * An integration test that submits an AccountSet transaction for each AccountSet flag for an account, validates each
 * one is applied, and then clears each flag and ensure the clearing operation is validated in the ledger.
 *
 * @see "https://xrpl.org/accountset.html"
 */
public class AccountSetIT extends AbstractIT {

  // TODO: Make an IT that sets all flags, and unsets only 1, and validate that only that 1 single flag was cleared.


  @Test
  public void disableAndEnableAllFlags() throws RippledClientErrorException {

    ///////////////////////
    // Create the account
    SeedWalletGenerationResult seedResult = walletFactory.randomWallet(true);
    final Wallet wallet = seedResult.wallet();
    logger.info("Generated source testnet wallet with address {}", wallet.xAddress());

    ///////////////////////
    // Fund the account
    FaucetAccountResponse fundResponse = faucetClient.fundAccount(FundAccountRequest.of(wallet.classicAddress().value()));
    logger.info("Source account has been funded: {}", fundResponse);
    assertThat(fundResponse.amount()).isGreaterThan(0);

    ///////////////////////
    // Get validated account info and validate account state

    AccountInfoResult accountInfo = given()
      .ignoreException(RuntimeException.class)
      .await().until(() -> this.getValidatedAccountInfo(wallet), isPresent())
      .get();

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

  /**
   * Scan the ledger for the requested account until the requested flags show up in a validated version of the account.
   * If the requested form of the account doesn't show up after 30 seconds, throw an exception.
   *
   * @return
   */
  private Optional<Boolean> validatedAccountHasFlags(final Wallet wallet, final AccountRootFlags... flags) {
    Objects.requireNonNull(wallet);
    Objects.requireNonNull(flags);

    // If all flags are not present, return Optional.empty so the scanner will keep trying.
    // If the accountInfo has all the requested flags, return true. Otherwise return false.
    return this.getValidatedAccountInfo(wallet)
      .map(accountInfoResult -> {
        logger.info("AccountInfoResponse Flags: {}", accountInfoResult.accountData().flags());
        // If the accountInfo has all the requested flags, return true. Otherwise return false.
        boolean allFlagsPresent = Arrays.stream(flags)
          .allMatch(flag -> accountInfoResult.accountData().flags().isSet(flag));
        return allFlagsPresent ? Optional.of(true) : Optional.<Boolean>empty();
      }).orElse(Optional.empty());
  }

  /**
   * Scan the ledger for the requested account until the requested flags show up in a validated version of the account.
   * If the requested form of the account doesn't show up after 30 seconds, throw an exception.
   */
  private Optional<Boolean> validatedAccountHasNotFlags(final Wallet wallet, final AccountRootFlags... flags) {
    Objects.requireNonNull(wallet);
    Objects.requireNonNull(flags);

    // If all flags are not present, return Optional.empty so the scanner will keep trying.
    // If the accountInfo has all the requested flags, return true. Otherwise return false.
    return this.getValidatedAccountInfo(wallet)
      .map(accountInfoResult -> {
        logger.info("AccountInfoResponse Flags: {}", accountInfoResult.accountData().flags());
        // If the accountInfo has all the requested flags, return true. Otherwise return false.
        boolean noFlagsPresent = Arrays.stream(flags)
          .noneMatch(flag -> accountInfoResult.accountData().flags().isSet(flag));
        return noFlagsPresent ? Optional.of(true) : Optional.<Boolean>empty();
      }).orElse(Optional.empty());
  }

  private void assertSetFlag(
      final Wallet wallet, final AccountSetFlag accountSetFlag, final AccountRootFlags accountRootFlag
  ) throws RippledClientErrorException {
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
    // Returns true, or else throws...
    boolean result = given().ignoreException(RuntimeException.class)
      .await()
      .atMost(Duration.ONE_MINUTE)
      .until(
        () -> validatedAccountHasFlags(wallet, accountRootFlag),
        isPresent()
    ).get();
    assertThat(result).isTrue();
  }

  private void assertClearFlag(
      final Wallet wallet, final AccountSetFlag accountSetFlag, final AccountRootFlags accountRootFlag
  ) throws RippledClientErrorException {
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
    // Returns true, or else throws...
    boolean result = given().ignoreException(RuntimeException.class)
      .await()
      .atMost(Duration.ONE_MINUTE)
      .until(
        () -> validatedAccountHasNotFlags(wallet, accountRootFlag),
        isPresent()
    ).get();
    assertThat(result).isTrue();
  }
}
