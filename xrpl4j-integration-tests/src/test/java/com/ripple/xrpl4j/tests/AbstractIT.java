package com.ripple.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import com.google.common.primitives.UnsignedLong;
import com.ripple.xrpl4j.client.XrplClient;
import com.ripple.xrpl4j.client.faucet.FaucetAccountResponse;
import com.ripple.xrpl4j.client.faucet.FaucetClient;
import com.ripple.xrpl4j.client.faucet.FundAccountRequest;
import com.ripple.xrpl4j.client.model.JsonRpcResult;
import com.ripple.xrpl4j.client.model.accounts.AccountChannelsRequestParams;
import com.ripple.xrpl4j.client.model.accounts.AccountChannelsResult;
import com.ripple.xrpl4j.client.model.accounts.AccountInfoRequestParams;
import com.ripple.xrpl4j.client.model.accounts.AccountInfoResult;
import com.ripple.xrpl4j.client.model.accounts.AccountLinesRequestParams;
import com.ripple.xrpl4j.client.model.accounts.AccountLinesResult;
import com.ripple.xrpl4j.client.model.accounts.AccountObjectsRequestParams;
import com.ripple.xrpl4j.client.model.accounts.AccountObjectsResult;
import com.ripple.xrpl4j.client.model.ledger.LedgerRequestParams;
import com.ripple.xrpl4j.client.model.ledger.LedgerResult;
import com.ripple.xrpl4j.client.model.ledger.objects.LedgerObject;
import com.ripple.xrpl4j.client.model.path.RipplePathFindRequestParams;
import com.ripple.xrpl4j.client.model.path.RipplePathFindResult;
import com.ripple.xrpl4j.client.model.transactions.TransactionRequestParams;
import com.ripple.xrpl4j.client.model.transactions.TransactionResult;
import com.ripple.xrpl4j.client.rippled.JsonRpcClientErrorException;
import com.ripple.xrpl4j.model.transactions.Address;
import com.ripple.xrpl4j.model.transactions.Flags;
import com.ripple.xrpl4j.model.transactions.IssuedCurrencyAmount;
import com.ripple.xrpl4j.model.transactions.Transaction;
import com.ripple.xrpl4j.wallet.DefaultWalletFactory;
import com.ripple.xrpl4j.wallet.SeedWalletGenerationResult;
import com.ripple.xrpl4j.wallet.Wallet;
import com.ripple.xrpl4j.wallet.WalletFactory;
import okhttp3.HttpUrl;
import org.awaitility.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class AbstractIT {

  public static final Duration POLL_INTERVAL = Duration.ONE_HUNDRED_MILLISECONDS;
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

  protected <T> T scanForResult(Supplier<T> resultSupplier, Predicate<T> condition) {
    return given()
      .atMost(Duration.ONE_MINUTE.divide(2))
      .pollInterval(POLL_INTERVAL)
      .await()
      .until(() -> {
        T result = resultSupplier.get();
        if (result == null) {
          return null;
        }
        return condition.test(result) ? result : null;
      }, is(notNullValue()));
  }

  protected <T extends JsonRpcResult> T scanForResult(Supplier<T> resultSupplier) {
    Objects.requireNonNull(resultSupplier);
    return given()
      .pollInterval(POLL_INTERVAL)
      .atMost(Duration.ONE_MINUTE.divide(2))
      .ignoreException(RuntimeException.class)
      .await()
      .until(resultSupplier::get, is(notNullValue()));
  }

  protected <T extends LedgerObject> T scanForLedgerObject(Supplier<T> ledgerObjectSupplier) {
    Objects.requireNonNull(ledgerObjectSupplier);
    return given()
      .pollInterval(POLL_INTERVAL)
      .atMost(Duration.ONE_MINUTE.divide(2))
      .ignoreException(RuntimeException.class)
      .await()
      .until(ledgerObjectSupplier::get, is(notNullValue()));
  }

  protected AccountObjectsResult getValidatedAccountObjects(Address classicAddress) {
    try {
      AccountObjectsRequestParams params = AccountObjectsRequestParams.builder()
        .account(classicAddress)
        .ledgerIndex("validated")
        .build();
      return xrplClient.accountObjects(params);
    } catch (JsonRpcClientErrorException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  protected <T extends LedgerObject> List<T> getValidatedAccountObjects(Address classicAddress, Class<T> clazz) {
    try {
      AccountObjectsRequestParams params = AccountObjectsRequestParams.builder()
        .account(classicAddress)
        .ledgerIndex("validated")
        .build();
      List<LedgerObject> ledgerObjects = xrplClient.accountObjects(params).accountObjects();
      return ledgerObjects
        .stream()
        .filter(object -> clazz.isAssignableFrom(object.getClass()))
        .map(object -> (T) object)
        .collect(Collectors.toList());
    } catch (JsonRpcClientErrorException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  protected AccountChannelsResult getValidatedAccountChannels(Address classicAddress) {
    try {
      AccountChannelsRequestParams params = AccountChannelsRequestParams.builder()
        .account(classicAddress)
        .ledgerIndex("validated")
        .build();
      return xrplClient.accountChannels(params);
    } catch (JsonRpcClientErrorException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  protected AccountInfoResult getValidatedAccountInfo(Address classicAddress) {
    try {
      AccountInfoRequestParams params = AccountInfoRequestParams.builder()
        .account(classicAddress)
        .ledgerIndex("validated")
        .build();
      return xrplClient.accountInfo(params);
    } catch (Exception | JsonRpcClientErrorException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  protected <TxnType extends Transaction<? extends Flags>> TransactionResult<TxnType> getValidatedTransaction(
    String transactionHash,
    Class<TxnType> transactionType
  ) {
    try {
      TransactionResult<TxnType> transaction = xrplClient.transaction(
        TransactionRequestParams.of(transactionHash),
        transactionType
      );
      return transaction.validated() ? transaction : null;
    } catch (JsonRpcClientErrorException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  protected LedgerResult getValidatedLedger() {
    try {
      LedgerRequestParams params = LedgerRequestParams.builder()
        .ledgerIndex("validated")
        .build();
      return xrplClient.ledger(params);
    } catch (JsonRpcClientErrorException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  protected RipplePathFindResult getValidatedRipplePath(
    Wallet sourceWallet,
    Wallet destinationWallet,
    IssuedCurrencyAmount destinationAmount
  ) {
    try {
      RipplePathFindRequestParams pathFindParams = RipplePathFindRequestParams.builder()
        .sourceAccount(sourceWallet.classicAddress())
        .destinationAccount(destinationWallet.classicAddress())
        .destinationAmount(destinationAmount)
        .ledgerIndex("validated")
        .build();

      return xrplClient.ripplePathFind(pathFindParams);
    } catch (JsonRpcClientErrorException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  protected AccountLinesResult getValidatedAccountLines(Address classicAddress, Address peerAddress) {
    try {
      AccountLinesRequestParams params = AccountLinesRequestParams.builder()
        .account(classicAddress)
        .peer(peerAddress)
        .ledgerIndex("validated")
        .build();

      return xrplClient.accountLines(params);
    } catch (JsonRpcClientErrorException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  protected UnsignedLong instantToXrpTimestamp(Instant instant) {
    return UnsignedLong.valueOf(instant.getEpochSecond() - 0x386d4380);
  }

  protected Instant xrpTimestampToInstant(UnsignedLong xrpTimeStamp) {
    return Instant.ofEpochSecond(xrpTimeStamp.plus(UnsignedLong.valueOf(0x386d4380)).longValue());
  }

}
