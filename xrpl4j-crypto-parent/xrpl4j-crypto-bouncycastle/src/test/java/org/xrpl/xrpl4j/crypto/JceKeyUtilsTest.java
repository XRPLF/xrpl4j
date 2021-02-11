package org.xrpl.xrpl4j.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.BaseEncoding;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.keypairs.Secp256k1;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * Unit tests for {@link JceKeyUtils}.
 */
public class JceKeyUtilsTest {

  /////////////////
  // EC Private Key
  /////////////////

  // BouncyCastle Encoding
  private static final String EC_PRIVATE_KEY_HEX = "D12D2FACA9AD92828D89683778CB8DFCCDBD6C9E92F6AB7D6065E8AACC1FF6D6";
  private static final BigInteger EC_PRIVATE_KEY_BIGINTEGER = new BigInteger(EC_PRIVATE_KEY_HEX, 16);

  // Java Encoding
  private static final String EC_PRIVATE_KEY_HEX_ENCODED =
    "303E020100301006072A8648CE3D020106052B8104000A042730250201010420" + EC_PRIVATE_KEY_HEX;

  // The binary representation of a secp256k1 is slightly different from
  private static final byte[] EC_PRIVATE_KEY_BYTES = BaseEncoding.base16().decode(EC_PRIVATE_KEY_HEX);
  private static final BigInteger EC_PRIVATE_KEY_BIGINTEGER2 = new BigInteger(1, EC_PRIVATE_KEY_BYTES);

  /////////////////
  // EC Public Key
  /////////////////

  // BouncyCastle Encoding.
  private static final String EC_PUBLIC_KEY_HEX
    = "04661BA57FED0D115222E30FE7E9509325EE30E7E284D3641E6FB5E67368C2DB185ADA8EFC5DC43AF6BF474A41ED6237573DC4ED693D4" +
    "9102C42FFC88510500799";

  // Java encoding
  private static final String EC_PUBLIC_KEY_HEX_ENCODED =
    "3056301006072A8648CE3D020106052B8104000A034200" + EC_PUBLIC_KEY_HEX;

  @BeforeEach
  public void setUp() {
    assertThat(EC_PRIVATE_KEY_BIGINTEGER).isEqualTo(EC_PRIVATE_KEY_BIGINTEGER2);
    assertThat(EC_PRIVATE_KEY_HEX).isEqualTo(EC_PRIVATE_KEY_BIGINTEGER.toString(16).toUpperCase());
  }

  ////////////////
  // EC PrivateKey
  ////////////////

  /**
   * Create a BouncyCastle private key, convert to XRPL-4j and then convert back to BouncyCastle and ensure the keys
   * match. Next, convert to {@link java.security.PrivateKey} and back and ensure everything is the same underlying
   * BigInteger.
   *
   * @see "https://stackoverflow.com/questions/61069182/derive-ec-public-key-from-private-key-string-in-native-java-
   *   for-curve-secp256k1"
   */
  @Test
  public void privateKeyBcToXrpl4jToJavaAndBack() throws NoSuchAlgorithmException, InvalidKeySpecException {
    final ECPrivateKeyParameters expectedBouncyCastlePrivateKey
      = new ECPrivateKeyParameters(EC_PRIVATE_KEY_BIGINTEGER, Secp256k1.ecDomainParameters);

    ////////////////
    // BC to Java Native
    ////////////////
    final KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", new BouncyCastleProvider());

    // Option1
    {
      final ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("secp256k1");
      final ECNamedCurveSpec params = new ECNamedCurveSpec("secp256k1", spec.getCurve(), spec.getG(), spec.getN());
      final KeySpec privKeySpec = new ECPrivateKeySpec(EC_PRIVATE_KEY_BIGINTEGER, params);
      final java.security.PrivateKey expectedPrivateKeyNative = keyFactory.generatePrivate(privKeySpec);
      final ECPrivateKey ecPrivateKeyNative = (ECPrivateKey) expectedPrivateKeyNative;
      assertThat(expectedBouncyCastlePrivateKey.getD()).isEqualTo(ecPrivateKeyNative.getS());
    }
    // Option2
    {
      final byte[] privateKeyFullBytes = BaseEncoding.base16().decode(EC_PRIVATE_KEY_HEX_ENCODED);
      final KeySpec privKeySpec = new PKCS8EncodedKeySpec(privateKeyFullBytes);
      final java.security.PrivateKey expectedPrivateKeyNative = keyFactory.generatePrivate(privKeySpec);
      final ECPrivateKey ecPrivateKeyNative = (ECPrivateKey) expectedPrivateKeyNative;
      assertThat(expectedBouncyCastlePrivateKey.getD()).isEqualTo(ecPrivateKeyNative.getS());

      final ECPrivateKeyParameters actualBcPrivateKey = JceKeyUtils.toEcPrivateKeyParameters(expectedPrivateKeyNative);
      assertThat(actualBcPrivateKey).isEqualToComparingFieldByField(expectedBouncyCastlePrivateKey);
    }
  }

  ///////////////
  // EC PublicKey
  ///////////////

  @Test
  public void derivePublicKey() {
    final ECPrivateKeyParameters bcPrivateKey
      = new ECPrivateKeyParameters(EC_PRIVATE_KEY_BIGINTEGER, Secp256k1.ecDomainParameters);
    assertThat(bcPrivateKey.getD()).isEqualTo(EC_PRIVATE_KEY_BIGINTEGER);

    ECPublicKeyParameters actualBcPublicKey = JceKeyUtils.toEcPublicKeyParameters(bcPrivateKey);
    String publicKeyBytes = BaseEncoding.base16().encode(actualBcPublicKey.getQ().getEncoded(false));
    assertThat(publicKeyBytes).isEqualTo(EC_PUBLIC_KEY_HEX);
  }

  @Test
  public void bcPublicKeyToJavaAndBack() {
    final ECPrivateKeyParameters bcPrivateKey
      = new ECPrivateKeyParameters(EC_PRIVATE_KEY_BIGINTEGER, Secp256k1.ecDomainParameters);
    assertThat(bcPrivateKey.getD()).isEqualTo(EC_PRIVATE_KEY_BIGINTEGER);
    final ECPublicKeyParameters bcPublicKey = JceKeyUtils.toEcPublicKeyParameters(bcPrivateKey);

    final java.security.PublicKey actualPublicKey = JceKeyUtils.toPublicKey(bcPublicKey);
    assertThat(BaseEncoding.base16().encode(actualPublicKey.getEncoded())).isEqualTo(EC_PUBLIC_KEY_HEX_ENCODED);

    final ECPublicKeyParameters bcPublicKey2 = JceKeyUtils.toEcPublicKeyParameters(actualPublicKey);
    assertThat(bcPublicKey2).isEqualToComparingFieldByField(bcPublicKey);
  }

  @Test
  public void javaPublicKeyToBc() {
    final ECPrivateKeyParameters bcPrivateKey
      = new ECPrivateKeyParameters(EC_PRIVATE_KEY_BIGINTEGER, Secp256k1.ecDomainParameters);
    assertThat(bcPrivateKey.getD()).isEqualTo(EC_PRIVATE_KEY_BIGINTEGER);
    final ECPublicKeyParameters bcPublicKey = JceKeyUtils.toEcPublicKeyParameters(bcPrivateKey);

    final PublicKey actualPublicKey = JceKeyUtils.toPublicKey(bcPublicKey);

    assertThat(BaseEncoding.base16().encode(actualPublicKey.getEncoded())).isEqualTo(EC_PUBLIC_KEY_HEX_ENCODED);
  }

}