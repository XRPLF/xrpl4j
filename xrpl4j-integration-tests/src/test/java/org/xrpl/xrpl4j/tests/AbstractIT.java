package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import com.google.common.primitives.UnsignedLong;
import org.awaitility.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.model.client.accounts.AccountChannelsRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountChannelsResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountLinesRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountLinesResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsResult;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerResult;
import org.xrpl.xrpl4j.model.client.path.RipplePathFindRequestParams;
import org.xrpl.xrpl4j.model.client.path.RipplePathFindResult;
import org.xrpl.xrpl4j.model.client.rippled.XrplResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionRequestParams;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.ledger.LedgerObject;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.xrpl.xrpl4j.wallet.DefaultWalletFactory;
import org.xrpl.xrpl4j.wallet.SeedWalletGenerationResult;
import org.xrpl.xrpl4j.wallet.Wallet;
import org.xrpl.xrpl4j.wallet.WalletFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class AbstractIT {

  public static final Duration POLL_INTERVAL = Duration.ONE_HUNDRED_MILLISECONDS;
  protected static RippledContainer rippledContainer = new RippledContainer().start();

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  protected final XrplClient xrplClient = rippledContainer.getXrplClient();
  protected final WalletFactory walletFactory = DefaultWalletFactory.getInstance();

  protected Wallet createRandomAccount() {
    ///////////////////////
    // Create the account
    SeedWalletGenerationResult seedResult = walletFactory.randomWallet(true);
    final Wallet wallet = seedResult.wallet();
    logger.info("Generated testnet wallet with address {}", wallet.xAddress());

    fundAccount(wallet);

    return wallet;
  }

  /**
   * Funds a wallet with 1000 XRP using the Master wallet on the rippled test container.
   * @param wallet
   */
  protected void fundAccount(Wallet wallet) {
    try {
      sendPayment(rippledContainer.getMasterWallet(), wallet.classicAddress(),
        XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(1000)));
    } catch (JsonRpcClientErrorException e) {
      throw new RuntimeException("could not fund account", e);
    }
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

  protected <T extends XrplResult> T scanForResult(Supplier<T> resultSupplier) {
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

  protected <TxnType extends Transaction> TransactionResult<TxnType> getValidatedTransaction(
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

  protected AccountInfoResult getCurrentAccountInfo(Address classicAddress) {
    try {
      AccountInfoRequestParams params = AccountInfoRequestParams.builder()
        .account(classicAddress)
        .ledgerIndex("current")
        .build();
      return xrplClient.accountInfo(params);
    } catch (Exception | JsonRpcClientErrorException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  protected void sendPayment(Wallet sourceWallet, Address destinationAddress, XrpCurrencyAmount paymentAmount)
    throws JsonRpcClientErrorException {
    FeeResult feeResult = xrplClient.fee();
    AccountInfoResult accountInfo = this.scanForResult(() -> this.getCurrentAccountInfo(sourceWallet.classicAddress()));
    Payment payment = Payment.builder()
      .account(sourceWallet.classicAddress())
      .fee(feeResult.drops().minimumFee())
      .sequence(accountInfo.accountData().sequence())
      .destination(destinationAddress)
      .amount(paymentAmount)
      .signingPublicKey(sourceWallet.publicKey())
      .build();

    SubmitResult<Payment> result = xrplClient.submit(sourceWallet, payment);
    assertThat(result.engineResult()).isNotEmpty().get().isEqualTo("tesSUCCESS");
    logger.info("Payment successful: " + rippledContainer.getBaseUri().toString()
      + result.transaction().hash().orElse("n/a"));

    this.scanForResult(
      () -> this.getValidatedTransaction(
        result.transaction().hash()
          .orElseThrow(() -> new RuntimeException("Could not look up Payment because the result did not have a hash.")),
        Payment.class)
    );
  }

  Instant ledgerNow() throws JsonRpcClientErrorException {
    return xrplClient.serverInfo().time().toInstant();
  }

}
