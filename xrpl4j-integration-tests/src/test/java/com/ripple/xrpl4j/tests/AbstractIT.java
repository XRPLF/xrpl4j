package com.ripple.xrpl4j.tests;

import static org.awaitility.Awaitility.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import com.ripple.xrpl4j.model.transactions.Address;
import com.ripple.xrpl4j.wallet.DefaultWalletFactory;
import com.ripple.xrpl4j.wallet.WalletFactory;
import com.ripple.xrplj4.client.XrplClient;
import com.ripple.xrplj4.client.faucet.FaucetClient;
import com.ripple.xrplj4.client.model.accounts.AccountInfoResult;
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

  //////////////////////
  // Ledger Helpers
  //////////////////////

  /**
   * Poll the ledger for the account until the given {@link Predicate} on the resulting {@link AccountInfoResult}
   * is true. This can be useful for validating account changes that may not take effect immediately.
   *
   * @param classicAddress The classic XRP address of the account to scan.
   * @param condition
   * @return
   */
  protected boolean scanAccountInfoForCondition(final Address classicAddress, Predicate<AccountInfoResult> condition) {
    return given()
      .pollDelay(Duration.TWO_SECONDS)
      .atMost(Duration.ONE_MINUTE.divide(2))
      .await()
      .until(() -> {
      AccountInfoResult validatedAccountInfo = getAccountInfoResult(classicAddress);
      if (validatedAccountInfo == null) {
        return false;
      }
      return condition.test(validatedAccountInfo);
    }, is(true));
  }

  /**
   * Poll the ledger for the account using an {@link org.awaitility.Awaitility} for 30 seconds, until the account
   * exists.
   * @param classicAddress
   */
  protected AccountInfoResult scanForAccountInfo(final Address classicAddress) {
    Objects.requireNonNull(classicAddress);
    return given().pollDelay(Duration.TWO_SECONDS).atMost(Duration.ONE_MINUTE.divide(2))
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
