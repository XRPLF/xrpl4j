package org.xrpl.xrpl4j.tests.v3;

import static org.awaitility.Awaitility.given;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;

import com.google.common.primitives.UnsignedLong;
import org.awaitility.Durations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.codec.addresses.VersionType;
import org.xrpl.xrpl4j.crypto.bc.signing.BcDerivedKeySignatureService;
import org.xrpl.xrpl4j.crypto.bc.signing.BcSignatureService;
import org.xrpl.xrpl4j.crypto.core.JavaKeystoreLoader;
import org.xrpl.xrpl4j.crypto.core.ServerSecret;
import org.xrpl.xrpl4j.crypto.core.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.core.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.core.keys.PrivateKeyReference;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.core.keys.Seed;
import org.xrpl.xrpl4j.crypto.core.signing.SignatureService;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountChannelsRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountChannelsResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountLinesRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountLinesResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsResult;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerResult;
import org.xrpl.xrpl4j.model.client.path.RipplePathFindRequestParams;
import org.xrpl.xrpl4j.model.client.path.RipplePathFindResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionRequestParams;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.ledger.LedgerObject;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.TransactionType;
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
      public VersionType versionType() {
        return VersionType.ED25519;
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
      public VersionType versionType() {
        return VersionType.SECP256K1;
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

  //////////////////////
  // Ledger Helpers
  //////////////////////

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

  //////////////////
  // Private Helpers
  //////////////////

  protected PrivateKeyReference constructPrivateKeyReference(
    final String keyIdentifier, final VersionType versionType
  ) {
    Objects.requireNonNull(keyIdentifier);
    Objects.requireNonNull(versionType);

    return new PrivateKeyReference() {
      @Override
      public String keyIdentifier() {
        return keyIdentifier;
      }

      @Override
      public VersionType versionType() {
        return versionType;
      }
    };
  }

  protected PrivateKey constructPrivateKey(
    final String keyIdentifier, final VersionType versionType
  ) {
    Objects.requireNonNull(keyIdentifier);
    Objects.requireNonNull(versionType);

    switch (versionType) {
      case ED25519: {
        return Seed.ed25519Seed().deriveKeyPair().privateKey();
      }
      case SECP256K1: {
        return Seed.secp256k1Seed().deriveKeyPair().privateKey();
      }
      default: {
        throw new RuntimeException("Unhandled VersionType: " + versionType);
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
