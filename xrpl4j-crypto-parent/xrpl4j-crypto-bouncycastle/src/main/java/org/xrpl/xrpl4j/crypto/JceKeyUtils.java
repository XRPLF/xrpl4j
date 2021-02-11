package org.xrpl.xrpl4j.crypto;

import com.google.common.base.Preconditions;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.params.ECNamedDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.ECPointUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.keypairs.Secp256k1;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.util.Objects;

/**
 * Utility class for converting between XRPL-4j public/private keys and BouncyCastle implementations.
 */
@SuppressWarnings("unused")
public final class JceKeyUtils {

  static {
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
      final BouncyCastleProvider bcProvider = new BouncyCastleProvider();
      if (Security.addProvider(bcProvider) == -1) {
        throw new RuntimeException("Could not configure BouncyCastle provider");
      }
    }
  }

  private static final String SECP256K1 = "secp256k1";

  private static final ECNamedCurveParameterSpec EC_PARAMS = ECNamedCurveTable.getParameterSpec(SECP256K1);
  private static final ECNamedCurveSpec EC_NAMED_CURVE = new ECNamedCurveSpec(
    EC_PARAMS.getName(),
    EC_PARAMS.getCurve(),
    EC_PARAMS.getG(),
    EC_PARAMS.getN()
  );

  private static final String ECDSA = "ECDSA";

  /**
   * Convert a {@link ECPrivateKeyParameters} to a {@link java.security.PrivateKey}.
   *
   * @param ecPrivateKeyParameters A {@link ECPrivateKeyParameters}.
   *
   * @return A {@link java.security.PrivateKey}.
   */
  public static java.security.PrivateKey toPrivateKey(final ECPrivateKeyParameters ecPrivateKeyParameters) {
    Objects.requireNonNull(ecPrivateKeyParameters);
    try {
      KeyFactory kf = KeyFactory.getInstance(ECDSA, new BouncyCastleProvider());
      ECPrivateKeySpec ecPrivateKeySpec = new ECPrivateKeySpec(ecPrivateKeyParameters.getD(), EC_NAMED_CURVE);
      return kf.generatePrivate(ecPrivateKeySpec);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Convert a {@link PrivateKey} to a {@link ECPrivateKeyParameters}.
   *
   * @param privateKey A {@link PrivateKey}.
   *
   * @return A {@link ECPrivateKeyParameters}.
   */
  public static ECPrivateKeyParameters toEcPrivateKeyParameters(final java.security.PrivateKey privateKey) {
    Objects.requireNonNull(privateKey);
    Preconditions
      .checkArgument(privateKey instanceof ECPrivateKey, "PrivateKey input must be an instance of ECPrivateKey");

    final ECPrivateKey ecPrivateKey = (ECPrivateKey) privateKey;
    return new ECPrivateKeyParameters(ecPrivateKey.getS(), Secp256k1.ecDomainParameters);
  }

  /**
   * Convert a {@link ECPrivateKeyParameters} to a {@link ECPublicKeyParameters}.
   *
   * @param ecPrivateKeyParameters A {@link ECPrivateKeyParameters}.
   *
   * @return A {@link ECPublicKeyParameters}.
   */
  public static ECPublicKeyParameters toEcPublicKeyParameters(final ECPrivateKeyParameters ecPrivateKeyParameters) {
    Objects.requireNonNull(ecPrivateKeyParameters);
    final ECPoint q = ecPrivateKeyParameters.getParameters().getG().multiply(ecPrivateKeyParameters.getD());
    return new ECPublicKeyParameters(q, ecPrivateKeyParameters.getParameters());
  }

  /**
   * Convert a {@link PublicKey} to a {@link ECPublicKeyParameters}.
   *
   * @param publicKey A {@link PublicKey}.
   *
   * @return A {@link ECPublicKeyParameters}.
   */
  @SuppressWarnings("checkstyle:LocalVariableName")
  public static ECPublicKeyParameters toEcPublicKeyParameters(final java.security.PublicKey publicKey) {
    Objects.requireNonNull(publicKey);
    Preconditions.checkArgument(publicKey instanceof ECPublicKey, "PublicKey input must be an instance of ECPublicKey");

    final ECPublicKey ecPublicKey = (ECPublicKey) publicKey;
    byte[] x = ecPublicKey.getW().getAffineX().toByteArray();
    byte[] y = ecPublicKey.getW().getAffineY().toByteArray();

    // assumes that x and y are (unsigned) big endian encoded
    BigInteger xbi = new BigInteger(1, x);
    BigInteger ybi = new BigInteger(1, y);
    X9ECParameters x9 = org.bouncycastle.asn1.x9.ECNamedCurveTable.getByName(SECP256K1);
    ASN1ObjectIdentifier oid = org.bouncycastle.asn1.x9.ECNamedCurveTable.getOID(SECP256K1);
    ECCurve curve = x9.getCurve();
    ECPoint point = curve.createPoint(xbi, ybi);
    ECNamedDomainParameters dParams = new ECNamedDomainParameters(oid,
      x9.getCurve(), x9.getG(), x9.getN(), x9.getH(), x9.getSeed());
    return new ECPublicKeyParameters(point, dParams);
  }

  /**
   * Convert a {@link ECPublicKeyParameters} to a {@link PublicKey}.
   *
   * @param ecPublicKeyParameters A {@link ECPublicKeyParameters}.
   *
   * @return A {@link java.security.PublicKey}.
   */
  public static java.security.PublicKey toPublicKey(final ECPublicKeyParameters ecPublicKeyParameters) {
    try {
      java.security.spec.ECPoint point = ECPointUtil.decodePoint(
        EC_NAMED_CURVE.getCurve(), ecPublicKeyParameters.getQ().getEncoded(false)
      );
      ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(point, EC_NAMED_CURVE);
      KeyFactory kf = KeyFactory.getInstance(ECDSA, new BouncyCastleProvider());
      return kf.generatePublic(pubKeySpec);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * No-args Constructor to prevent instantiation.
   */
  private JceKeyUtils() {
  }

}
