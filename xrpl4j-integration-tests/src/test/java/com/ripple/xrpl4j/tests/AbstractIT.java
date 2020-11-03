package com.ripple.xrpl4j.tests;

import com.ripple.xrpl4j.wallet.DefaultWalletFactory;
import com.ripple.xrpl4j.wallet.Wallet;
import com.ripple.xrpl4j.wallet.WalletFactory;
import com.ripple.xrplj4.client.XrplClient;
import com.ripple.xrplj4.client.faucet.FaucetClient;
import com.ripple.xrplj4.client.model.accounts.AccountInfoResult;
import com.ripple.xrplj4.client.rippled.RippledClientErrorException;
import okhttp3.HttpUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

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
   * Get the requested account from the most recently validated ledger, if the account exists.
   */
  protected Optional<AccountInfoResult> getValidatedAccountInfo(final Wallet wallet) {
    Objects.requireNonNull(wallet);
    try {
      return Optional.ofNullable(xrplClient.accountInfo(wallet.classicAddress(), "validated"));
    } catch (Exception | RippledClientErrorException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

}
