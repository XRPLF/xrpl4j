package org.xrpl.xrpl4j.crypto.bc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.github.benmanes.caffeine.cache.CaffeineSpec;
import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.addresses.VersionType;
import org.xrpl.xrpl4j.crypto.core.KeyMetadata;
import org.xrpl.xrpl4j.crypto.core.KeyStoreType;
import org.xrpl.xrpl4j.crypto.core.ServerSecret;
import org.xrpl.xrpl4j.crypto.core.ServerSecretSupplier;
import org.xrpl.xrpl4j.crypto.core.keys.Ed25519KeyPairService;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.core.keys.Secp256k1KeyPairService;
import org.xrpl.xrpl4j.crypto.core.keys.Seed;
import org.xrpl.xrpl4j.crypto.core.signing.Signature;
import org.xrpl.xrpl4j.crypto.core.signing.SignatureWithKeyMetadata;
import org.xrpl.xrpl4j.crypto.core.signing.SignatureWithPublicKey;
import org.xrpl.xrpl4j.crypto.core.signing.SingleSingedTransaction;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Unit tests for {@link DerivedKeyDelegatedSignatureService}.
 */
class DerivedKeyDelegatedSignatureServiceTest {

  private static final String sourceClassicAddressEd = "rLg3vY8w2tTZz1WVYjub32V4SGynLWnNRw";
  private static final String sourceClassicAddressEc = "rDt78kzcAfRf5NwmwL4f3E5pK14iM4CxRi";

  // Dest address
  private static final String destinationClassicAddress = "rKdi2esXfU7VmZyvRtMKZFFMVESBLE1iiw";

  private DerivedKeyDelegatedSignatureService edSignatureService;
  private DerivedKeyDelegatedSignatureService ecSignatureService;

  @BeforeEach
  public void setUp() {
    final ServerSecretSupplier serverSecretSupplier = () -> ServerSecret.of("happy".getBytes(StandardCharsets.UTF_8));
    this.edSignatureService = new DerivedKeyDelegatedSignatureService(serverSecretSupplier, VersionType.ED25519);
    this.ecSignatureService = new DerivedKeyDelegatedSignatureService(serverSecretSupplier, VersionType.SECP256K1);
  }

  @Test
  void constructorWithNulls() {
    // 2-arg Constructor
    assertThrows(NullPointerException.class, () -> new DerivedKeyDelegatedSignatureService(null, VersionType.ED25519));
    assertThrows(NullPointerException.class,
      () -> new DerivedKeyDelegatedSignatureService(() -> ServerSecret.of(new byte[32]), null));

    // 4-arg Constructor
    assertThrows(NullPointerException.class, () -> new DerivedKeyDelegatedSignatureService(
      null,
      VersionType.ED25519,
      mock(Ed25519KeyPairService.class),
      mock(Secp256k1KeyPairService.class)
    ));
    assertThrows(NullPointerException.class, () -> new DerivedKeyDelegatedSignatureService(
      () -> ServerSecret.of(new byte[32]),
      null,
      mock(Ed25519KeyPairService.class),
      mock(Secp256k1KeyPairService.class)
    ));
    assertThrows(NullPointerException.class, () -> new DerivedKeyDelegatedSignatureService(
      () -> ServerSecret.of(new byte[32]),
      VersionType.ED25519,
      null,
      mock(Secp256k1KeyPairService.class)
    ));
    assertThrows(NullPointerException.class, () -> new DerivedKeyDelegatedSignatureService(
      () -> ServerSecret.of(new byte[32]),
      VersionType.ED25519,
      mock(Ed25519KeyPairService.class),
      null
    ));

    // 5-arg Constructor
    assertThrows(NullPointerException.class, () -> new DerivedKeyDelegatedSignatureService(
      null,
      VersionType.ED25519,
      mock(Ed25519KeyPairService.class),
      mock(Secp256k1KeyPairService.class),
      CaffeineSpec.parse("")
    ));
    assertThrows(NullPointerException.class, () -> new DerivedKeyDelegatedSignatureService(
      () -> ServerSecret.of(new byte[32]),
      null,
      mock(Ed25519KeyPairService.class),
      mock(Secp256k1KeyPairService.class),
      CaffeineSpec.parse("")
    ));
    assertThrows(NullPointerException.class, () -> new DerivedKeyDelegatedSignatureService(
      () -> ServerSecret.of(new byte[32]),
      VersionType.ED25519,
      null,
      mock(Secp256k1KeyPairService.class),
      CaffeineSpec.parse("")
    ));
    assertThrows(NullPointerException.class, () -> new DerivedKeyDelegatedSignatureService(
      () -> ServerSecret.of(new byte[32]),
      VersionType.ED25519,
      mock(Ed25519KeyPairService.class),
      null,
      CaffeineSpec.parse("")
    ));
    assertThrows(NullPointerException.class, () -> new DerivedKeyDelegatedSignatureService(
      () -> ServerSecret.of(new byte[32]),
      VersionType.ED25519,
      mock(Ed25519KeyPairService.class),
      mock(Secp256k1KeyPairService.class),
      null
    ));

  }

