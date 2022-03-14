package org.xrpl.xrpl4j.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.given;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;

import com.google.common.primitives.UnsignedLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountChannelsRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountChannelsResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountLinesRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountLinesResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsResult;
import org.xrpl.xrpl4j.model.client.accounts.TrustLine;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerResult;
import org.xrpl.xrpl4j.model.client.path.RipplePathFindRequestParams;
import org.xrpl.xrpl4j.model.client.path.RipplePathFindResult;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionRequestParams;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.ledger.LedgerObject;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.TrustSet;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.xrpl.xrpl4j.tests.environment.XrplEnvironment;
import org.xrpl.xrpl4j.wallet.DefaultWalletFactory;
import org.xrpl.xrpl4j.wallet.SeedWalletGenerationResult;
import org.xrpl.xrpl4j.wallet.Wallet;
import org.xrpl.xrpl4j.wallet.WalletFactory;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class AbstractIT {

  public static final String SUCCESS_STATUS = "tesSUCCESS";

  protected static XrplEnvironment xrplEnvironment = XrplEnvironment.getConfiguredEnvironment();

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  protected final XrplClient xrplClient = xrplEnvironment.getXrplClient();
  protected final WalletFactory walletFactory = DefaultWalletFactory.getInstance();

  protected Wallet createRandomAccount() {
    ///////////////////////
    // Create the account
    SeedWalletGenerationResult seedResult = walletFactory.randomWallet(true);
    final Wallet wallet = seedResult.wallet();
    logger.info("Generated testnet wallet with XAddress={} (Classic={})", wallet.xAddress(), wallet.classicAddress());

    fundAccount(wallet);

    return wallet;
  }

  /**
   * Funds a wallet with 1000 XRP.
   *
   * @param wallet The {@link Wallet} to fund.
   */
  protected void fundAccount(Wallet wallet) {
    xrplEnvironment.fundAccount(wallet.classicAddress());
  }

  //////////////////////
  // Ledger Helpers
  //////////////////////

  protected <T> T scanForResult(Supplier<T> resultSupplier, Predicate<T> condition) {
    return given()
      .atMost(30, TimeUnit.SECONDS)
      .pollInterval(100, TimeUnit.MILLISECONDS)
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
      .pollInterval(100, TimeUnit.MILLISECONDS)
      .atMost(30, TimeUnit.SECONDS)
      .ignoreException(RuntimeException.class)
      .await()
      .until(resultSupplier::get, is(notNullValue()));
  }

  protected <T extends LedgerObject> T scanForLedgerObject(Supplier<T> ledgerObjectSupplier) {
    Objects.requireNonNull(ledgerObjectSupplier);
    return given()
      .pollInterval(100, TimeUnit.MILLISECONDS)
      .atMost(30, TimeUnit.SECONDS)
      .ignoreException(RuntimeException.class)
      .await()
      .until(ledgerObjectSupplier::get, is(notNullValue()));
  }

  protected AccountObjectsResult getValidatedAccountObjects(Address classicAddress) {
    try {
      AccountObjectsRequestParams params = AccountObjectsRequestParams.builder()
        .account(classicAddress)
        .ledgerSpecifier(LedgerSpecifier.VALIDATED)
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
        .ledgerSpecifier(LedgerSpecifier.VALIDATED)
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
        .ledgerSpecifier(LedgerSpecifier.VALIDATED)
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
        .ledgerSpecifier(LedgerSpecifier.VALIDATED)
        .build();
      return xrplClient.accountInfo(params);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  protected <T extends Transaction> TransactionResult<T> getValidatedTransaction(
    Hash256 transactionHash,
    Class<T> transactionType
  ) {
    try {
      TransactionResult<T> transaction = xrplClient.transaction(
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
        .ledgerSpecifier(LedgerSpecifier.VALIDATED)
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
        .ledgerSpecifier(LedgerSpecifier.VALIDATED)
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
        .ledgerSpecifier(LedgerSpecifier.VALIDATED)
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

  /**
   * Create a trustline between the given issuer and counterparty accounts for the given currency code and
   * with the given limit.
   *
   * @param currency           The currency code of the trustline to create.
   * @param value              The trustline limit of the trustline to create.
   * @param issuerWallet       The {@link Wallet} of the issuer account.
   * @param counterpartyWallet The {@link Wallet} of the counterparty account.
   * @param fee                The current network fee, as an {@link XrpCurrencyAmount}.
   *
   * @return The {@link TrustLine} that gets created.
   * @throws JsonRpcClientErrorException If anything goes wrong while communicating with rippled.
   */
  public TrustLine createTrustLine(
    String currency,
    String value,
    Wallet issuerWallet,
    Wallet counterpartyWallet,
    XrpCurrencyAmount fee
  ) throws JsonRpcClientErrorException {
    AccountInfoResult counterpartyAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(counterpartyWallet.classicAddress())
    );

    TrustSet trustSet = TrustSet.builder()
      .account(counterpartyWallet.classicAddress())
      .fee(fee)
      .sequence(counterpartyAccountInfo.accountData().sequence())
      .limitAmount(IssuedCurrencyAmount.builder()
        .currency(currency)
        .issuer(issuerWallet.classicAddress())
        .value(value)
        .build())
      .signingPublicKey(counterpartyWallet.publicKey())
      .build();

    SubmitResult<TrustSet> trustSetSubmitResult = xrplClient.submit(counterpartyWallet, trustSet);
    assertThat(trustSetSubmitResult.result()).isEqualTo("tesSUCCESS");
    assertThat(trustSetSubmitResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(trustSetSubmitResult.transactionResult().hash());
    logger.info(
      "TrustSet transaction successful: https://testnet.xrpl.org/transactions/" +
        trustSetSubmitResult.transactionResult().hash()
    );

    return scanForResult(
      () ->
        getValidatedAccountLines(issuerWallet.classicAddress(), counterpartyWallet.classicAddress()),
      linesResult -> !linesResult.lines().isEmpty()
    )
      .lines().get(0);
  }

  /**
   * Send issued currency funds from an issuer to a counterparty.
   *
   * @param currency           The currency code to send.
   * @param value              The amount of currency to send.
   * @param issuerWallet       The {@link Wallet} of the issuer account.
   * @param counterpartyWallet The {@link Wallet} of the counterparty account.
   * @param fee                The current network fee, as an {@link XrpCurrencyAmount}.
   *
   * @throws JsonRpcClientErrorException If anything goes wrong while communicating with rippled.
   */
  public void sendIssuedCurrency(
    String currency,
    String value,
    Wallet issuerWallet,
    Wallet counterpartyWallet,
    XrpCurrencyAmount fee
  ) throws JsonRpcClientErrorException {
    ///////////////////////////
    // Issuer sends a payment with the issued currency to the counterparty
    AccountInfoResult issuerAccountInfo = this.scanForResult(
      () -> getValidatedAccountInfo(issuerWallet.classicAddress())
    );

    Payment fundCounterparty = Payment.builder()
      .account(issuerWallet.classicAddress())
      .fee(fee)
      .sequence(issuerAccountInfo.accountData().sequence())
      .destination(counterpartyWallet.classicAddress())
      .amount(IssuedCurrencyAmount.builder()
        .issuer(issuerWallet.classicAddress())
        .currency(currency)
        .value(value)
        .build())
      .signingPublicKey(issuerWallet.publicKey())
      .build();

    SubmitResult<Payment> paymentResult = xrplClient.submit(issuerWallet, fundCounterparty);
    assertThat(paymentResult.result()).isEqualTo("tesSUCCESS");
    assertThat(paymentResult.transactionResult().transaction().hash()).isNotEmpty().get()
      .isEqualTo(paymentResult.transactionResult().hash());
    logger.info(
      "Payment transaction successful: https://testnet.xrpl.org/transactions/" +
        paymentResult.transactionResult().hash()
    );

    this.scanForResult(
      () -> getValidatedTransaction(
        paymentResult.transactionResult().hash(),
        Payment.class)
    );

  }
}
