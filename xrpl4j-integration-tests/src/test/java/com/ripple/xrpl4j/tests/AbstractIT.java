package com.ripple.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import com.ripple.xrpl4j.model.transactions.Address;
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
import com.ripple.xrplj4.client.model.accounts.AccountObjectsRequestParams;
import com.ripple.xrplj4.client.model.accounts.AccountObjectsResult;
import com.ripple.xrplj4.client.rippled.RippledClientErrorException;
import okhttp3.HttpUrl;
import org.awaitility.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Predicate;

public abstract class AbstractIT {

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  protected final FaucetClient faucetClient =
    FaucetClient.construct(HttpUrl.parse("https://faucet.altnet.rippletest.net"));

  protected final XrplClient xrplClient = new XrplClient(HttpUrl.parse("https://s.altnet.rippletest.net:51234"));
  protected final WalletFactory walletFactory = DefaultWalletFactory.getInstance();

  protected Wallet createRandomAccount() {
    ///////////////////////
    // Create the account
    SeedWalletGenerationResult seedResult = walletFactory.randomWallet(true);
    final Wallet wallet = seedResult.wallet();
    logger.info("Generated testnet wallet with address {}", wallet.xAddress());

    ///////////////////////
    // Fund the account
    FaucetAccountResponse fundResponse = faucetClient.fundAccount(FundAccountRequest.of(wallet.classicAddress().value()));
    logger.info("Account has been funded: {}", fundResponse);
    assertThat(fundResponse.amount()).isGreaterThan(0);
    return wallet;
  }

  //////////////////////
  // Ledger Helpers
  //////////////////////

  protected AccountObjectsResult scanAccountObjectsForCondition(
    final Address classicAddress,
    final Predicate<AccountObjectsResult> condition
  ) {
    return given()
      .pollDelay(Duration.TWO_SECONDS)
      .atMost(Duration.ONE_MINUTE.divide(2))
      .await()
      .until(() -> {
        AccountObjectsResult validatedAccountObjects = getAccountObjects(classicAddress);
        if (validatedAccountObjects == null) {
          return null;
        }
        return condition.test(validatedAccountObjects) ? validatedAccountObjects : null;
      }, is(notNullValue()));
  }

  protected AccountObjectsResult scanForAccountObjects(final Address classicAddress) {
    return given()
      .pollDelay(Duration.TWO_SECONDS)
      .atMost(Duration.ONE_MINUTE.divide(2))
      .ignoreException(RuntimeException.class)
      .await()
      .until(
        () -> getAccountObjects(classicAddress),
        is(notNullValue())
      );
  }

  protected AccountObjectsResult getAccountObjects(Address classicAddress) {
    try {
      return xrplClient.accountObjects(
        AccountObjectsRequestParams.builder()
        .account(classicAddress)
        .build()
      );
    } catch (RippledClientErrorException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Poll the ledger for the account until the given {@link Predicate} on the resulting {@link AccountInfoResult}
   * is true. This can be useful for validating account changes that may not take effect immediately.
   *
   * @param classicAddress The classic XRP address of the account to scan.
   * @param condition A {@link Predicate} which will be checked against each polling of {@link AccountInfoResult},
   *                  and will cause this method to return once true.
   * @return The {@link AccountInfoResult} that satisfied the {@code condition}.
   * @throws org.awaitility.core.ConditionTimeoutException If no {@link AccountInfoResult} matching the {@link Predicate}
   *          exists after 30 seconds.
   */
  protected AccountInfoResult scanAccountInfoForCondition(final Address classicAddress, Predicate<AccountInfoResult> condition) {
    return given()
      .pollDelay(Duration.TWO_SECONDS)
      .atMost(Duration.ONE_MINUTE.divide(2))
      .await()
      .until(() -> {
        AccountInfoResult validatedAccountInfo = getAccountInfoResult(classicAddress);
        if (validatedAccountInfo == null) {
          return null;
        }
        return condition.test(validatedAccountInfo) ? validatedAccountInfo : null;
      }, is(notNullValue()));
  }

  /**
   * Poll the ledger for the account using an {@link org.awaitility.Awaitility} for 30 seconds, until the account
   * exists.
   * @param classicAddress The classic XRPL {@link Address} of the account to scan for.
   * @return The {@link AccountInfoResult} associated with {@code classicAddress}.
   * @throws org.awaitility.core.ConditionTimeoutException If no {@link AccountInfoResult} for the given address
   *  exists after 30 seconds.
   */
  protected AccountInfoResult scanForAccountInfo(final Address classicAddress) {
    Objects.requireNonNull(classicAddress);
    return given()
      .pollDelay(Duration.TWO_SECONDS)
      .atMost(Duration.ONE_MINUTE.divide(2))
      .ignoreException(RuntimeException.class)
      .await().until(() -> getAccountInfoResult(classicAddress), is(notNullValue()));
  }

  private AccountInfoResult getAccountInfoResult(Address classicAddress) {
    try {
      return xrplClient.accountInfo(classicAddress, "validated");
    } catch (Exception | RippledClientErrorException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

}
