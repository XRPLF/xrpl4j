package org.xrpl.xrpl4j.crypto.signing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.github.benmanes.caffeine.cache.CaffeineSpec;
import com.google.common.primitives.UnsignedInteger;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.addresses.VersionType;
import org.xrpl.xrpl4j.crypto.KeyMetadata;
import org.xrpl.xrpl4j.crypto.KeyStoreType;
import org.xrpl.xrpl4j.crypto.PublicKey;
import org.xrpl.xrpl4j.crypto.Seed;
import org.xrpl.xrpl4j.crypto.ServerSecretSupplier;
import org.xrpl.xrpl4j.keypairs.KeyPairService;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Unit tests for {@link DerivedKeysSignatureService}.
 */
class DerivedKeysSignatureServiceTest {

  private static final String SECP256K1 = "secp256k1";
  private static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName(SECP256K1);
  private static final ECDomainParameters EC_CURVE =
    new ECDomainParameters(
      CURVE_PARAMS.getCurve(),
      CURVE_PARAMS.getG(),
      CURVE_PARAMS.getN(),
      CURVE_PARAMS.getH()
    );

  // Source ed25519 Key
  // 32 Bytes (no XRPL prefix here)
  private static final String ED_PRIVATE_KEY_HEX = "60F72F359647AD376D2CB783340CD843BD57CCD46093AA16B0C4D3A5143BADC5";
  private ECPrivateKeyParameters knownEcPrivateKeyParameters;
  private static final String sourceClassicAddressEd = "rLg3vY8w2tTZz1WVYjub32V4SGynLWnNRw";

  // Source secp256k1 Key
  // 33 Bytes
  private static final String EC_PRIVATE_KEY_HEX = "0093CC77E2333958D1480FC36811A68A1785258F65251DE100012FA18D0186FFB0";
  private Ed25519PrivateKeyParameters knownEd25519PrivateKeyParameters;
  private static final String sourceClassicAddressEc = "rDt78kzcAfRf5NwmwL4f3E5pK14iM4CxRi";

  // Dest address
  private static final String destinationClassicAddress = "rKdi2esXfU7VmZyvRtMKZFFMVESBLE1iiw";

  private DerivedKeysSignatureService edSignatureService;
  private DerivedKeysSignatureService ecSignatureService;

  @BeforeEach
  public void setUp() {
    final ServerSecretSupplier serverSecretSupplier = "happy"::getBytes;
    this.edSignatureService = new DerivedKeysSignatureService(serverSecretSupplier, VersionType.ED25519);
    this.ecSignatureService = new DerivedKeysSignatureService(serverSecretSupplier, VersionType.SECP256K1);
  }

  @Test
  void constructorWithNulls() {
    // 2-arg Constructor
    assertThrows(NullPointerException.class, () -> new DerivedKeysSignatureService(null, VersionType.ED25519));
    assertThrows(NullPointerException.class, () -> new DerivedKeysSignatureService(() -> new byte[32], null));

    // 3-arg Constructor
    assertThrows(NullPointerException.class, () -> new DerivedKeysSignatureService(
      null,
      VersionType.ED25519,
      mock(KeyPairService.class)
    ));
    assertThrows(NullPointerException.class, () -> new DerivedKeysSignatureService(
      () -> new byte[32],
      null,
      mock(KeyPairService.class)
    ));
    assertThrows(NullPointerException.class, () -> new DerivedKeysSignatureService(
      () -> new byte[32],
      VersionType.ED25519,
      null
    ));

    // 4-arg Constructor
    assertThrows(NullPointerException.class, () -> new DerivedKeysSignatureService(
      null,
      VersionType.ED25519,
      mock(KeyPairService.class),
      CaffeineSpec.parse("")
    ));
    assertThrows(NullPointerException.class, () -> new DerivedKeysSignatureService(
      () -> new byte[32],
      null,
      mock(KeyPairService.class),
      CaffeineSpec.parse("")
    ));
    assertThrows(NullPointerException.class, () -> new DerivedKeysSignatureService(
      () -> new byte[32],
      VersionType.ED25519,
      null,
      CaffeineSpec.parse("")
    ));
    assertThrows(NullPointerException.class, () -> new DerivedKeysSignatureService(
      () -> new byte[32],
      VersionType.ED25519,
      mock(KeyPairService.class),
      null
    ));

  }

  @Test
  void constructorWithExternalCaffeineConfig() {
    // This test merely assert that construction succeeds.
    new DerivedKeysSignatureService(
      () -> new byte[32],
      VersionType.ED25519,
      mock(KeyPairService.class),
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
      .signingPublicKey(publicKey.base16Encoded())
      .build();

    final ExecutorService pool = Executors.newFixedThreadPool(5);
    final Callable<Boolean> signedTxCallable = () -> {
      SignedTransaction<Payment> signedTx = this.edSignatureService.sign(keyMetadata, paymentTransaction);
      return this.edSignatureService.verify(keyMetadata, signedTx);
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
      Signature signature = this.edSignatureService.signWithBehavior(keyMetadata, payment, SigningBehavior.MULTI);
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
      Signature signature = this.ecSignatureService.signWithBehavior(keyMetadata, payment, SigningBehavior.MULTI);
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
      .signingPublicKey(publicKey.base16Encoded())
      .build();

    final ExecutorService pool = Executors.newFixedThreadPool(5);
    final Callable<Boolean> signedTxCallable = () -> {
      SignedTransaction<Payment> signedTx = this.ecSignatureService.sign(keyMetadata, paymentTransaction);
      return this.ecSignatureService.verify(keyMetadata, signedTx);
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
    assertThat(actualEcPublicKey.base16Encoded())
      .isEqualTo("EDFB5C6D87DACC6DCD852D7F1CE6914EDD2A82C1D7ECB9AF866E48A01D45E9E6DD");
    assertThat(actualEcPublicKey.base58Encoded()).isEqualTo("aKGg99sHNs3Vs5nUXKyjiv2ED73izdrDR2Pjy1mRY54WmxAkusZZ");
    assertThat(actualEcPublicKey.versionType()).isEqualTo(VersionType.ED25519);
  }

  @Test
  void getPublicKeyEc() {
    PublicKey actualEcPublicKey = this.ecSignatureService.getPublicKey(keyMetadata("ed_key"));
    assertThat(actualEcPublicKey.base16Encoded())
      .isEqualTo("0308C7F864BB4CA1B6598BF9BB0B538AB58AAB9B4E42E5C1A2A95136125711ACB2");
    assertThat(actualEcPublicKey.base58Encoded()).isEqualTo("aBPyf7q6qWdDbSWEvm47oQTotG7qtKPVFebfbR1u4aY73Z6roFCH");
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
      .forEach(seed -> assertThat(seed.value()).isEqualTo("sEd7sYDb1EARo6GFwHFnW3ShnefjGKW"));
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
      .forEach(seed -> assertThat(seed.value()).isEqualTo("shSZD6BGMy5Pv8RhtvDuVXZGGjt9m"));
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
