package com.ripple.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.codec.binary.XrplBinaryCodec;
import com.ripple.xrpl4j.keypairs.DefaultKeyPairService;
import com.ripple.xrpl4j.keypairs.KeyPairService;
import com.ripple.xrpl4j.model.jackson.ObjectMapperFactory;
import com.ripple.xrpl4j.model.transactions.AccountSet;
import com.ripple.xrpl4j.model.transactions.AccountSet.AccountSetFlag;
import com.ripple.xrpl4j.model.transactions.Address;
import com.ripple.xrpl4j.model.transactions.Flags.AccountRootFlags;
import com.ripple.xrpl4j.model.transactions.ImmutableAccountSet;
import com.ripple.xrpl4j.wallet.DefaultWalletFactory;
import com.ripple.xrpl4j.wallet.SeedWalletGenerationResult;
import com.ripple.xrpl4j.wallet.Wallet;
import com.ripple.xrpl4j.wallet.WalletFactory;
import com.ripple.xrplj4.client.XrplClient;
import com.ripple.xrplj4.client.faucet.FaucetAccountResponse;
import com.ripple.xrplj4.client.faucet.FaucetClient;
import com.ripple.xrplj4.client.faucet.FundAccountRequest;
import com.ripple.xrplj4.client.model.accounts.AccountInfoRequestParams;
import com.ripple.xrplj4.client.model.accounts.AccountInfoResult;
import com.ripple.xrplj4.client.model.fees.FeeResult;
import com.ripple.xrplj4.client.model.transactions.SubmissionResult;
import com.ripple.xrplj4.client.model.transactions.SubmitAccountSetResponse;
import com.ripple.xrplj4.client.rippled.ImmutableJsonRpcRequest;
import com.ripple.xrplj4.client.rippled.JsonRpcRequest;
import com.ripple.xrplj4.client.rippled.RippledClient;
import com.ripple.xrplj4.client.rippled.RippledClientErrorException;
import com.ripple.xrplj4.client.rippled.TransactionBlobWrapper;
import com.ripple.xrplj4.client.rippled.XrplMethods;
import okhttp3.HttpUrl;
import org.immutables.value.Value;
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
public class AccountSetIT {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  public final FaucetClient faucetClient =
      FaucetClient.construct(HttpUrl.parse("https://faucet.altnet.rippletest.net"));

  public final XrplClient xrplClient = new XrplClient(HttpUrl.parse("https://s.altnet.rippletest.net:51234"));
  public final WalletFactory walletFactory = DefaultWalletFactory.getInstance();

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
    AccountInfoResult accountInfo = scanLedgerFor30Seconds(() -> this.getValidatedAccountInfo(wallet));
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
    return this.scanLedgerFor30Seconds(() -> {
      // If the accountInfo has all the requested flags, return true. Otherwise return false.
      return this.getValidatedAccountInfo(wallet)
          .filter(AccountInfoResult::validated)
          .map(accountInfoResult -> {
            logger.info("AccountInfoResponse Flags: {}", accountInfoResult.accountData().flags());
            // If the accountInfo has all the requested flags, return true. Otherwise return false.
            boolean allFlagsPresent = Arrays.stream(flags)
                .allMatch(flag -> accountInfoResult.accountData().flags().isSet(flag));
            return allFlagsPresent ? Optional.of(true) : Optional.empty();
          });
    });
  }

  /**
   * Scan the ledger for the requested account until the requested flags show up in a validated version of the account.
   * If the requested form of the account doesn't show up after 30 seconds, throw an exception.
   *
   * @return
   */
  private Optional<Boolean> validatedAccountHasNotFlags(final Wallet wallet, final AccountRootFlags... flags) {
    Objects.requireNonNull(wallet);
    Objects.requireNonNull(flags);

    // If all flags are not present, return Optional.empty so the scanner will keep trying.
    return this.scanLedgerFor30Seconds(() -> {
      // If the accountInfo has all the requested flags, return true. Otherwise return false.
      return this.getValidatedAccountInfo(wallet)
          .filter(AccountInfoResult::validated)
          .map(accountInfoResult -> {
            logger.info("AccountInfoResponse Flags: {}", accountInfoResult.accountData().flags());
            // If the accountInfo has all the requested flags, return true. Otherwise return false.
            boolean noFlagsPresent = Arrays.stream(flags)
                .noneMatch(flag -> accountInfoResult.accountData().flags().isSet(flag));
            return noFlagsPresent ? Optional.of(true) : Optional.empty();
          });
    });
  }

  //////////////////////
  // Ledger Helpers
  //////////////////////

  /**
   * Get the requested account from the most recently validated ledger, if the account exists.
   */
  private Optional<AccountInfoResult> getValidatedAccountInfo(final Wallet wallet) {
    Objects.requireNonNull(wallet);
    try {
      return Optional.ofNullable(xrplClient.accountInfo(wallet.classicAddress(), "validated"))
          .filter(AccountInfoResult::validated);
    } catch (Exception | RippledClientErrorException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Scan the ledger by calling the {@code supplier} until a value becomes present. If a value never becomes present
   * after 30 seconds, then throw an exception.
   *
   * @param supplier
   * @param <T>
   * @return
   */
  private <T> T scanLedgerFor30Seconds(final Supplier<Optional<T>> supplier) {
    Objects.requireNonNull(supplier);
    for (int i = 0; i < 30; i++) {
      try {
        Optional<T> value = supplier.get();
        if (value.isPresent()) {
          return value.get();
        } else {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e1) {
            throw new RuntimeException(e1.getMessage(), e1);
          }
        }
      } catch (Exception e) {
        // The rippleclient throws an exception if an account is not found.
        logger.warn(e.getMessage());
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e1) {
          throw new RuntimeException(e1.getMessage(), e1);
        }
      }
    }

    throw new RuntimeException("Unable to obtain value from XRPL before 30s timeout.");
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
    boolean result = scanLedgerFor30Seconds(
        () -> validatedAccountHasFlags(wallet, accountRootFlag)
    );
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
    boolean result = scanLedgerFor30Seconds(
        () -> validatedAccountHasNotFlags(wallet, accountRootFlag)
    );
    assertThat(result).isTrue();
  }
}
