package org.xrpl.xrpl4j.crypto.signing;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedInteger;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.binary.XrplBinaryCodec;
import org.xrpl.xrpl4j.crypto.KeyMetadata;
import org.xrpl.xrpl4j.crypto.PublicKey;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Unit tests for {@link SoftHsmSignatureService}.
 */
public class SoftHsmSignatureServiceTest {

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
  private static final String sourceClassicAddressED = "rLg3vY8w2tTZz1WVYjub32V4SGynLWnNRw";

  // Source secp256k1 Key
  // 33 Bytes
  private static final String EC_PRIVATE_KEY_HEX = "0093CC77E2333958D1480FC36811A68A1785258F65251DE100012FA18D0186FFB0";
  private static final String sourceClassicAddressEC = "rDt78kzcAfRf5NwmwL4f3E5pK14iM4CxRi";
  // private static final String sourceSecret = "sh5U34QGnsduZBnANjLxvwqcydYsE";

  // Dest address
  private static final String destinationClassicAddress = "rKdi2esXfU7VmZyvRtMKZFFMVESBLE1iiw";
  // private static final String destinationSecret = "snvSCVszQz3bLfPaGNqcU2bM7PsVR";

  private SignatureUtils signatureUtils;
  private SoftHsmSignatureService signer;

  // These are the BC equivalents of our know keys, as taken from the bytes above.
  private ECPrivateKeyParameters knownEcPrivateKeyParameters;
  private Ed25519PrivateKeyParameters knownEdPrivateKeyParameters;

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  @BeforeEach
  public void setUp() {
    this.signatureUtils = new SignatureUtils(ObjectMapperFactory.create(), new XrplBinaryCodec());

    this.knownEcPrivateKeyParameters = new ECPrivateKeyParameters(
      new BigInteger(EC_PRIVATE_KEY_HEX, 16), EC_CURVE
    );

    this.knownEdPrivateKeyParameters = new Ed25519PrivateKeyParameters(
      BaseEncoding.base16().decode(ED_PRIVATE_KEY_HEX), 0
    );
  }

  /**
   * Sign and verify a payment transaction using a newly generated secp256k1 EC public/private key-pair.
   */
  @Test
  void signAndVerifyWithKnownEdKeys() {
    this.signer = new SoftHsmSignatureService(
      signatureUtils,
      knownEdPrivateKeyParameters, // <-- Use known value.
      knownEcPrivateKeyParameters // <-- Unused in this test.
    );

    final KeyMetadata signingKeyMetadata = keyMetadata(SoftHsmSignatureService.KEY_ID_ED25519);
    final PublicKey publicKey = this.signer.getPublicKey(signingKeyMetadata);

    final Payment paymentTransaction = Payment.builder()
      .account(Address.of(sourceClassicAddressED))
      .fee(XrpCurrencyAmount.ofDrops(10L))
      .sequence(UnsignedInteger.ONE)
      .destination(Address.of(destinationClassicAddress))
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .signingPublicKey(publicKey.base16Encoded())
      .build();

    final SignedTransaction result = signer.sign(signingKeyMetadata, paymentTransaction);
    assertThat(result).isNotNull();
    assertThat(result.unsignedTransaction()).isEqualTo(paymentTransaction);

    final boolean signatureResult = signer.verify(signingKeyMetadata, result);
    assertThat(signatureResult).isTrue();
  }

  @Test
  void signAndVerifyWithNewEd25519Keys() {
    // Generates new keys internally on every run...
    this.signer = new SoftHsmSignatureService(signatureUtils);

    final KeyMetadata signingKeyMetadata = keyMetadata(SoftHsmSignatureService.KEY_ID_ED25519);
    final PublicKey publicKey = this.signer.getPublicKey(signingKeyMetadata);

    final Payment paymentTransaction = Payment.builder()
      .account(Address.of(sourceClassicAddressEC))
      .fee(XrpCurrencyAmount.ofDrops(10L))
      .sequence(UnsignedInteger.ONE)
      .destination(Address.of(destinationClassicAddress))
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .signingPublicKey(publicKey.base16Encoded())
      .build();

    final SignedTransaction result = signer.sign(signingKeyMetadata, paymentTransaction);
    assertThat(result).isNotNull();
    assertThat(result.unsignedTransaction()).isEqualTo(paymentTransaction);

    final boolean signatureResult = signer.verify(signingKeyMetadata, result);
    assertThat(signatureResult).isTrue();
  }

  /**
   * Sign and verify a payment transaction using a known secp256k1 EC public/private key-pair.
   */
  @Test
  void signAndVerifyWithKnownEcKeys() {
    this.signer = new SoftHsmSignatureService(
      signatureUtils,
      knownEdPrivateKeyParameters, // <-- Unused in this test.
      knownEcPrivateKeyParameters // <-- Use known value.
    );

    final KeyMetadata signingKeyMetadata = keyMetadata(SoftHsmSignatureService.KEY_ID_SECP256K1);
    final PublicKey publicKey = this.signer.getPublicKey(signingKeyMetadata);

    final Payment paymentTransaction = Payment.builder()
      .account(Address.of(sourceClassicAddressEC))
      .fee(XrpCurrencyAmount.ofDrops(10L))
      .sequence(UnsignedInteger.ONE)
      .destination(Address.of(destinationClassicAddress))
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .signingPublicKey(publicKey.base16Encoded())
      .build();

    final SignedTransaction result = signer.sign(signingKeyMetadata, paymentTransaction);
    assertThat(result).isNotNull();
    assertThat(result.unsignedTransaction()).isEqualTo(paymentTransaction);

    final boolean signatureResult = signer.verify(signingKeyMetadata, result);
    assertThat(signatureResult).isTrue();
  }

  /**
   * Sign and verify a payment transaction using a newly generated secp256k1 EC public/private key-pair.
   */
  @Test
  void signAndVerifyWithNewEcKeys() {
    // Generates new keys internally on every run...
    this.signer = new SoftHsmSignatureService(signatureUtils);

    final KeyMetadata signingKeyMetadata = this.keyMetadata(SoftHsmSignatureService.KEY_ID_SECP256K1);
    final PublicKey publicKey = this.signer.getPublicKey(signingKeyMetadata);

    final Payment paymentTransaction = Payment.builder()
      .account(Address.of(sourceClassicAddressEC))
      .fee(XrpCurrencyAmount.ofDrops(10L))
      .sequence(UnsignedInteger.ONE)
      .destination(Address.of(destinationClassicAddress))
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .signingPublicKey(publicKey.base16Encoded())
      .build();

    final SignedTransaction result = signer.sign(signingKeyMetadata, paymentTransaction);
    assertThat(result).isNotNull();
    assertThat(result.unsignedTransaction()).isEqualTo(paymentTransaction);

    final boolean signatureResult = signer.verify(signingKeyMetadata, result);
    assertThat(signatureResult).isTrue();
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
