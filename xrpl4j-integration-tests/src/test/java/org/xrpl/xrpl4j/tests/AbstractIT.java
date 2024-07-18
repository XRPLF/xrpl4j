package org.xrpl.xrpl4j.tests;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: integration-tests
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.awaitility.Durations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.codec.addresses.KeyType;
import org.xrpl.xrpl4j.crypto.JavaKeystoreLoader;
import org.xrpl.xrpl4j.crypto.ServerSecret;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.keys.PrivateKeyReference;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.crypto.signing.SignatureService;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.crypto.signing.bc.BcDerivedKeySignatureService;
import org.xrpl.xrpl4j.crypto.signing.bc.BcSignatureService;
import org.xrpl.xrpl4j.model.client.Finality;
import org.xrpl.xrpl4j.model.client.FinalityStatus;
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
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
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
import org.xrpl.xrpl4j.model.transactions.TransactionResultCodes;
import org.xrpl.xrpl4j.model.transactions.TransactionType;
import org.xrpl.xrpl4j.model.transactions.TrustSet;
import org.xrpl.xrpl4j.model.transactions.XChainCreateBridge;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.xrpl.xrpl4j.tests.environment.XrplEnvironment;

import java.security.Key;
import java.security.KeyStore;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * An abstract class that contains helper functionality to support all ITs.
 */
public abstract class AbstractIT {

  public static final Duration POLL_INTERVAL = Durations.ONE_HUNDRED_MILLISECONDS;

  public static final String SUCCESS_STATUS = TransactionResultCodes.TES_SUCCESS;

  protected static XrplEnvironment xrplEnvironment = XrplEnvironment.getConfiguredEnvironment();

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  protected final XrplClient xrplClient;

  protected final SignatureService<PrivateKey> signatureService;
  protected final SignatureService<PrivateKeyReference> derivedKeySignatureService;

  /**
   * No-args Constructor.
   */
  protected AbstractIT() {
    this.xrplClient = xrplEnvironment.getXrplClient();
    this.signatureService = this.constructSignatureService();
    this.derivedKeySignatureService = this.constructDerivedKeySignatureService();
  }

  /**
   * Helper function to print log statements for Integration Tests which is network specific.
   *
   * @param transactionType {@link TransactionType} to be logged for the executed transaction.
   * @param hash            {@link Hash256} to be logged for the executed transaction.
   */
  protected void logInfo(TransactionType transactionType, Hash256 hash) {
    String url = System.getProperty("useTestnet") != null ? "https://testnet.xrpl.org/transactions/" :
      (System.getProperty("useDevnet") != null ? "https://devnet.xrpl.org/transactions/" : "");
    logger.info(transactionType.value() + " transaction successful: {}{}", url, hash);
  }

  protected KeyPair createRandomAccountEd25519() {
    // Create the account
    final KeyPair randomKeyPair = Seed.ed25519Seed().deriveKeyPair();
    logger.info(
      "Generated testnet wallet with ClassicAddress={})",
      randomKeyPair.publicKey().deriveAddress()
    );

    fundAccount(randomKeyPair.publicKey().deriveAddress());

    return randomKeyPair;
  }

  protected KeyPair createRandomAccountSecp256k1() {
    // Create the account
    final KeyPair randomKeyPair = Seed.secp256k1Seed().deriveKeyPair();
    logger.info(
      "Generated testnet wallet with ClassicAddress={})",
      randomKeyPair.publicKey().deriveAddress()
    );

    fundAccount(randomKeyPair.publicKey().deriveAddress());

    return randomKeyPair;
  }

  protected PrivateKeyReference createRandomPrivateKeyReferenceEd25519() {
    final PrivateKeyReference privateKeyReference = new PrivateKeyReference() {
      @Override
      public KeyType keyType() {
        return KeyType.ED25519;
      }

      @Override
      public String keyIdentifier() {
        return UUID.randomUUID().toString();
      }
    };

    PublicKey publicKey = derivedKeySignatureService.derivePublicKey(privateKeyReference);
    logger.info("Generated testnet wallet with ClassicAddress={})", publicKey.deriveAddress());
    fundAccount(publicKey.deriveAddress());

    return privateKeyReference;
  }

  protected PrivateKeyReference createRandomPrivateKeyReferenceSecp256k1() {
    final PrivateKeyReference privateKeyReference = new PrivateKeyReference() {
      @Override
      public KeyType keyType() {
        return KeyType.SECP256K1;
      }

      @Override
      public String keyIdentifier() {
        return UUID.randomUUID().toString();
      }
    };

    PublicKey publicKey = derivedKeySignatureService.derivePublicKey(privateKeyReference);
    logger.info("Generated testnet wallet with ClassicAddress={})", publicKey.deriveAddress());
    fundAccount(publicKey.deriveAddress());

    return privateKeyReference;
  }

  /**
   * Funds a wallet with 1000 XRP.
   *
   * @param address The {@link Address} to fund.
   */
  protected void fundAccount(final Address address) {
    Objects.requireNonNull(address);
    xrplEnvironment.fundAccount(address);
  }

