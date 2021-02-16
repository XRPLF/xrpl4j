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
import org.xrpl.xrpl4j.codec.addresses.VersionType;
import org.xrpl.xrpl4j.crypto.BcKeyUtils;
import org.xrpl.xrpl4j.crypto.KeyMetadata;
import org.xrpl.xrpl4j.crypto.PublicKey;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Unit tests for {@link SingleKeySignatureService}.
 */
class SingleKeySignatureServiceTest {

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
  private static final String sourceClassicAddressED = "rLg3vY8w2tTZz1WVYjub32V4SGynLWnNRw";

  // Source secp256k1 Key
  // 33 Bytes
  private static final String EC_PRIVATE_KEY_HEX = "0093CC77E2333958D1480FC36811A68A1785258F65251DE100012FA18D0186FFB0";
  private Ed25519PrivateKeyParameters knownEd25519PrivateKeyParameters;
  private static final String sourceClassicAddressEC = "rDt78kzcAfRf5NwmwL4f3E5pK14iM4CxRi";

  // Dest address
  private static final String destinationClassicAddress = "rKdi2esXfU7VmZyvRtMKZFFMVESBLE1iiw";
  // private static final String destinationSecret = "snvSCVszQz3bLfPaGNqcU2bM7PsVR";

  private SingleKeySignatureService edSignatureService;
  private SingleKeySignatureService ecSignatureService;

  @BeforeEach
  public void setUp() {
    this.knownEd25519PrivateKeyParameters = new Ed25519PrivateKeyParameters(
      BaseEncoding.base16().decode(ED_PRIVATE_KEY_HEX), 0
    );
    this.edSignatureService = new SingleKeySignatureService(BcKeyUtils.toPrivateKey(knownEd25519PrivateKeyParameters));

    this.knownEcPrivateKeyParameters = new ECPrivateKeyParameters(
      new BigInteger(EC_PRIVATE_KEY_HEX, 16), EC_CURVE
    );
    this.ecSignatureService = new SingleKeySignatureService(BcKeyUtils.toPrivateKey(knownEcPrivateKeyParameters));

  }

  @Test
  void getPublicKeyEc() {
    PublicKey actualEcPublicKey = this.ecSignatureService.getPublicKey(keyMetadata("single_key"));
    assertThat(actualEcPublicKey.base16Encoded())
      .isEqualTo("0378272C2A8F6146FE94BA3D116F548179A9875CBBD52E9D9B91A0FA44AEC4684D");
    assertThat(actualEcPublicKey.base58Encoded()).isEqualTo("aBQFwK1G6ErqTM52SMAT5f6qaj4ARazaEw5fKHRbR1tYy5djdWAu");
    assertThat(actualEcPublicKey.versionType()).isEqualTo(VersionType.SECP256K1);
  }

  @Test
  void getPublicKeyEd() {
    PublicKey actualEcPublicKey = this.edSignatureService.getPublicKey(keyMetadata("single_key"));
    assertThat(actualEcPublicKey.base16Encoded())
      .isEqualTo("ED63DDD6F90FB4D4787972B8CD3FB960CDA53ED9F18A86BDAAFB4D8C2E8828C06A");
    assertThat(actualEcPublicKey.base58Encoded()).isEqualTo("aKEXRRa9n8iaCuiWBiUWniXBfoohrj8nR6E5pJ6DCB721WpCN65E");
    assertThat(actualEcPublicKey.versionType()).isEqualTo(VersionType.ED25519);
  }

  @Test
  void edDsaSignAndVerify() {
    final KeyMetadata keyMetadata = keyMetadata("foo");
    final PublicKey publicKey = this.edSignatureService.getPublicKey(keyMetadata);

    final Payment paymentTransaction = Payment.builder()
      .account(Address.of(sourceClassicAddressED))
      .fee(XrpCurrencyAmount.ofDrops(10L))
      .sequence(UnsignedInteger.ONE)
      .destination(Address.of(destinationClassicAddress))
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .signingPublicKey(publicKey.base16Encoded())
      .build();

    SignedTransaction transactionWithSignature = this.edSignatureService.sign(keyMetadata, paymentTransaction);
    assertThat(transactionWithSignature).isNotNull();
    assertThat(transactionWithSignature.unsignedTransaction()).isEqualTo(paymentTransaction);

    final boolean signatureResult = edSignatureService.verify(keyMetadata, transactionWithSignature);
    assertThat(signatureResult).isTrue();
  }

  @Test
  void ecDsaSignAndVerify() {
    final KeyMetadata keyMetadata = keyMetadata("foo");
    final PublicKey publicKey = this.ecSignatureService.getPublicKey(keyMetadata);

    final Payment paymentTransaction = Payment.builder()
      .account(Address.of(sourceClassicAddressEC))
      .fee(XrpCurrencyAmount.ofDrops(10L))
      .sequence(UnsignedInteger.ONE)
      .destination(Address.of(destinationClassicAddress))
      .amount(XrpCurrencyAmount.ofDrops(12345))
      .signingPublicKey(publicKey.base16Encoded())
      .build();

    SignedTransaction transactionWithSignature = this.ecSignatureService.sign(keyMetadata, paymentTransaction);
    assertThat(transactionWithSignature).isNotNull();
    assertThat(transactionWithSignature.unsignedTransaction()).isEqualTo(paymentTransaction);

    final boolean signatureResult = ecSignatureService.verify(keyMetadata, transactionWithSignature);
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