  @Test
  void constructorWithExternalCaffeineConfig() {
    // This test merely assert that construction succeeds.
    new DerivedKeyDelegatedSignatureService(
      () -> ServerSecret.of(new byte[32]),
      VersionType.ED25519,
      mock(Ed25519KeyPairService.class),
      mock(Secp256k1KeyPairService.class),
      CaffeineSpec.parse("maximumSize=200,expireAfterWrite=300s")
    );
  }

  @Test
  void keyStoreType() {
    assertThat(edSignatureService.keyStoreType()).isEqualTo(KeyStoreType.DERIVED_SERVER_SECRET);
    assertThat(ecSignatureService.keyStoreType()).isEqualTo(KeyStoreType.DERIVED_SERVER_SECRET);
  }

  @Test
  void signAndVerifyEd() {
    final KeyMetadata keyMetadata = keyMetadata("foo");
    final PublicKey publicKey = this.edSignatureService.getPublicKey(keyMetadata);

    final Payment paymentTransaction = Payment.builder()
      .account(Address.of(sourceClassicAddressEd))
      .fee(XrpCurrencyAmount.ofDrops(10L))
      .sequence(UnsignedInteger.ONE)
      .destination(Address.of(destinationClassicAddress))
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .signingPublicKey(publicKey.hexValue())
      .build();

    final ExecutorService pool = Executors.newFixedThreadPool(5);
    final Callable<Boolean> signedTxCallable = () -> {
      SingleSingedTransaction<Payment> signedTx = this.edSignatureService.sign(keyMetadata, paymentTransaction);
      return this.edSignatureService.verify(
        SignatureWithKeyMetadata.builder()
          .transactionSignature(signedTx.signature())
          .signingKeyMetadata(keyMetadata)
          .build(),
        paymentTransaction
      );
    };

    final List<Future<Boolean>> futureSeeds = new ArrayList<>();
    for (int i = 0; i < 500; i++) {
      futureSeeds.add(pool.submit(signedTxCallable));
    }

    futureSeeds.stream()
      .map($ -> {
        try {
          return $.get();
        } catch (InterruptedException | ExecutionException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      })
      .forEach(validSig -> assertThat(validSig).isTrue());
  }

  @Test
  void multiSignEd() {
    final KeyMetadata keyMetadata = keyMetadata("foo");
    final Payment payment = Payment.builder()
      .account(Address.of(sourceClassicAddressEd))
      .amount(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(Address.of("rE7QZCdvs64wWr88f44R8q8kQtCQwefXFv"))
        .value("100")
        .build())
      .destination(Address.of(destinationClassicAddress))
      .fee(XrpCurrencyAmount.ofDrops(0))
      .flags(Flags.PaymentFlags.of(2147483648L))
      .lastLedgerSequence(UnsignedInteger.valueOf(4419079))
      .sequence(UnsignedInteger.valueOf(4101911))
      .signingPublicKey("")
      .build();

    final Signature expectedSignature =
      Signature.builder()
        .value(
          UnsignedByteArray.fromHex("E2ACD61C90D93433402B1F704DA38DF72876B6788C2C05B3196E14BC711AECFF14A7D6276439A1" +
            "98D8B4880EE2DB544CF351A8CE231B3340F42F9BF1EDBF5104")
        )
        .build();

    final ExecutorService pool = Executors.newFixedThreadPool(5);
    final Callable<Boolean> signedTxCallable = () -> {
      final Signature signature = this.edSignatureService.multiSign(keyMetadata, payment).transactionSignature();
      return signature.equals(expectedSignature);
    };

    final List<Future<Boolean>> futureSeeds = new ArrayList<>();
    for (int i = 0; i < 500; i++) {
      futureSeeds.add(pool.submit(signedTxCallable));
    }

    futureSeeds.stream()
      .map($ -> {
        try {
          return $.get();
        } catch (InterruptedException | ExecutionException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      })
      .forEach(validSig -> assertThat(validSig).isTrue());
  }

  @Test
  void multiSignEc() {
    final KeyMetadata keyMetadata = keyMetadata("foo");
    final Payment payment = Payment.builder()
      .account(Address.of(sourceClassicAddressEd))
      .amount(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(Address.of("rE7QZCdvs64wWr88f44R8q8kQtCQwefXFv"))
        .value("100")
        .build())
      .destination(Address.of(destinationClassicAddress))
      .fee(XrpCurrencyAmount.ofDrops(0))
      .flags(Flags.PaymentFlags.of(2147483648L))
      .lastLedgerSequence(UnsignedInteger.valueOf(4419079))
      .sequence(UnsignedInteger.valueOf(4101911))
      .signingPublicKey("")
      .build();

    final Signature expectedSignature =
      Signature.builder()
        .value(
          UnsignedByteArray.fromHex("3045022100CE46B9624BB33FF860A2C7334D7A898A43DE7796B94D0647AEA6E15A3F5752690220" +
            "7BDD66268EBE71A5D0A456B795268A5E27A569472875D65C710B13152C4484A3")
        )
        .build();

    final ExecutorService pool = Executors.newFixedThreadPool(5);
    final Callable<Boolean> signedTxCallable = () -> {
      Signature signature = this.ecSignatureService.multiSign(keyMetadata, payment).transactionSignature();
      return signature.equals(expectedSignature);
    };

    final List<Future<Boolean>> futureSeeds = new ArrayList<>();
    for (int i = 0; i < 500; i++) {
      futureSeeds.add(pool.submit(signedTxCallable));
    }

    futureSeeds.stream()
      .map($ -> {
        try {
          return $.get();
        } catch (InterruptedException | ExecutionException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      })
      .forEach(validSig -> assertThat(validSig).isTrue());
  }

  @Test
  void signAndVerifyEc() {
    final KeyMetadata keyMetadata = keyMetadata("foo");
    final PublicKey publicKey = this.ecSignatureService.getPublicKey(keyMetadata);

    final Payment paymentTransaction = Payment.builder()
      .account(Address.of(sourceClassicAddressEc))
      .fee(XrpCurrencyAmount.ofDrops(10L))
      .sequence(UnsignedInteger.ONE)
      .destination(Address.of(destinationClassicAddress))
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .signingPublicKey(publicKey.hexValue())
      .build();

    final ExecutorService pool = Executors.newFixedThreadPool(5);
    final Callable<Boolean> signedTxCallable = () -> {

      SignatureWithPublicKey signedTx = this.ecSignatureService.multiSign(keyMetadata, paymentTransaction);
      return this.ecSignatureService.verify(
        SignatureWithKeyMetadata.builder()
          .transactionSignature(signedTx.transactionSignature())
          .signingKeyMetadata(keyMetadata)
          .build(),
        paymentTransaction
      );
    };

    final List<Future<Boolean>> futureSeeds = new ArrayList<>();
    for (int i = 0; i < 300; i++) {
      futureSeeds.add(pool.submit(signedTxCallable));
    }

    futureSeeds.stream()
      .map($ -> {
        try {
          return $.get();
        } catch (InterruptedException | ExecutionException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      })
      .forEach(validSig -> assertThat(validSig).isTrue());
  }

  @Test
  void getPublicKeyEd() {
    PublicKey actualEcPublicKey = this.edSignatureService.getPublicKey(keyMetadata("ec_key"));
    assertThat(actualEcPublicKey.hexValue())
      .isEqualTo("EDFB5C6D87DACC6DCD852D7F1CE6914EDD2A82C1D7ECB9AF866E48A01D45E9E6DD");
    assertThat(actualEcPublicKey.base58Value()).isEqualTo("aKGg99sHNs3Vs5nUXKyjiv2ED73izdrDR2Pjy1mRY54WmxAkusZZ");
    assertThat(actualEcPublicKey.versionType()).isEqualTo(VersionType.ED25519);
  }

  @Test
  void getPublicKeyEc() {
    PublicKey actualEcPublicKey = this.ecSignatureService.getPublicKey(keyMetadata("ed_key"));
    assertThat(actualEcPublicKey.hexValue())
      .isEqualTo("0308C7F864BB4CA1B6598BF9BB0B538AB58AAB9B4E42E5C1A2A95136125711ACB2");
    assertThat(actualEcPublicKey.base58Value()).isEqualTo("aBPyf7q6qWdDbSWEvm47oQTotG7qtKPVFebfbR1u4aY73Z6roFCH");
    assertThat(actualEcPublicKey.versionType()).isEqualTo(VersionType.SECP256K1);
  }

  @Test
  void generateEd25519XrplSeed() {
    final ExecutorService pool = Executors.newFixedThreadPool(5);
    final Callable<Seed> seedCheck = () -> this.edSignatureService.generateEd25519XrplSeed("test_account");

    final List<Future<Seed>> futureSeeds = new ArrayList<>();
    for (int i = 0; i < 10000; i++) {
      futureSeeds.add(pool.submit(seedCheck));
    }

    futureSeeds.stream()
      .map($ -> {
        try {
          return $.get();
        } catch (InterruptedException | ExecutionException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      })
      .forEach(
        seed -> assertThat(seed.decodedSeed().bytes().hexValue()).isEqualTo("sEd7sYDb1EARo6GFwHFnW3ShnefjGKW"));
  }

  @Test
  void generateSecp256k1Seed() {
    final ExecutorService pool = Executors.newFixedThreadPool(5);
    final Callable<Seed> seedCheck = () -> this.ecSignatureService.generateSecp256k1Seed("test_account");

    final List<Future<Seed>> futureSeeds = new ArrayList<>();
    for (int i = 0; i < 10000; i++) {
      futureSeeds.add(pool.submit(seedCheck));
    }

    futureSeeds.stream()
      .map($ -> {
        try {
          return $.get();
        } catch (InterruptedException | ExecutionException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      })
      .forEach(seed -> assertThat(seed.decodedSeed().bytes().hexValue()).isEqualTo("shSZD6BGMy5Pv8RhtvDuVXZGGjt9m"));
  }

  //////////////////
  // Private Helpers
  //////////////////

  /**
   * Helper function to generate Key meta-data based upon the supplied inputs.
   *
   * @param keyIdentifier A {@link String} identifying the key.
   *
   * @return A {@link KeyMetadata}.
   */
  private KeyMetadata keyMetadata(final String keyIdentifier) {
    Objects.requireNonNull(keyIdentifier);

    return KeyMetadata.builder()
      .platformIdentifier("jks")
      .keyringIdentifier("n/a")
      .keyIdentifier(keyIdentifier)
      .keyVersion("1")
      .keyPassword("password")
      .build();
  }
}