  protected <T extends Transaction> TransactionResult<T> signSubmitAndWait(
    T transaction,
    KeyPair keyPair,
    Class<T> transactionType
  )
    throws JsonRpcClientErrorException, JsonProcessingException {
    Preconditions.checkArgument(transaction.lastLedgerSequence().isPresent());

    SingleSignedTransaction<T> signedTransaction = signatureService.sign(
      keyPair.privateKey(),
      transaction
    );

    SubmitResult<T> voteSubmitResult = xrplClient.submit(signedTransaction);
    assertThat(voteSubmitResult.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

    Finality finality = scanForFinality(
      signedTransaction.hash(),
      voteSubmitResult.validatedLedgerIndex(),
      transaction.lastLedgerSequence().get(),
      transaction.sequence(),
      keyPair.publicKey().deriveAddress()
    );

    assertThat(finality.finalityStatus()).isEqualTo(FinalityStatus.VALIDATED_SUCCESS);

    return this.getValidatedTransaction(signedTransaction.hash(), transactionType);
  }

  //////////////////////
  // Ledger Helpers
  //////////////////////

  protected Finality scanForFinality(
    Hash256 transactionHash,
    LedgerIndex submittedOnLedgerIndex,
    UnsignedInteger lastLedgerSequence,
    UnsignedInteger transactionAccountSequence,
    Address account
  ) {
    return given()
      .pollInterval(POLL_INTERVAL)
      .atMost(Durations.ONE_MINUTE.dividedBy(2))
      .ignoreException(RuntimeException.class)
      .await()
      .until(
        () -> xrplClient.isFinal(
          transactionHash,
          submittedOnLedgerIndex,
          lastLedgerSequence,
          transactionAccountSequence,
          account
        ),
        is(equalTo(
            Finality.builder()
              .finalityStatus(FinalityStatus.VALIDATED_SUCCESS)
              .resultCode(TransactionResultCodes.TES_SUCCESS)
              .build()
          )
        )
      );
  }

  protected <T> T scanForResult(Supplier<T> resultSupplier, Predicate<T> condition) {
    return given()
      .atMost(Durations.ONE_MINUTE.dividedBy(2))
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
      .atMost(Durations.ONE_MINUTE.dividedBy(2))
      .ignoreException(RuntimeException.class)
      .await()
      .until(resultSupplier::get, is(notNullValue()));
  }

  protected <T extends LedgerObject> T scanForLedgerObject(Supplier<T> ledgerObjectSupplier) {
    Objects.requireNonNull(ledgerObjectSupplier);
    return given()
      .pollInterval(POLL_INTERVAL)
      .atMost(Durations.ONE_MINUTE.dividedBy(2))
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
    KeyPair sourceKeyPair,
    KeyPair destinationKeyPair,
    IssuedCurrencyAmount destinationAmount
  ) {
    try {
      RipplePathFindRequestParams pathFindParams = RipplePathFindRequestParams.builder()
        .sourceAccount(sourceKeyPair.publicKey().deriveAddress())
        .destinationAccount(destinationKeyPair.publicKey().deriveAddress())
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
   * Create a trustline between the given issuer and counterparty accounts for the given currency code and with the
   * given limit.
   *
   * @param currency           The currency code of the trustline to create.
   * @param value              The trustline limit of the trustline to create.
   * @param issuerKeyPair       The {@link KeyPair} of the issuer account.
   * @param counterpartyKeyPair The {@link KeyPair} of the counterparty account.
   * @param fee                The current network fee, as an {@link XrpCurrencyAmount}.
   *
   * @return The {@link TrustLine} that gets created.
   *
   * @throws JsonRpcClientErrorException If anything goes wrong while communicating with rippled.
   */
  public TrustLine createTrustLine(
    String currency,
    String value,
    KeyPair issuerKeyPair,
    KeyPair counterpartyKeyPair,
    XrpCurrencyAmount fee
  ) throws JsonRpcClientErrorException, JsonProcessingException {
    Address counterpartyAddress = counterpartyKeyPair.publicKey().deriveAddress();
    Address issuerAddress = issuerKeyPair.publicKey().deriveAddress();

    AccountInfoResult counterpartyAccountInfo = this.scanForResult(
      () -> this.getValidatedAccountInfo(counterpartyAddress)
    );

    TrustSet trustSet = TrustSet.builder()
      .account(counterpartyAddress)
      .fee(fee)
      .sequence(counterpartyAccountInfo.accountData().sequence())
      .limitAmount(IssuedCurrencyAmount.builder()
        .currency(currency)
        .issuer(issuerAddress)
        .value(value)
        .build())
      .signingPublicKey(counterpartyKeyPair.publicKey())
      .build();

    SingleSignedTransaction<TrustSet> signedTrustSet = signatureService.sign(
      counterpartyKeyPair.privateKey(),
      trustSet
    );
    SubmitResult<TrustSet> trustSetSubmitResult = xrplClient.submit(signedTrustSet);
    assertThat(trustSetSubmitResult.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);

    logInfo(
      trustSetSubmitResult.transactionResult().transaction().transactionType(),
      trustSetSubmitResult.transactionResult().hash()
    );

    return scanForResult(
      () ->
        getValidatedAccountLines(issuerAddress, counterpartyAddress),
      linesResult -> !linesResult.lines().isEmpty()
    )
      .lines().get(0);
  }

  /**
   * Send issued currency funds from an issuer to a counterparty.
   *
   * @param currency           The currency code to send.
   * @param value              The amount of currency to send.
   * @param issuerKeyPair       The {@link KeyPair} of the issuer account.
   * @param counterpartyKeyPair The {@link KeyPair} of the counterparty account.
   * @param fee                The current network fee, as an {@link XrpCurrencyAmount}.
   *
   * @throws JsonRpcClientErrorException If anything goes wrong while communicating with rippled.
   */
  public void sendIssuedCurrency(
    String currency,
    String value,
    KeyPair issuerKeyPair,
    KeyPair counterpartyKeyPair,
    XrpCurrencyAmount fee
  ) throws JsonRpcClientErrorException, JsonProcessingException {
    Address counterpartyAddress = counterpartyKeyPair.publicKey().deriveAddress();
    Address issuerAddress = issuerKeyPair.publicKey().deriveAddress();

    ///////////////////////////
    // Issuer sends a payment with the issued currency to the counterparty
    AccountInfoResult issuerAccountInfo = this.scanForResult(
      () -> getValidatedAccountInfo(issuerAddress)
    );

    Payment fundCounterparty = Payment.builder()
      .account(issuerAddress)
      .fee(fee)
      .sequence(issuerAccountInfo.accountData().sequence())
      .destination(counterpartyAddress)
      .amount(IssuedCurrencyAmount.builder()
        .issuer(issuerAddress)
        .currency(currency)
        .value(value)
        .build())
      .signingPublicKey(issuerKeyPair.publicKey())
      .build();

    SingleSignedTransaction<Payment> signedPayment = signatureService.sign(
      issuerKeyPair.privateKey(),
      fundCounterparty
    );
    SubmitResult<Payment> paymentResult = xrplClient.submit(signedPayment);
    assertThat(paymentResult.engineResult()).isEqualTo(TransactionResultCodes.TES_SUCCESS);
    assertThat(paymentResult.transactionResult().hash()).isEqualTo(signedPayment.hash());

    logInfo(
      paymentResult.transactionResult().transaction().transactionType(),
      paymentResult.transactionResult().hash()
    );

    this.scanForResult(
      () -> getValidatedTransaction(
        paymentResult.transactionResult().hash(),
        Payment.class)
    );

    this.scanForResult(
      () -> getValidatedAccountInfo(issuerAddress),
      result -> result.accountData().sequence().equals(issuerAccountInfo.accountData().sequence().plus(UnsignedInteger.ONE))
    );
  }

  //////////////////
  // Private Helpers
  //////////////////

  protected PrivateKeyReference constructPrivateKeyReference(
    final String keyIdentifier, final KeyType keyType
  ) {
    Objects.requireNonNull(keyIdentifier);
    Objects.requireNonNull(keyType);

    return new PrivateKeyReference() {
      @Override
      public String keyIdentifier() {
        return keyIdentifier;
      }

      @Override
      public KeyType keyType() {
        return keyType;
      }
    };
  }

  protected PrivateKey constructPrivateKey(
    final String keyIdentifier, final KeyType keyType
  ) {
    Objects.requireNonNull(keyIdentifier);
    Objects.requireNonNull(keyType);

    switch (keyType) {
      case ED25519: {
        return Seed.ed25519Seed().deriveKeyPair().privateKey();
      }
      case SECP256K1: {
        return Seed.secp256k1Seed().deriveKeyPair().privateKey();
      }
      default: {
        throw new RuntimeException("Unhandled KeyType: " + keyType);
      }
    }
  }

  protected SignatureService<PrivateKeyReference> constructDerivedKeySignatureService() {
    try {
      final Key secretKey = loadKeyStore().getKey("secret0", "password".toCharArray());
      return new BcDerivedKeySignatureService(() -> ServerSecret.of(secretKey.getEncoded()));
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  protected SignatureService<PrivateKey> constructSignatureService() {
    return new BcSignatureService();
  }

  protected KeyPair constructRandomAccount() {
    // Create the account
    final KeyPair randomKeyPair = Seed.ed25519Seed().deriveKeyPair();
    logger.info(
      "Generated testnet wallet with ClassicAddress={})",
      randomKeyPair.publicKey().deriveAddress()
    );

    fundAccount(randomKeyPair.publicKey().deriveAddress());

    return randomKeyPair;
  }


  protected PublicKey toPublicKey(final PrivateKeyReference privateKeyReference) {
    return derivedKeySignatureService.derivePublicKey(privateKeyReference);
  }

  protected Address toAddress(final PrivateKeyReference privateKeyReference) {
    return toPublicKey(privateKeyReference).deriveAddress();
  }

  private KeyStore loadKeyStore() {
    final String jksFileName = "crypto/crypto.p12";
    final char[] jksPassword = "password".toCharArray();
    return JavaKeystoreLoader.loadFromClasspath(jksFileName, jksPassword);
  }
}
