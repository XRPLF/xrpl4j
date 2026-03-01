package org.xrpl.xrpl4j.crypto.mpt;

import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

/**
 * Utility class for secp256k1 elliptic curve operations using BouncyCastle.
 *
 * <p>This is a static utility class - all methods are static and no instances should be created.</p>
 *
 * <p>Size constants match the C implementation in mpt_utility.h.</p>
 */
public final class Secp256k1Operations {

  // ============================================================================
  // Size Constants (matching mpt_utility.h)
  // ============================================================================

  /**
   * Size of a SHA-256 half hash in bytes (kMPT_HALF_SHA_SIZE).
   */
  public static final int HALF_SHA_SIZE = 32;

  /**
   * Size of a compressed public key in bytes (kMPT_PUBKEY_SIZE).
   */
  public static final int PUBKEY_SIZE = 33;

  /**
   * Size of a private key in bytes (kMPT_PRIVKEY_SIZE).
   */
  public static final int PRIVKEY_SIZE = 32;

  /**
   * Size of a blinding factor in bytes (kMPT_BLINDING_FACTOR_SIZE).
   */
  public static final int BLINDING_FACTOR_SIZE = 32;

  /**
   * Size of a single ElGamal ciphertext component in bytes (kMPT_ELGAMAL_CIPHER_SIZE).
   */
  public static final int ELGAMAL_CIPHER_SIZE = 33;

  /**
   * Total size of an ElGamal ciphertext pair (c1 || c2) in bytes (kMPT_ELGAMAL_TOTAL_SIZE).
   */
  public static final int ELGAMAL_TOTAL_SIZE = 66;

  /**
   * Size of a Pedersen commitment in bytes (kMPT_PEDERSEN_COMMIT_SIZE).
   */
  public static final int PEDERSEN_COMMIT_SIZE = 33;

  /**
   * Size of a Schnorr proof in bytes (kMPT_SCHNORR_PROOF_SIZE).
   */
  public static final int SCHNORR_PROOF_SIZE = 65;

  /**
   * Size of an equality proof in bytes (kMPT_EQUALITY_PROOF_SIZE).
   */
  public static final int EQUALITY_PROOF_SIZE = 98;

  /**
   * Size of a Pedersen link proof in bytes (kMPT_PEDERSEN_LINK_SIZE).
   */
  public static final int PEDERSEN_LINK_SIZE = 195;

  /**
   * Size of a single bulletproof in bytes (kMPT_SINGLE_BULLETPROOF_SIZE).
   */
  public static final int SINGLE_BULLETPROOF_SIZE = 688;

  /**
   * Size of a double bulletproof in bytes (kMPT_DOUBLE_BULLETPROOF_SIZE).
   */
  public static final int DOUBLE_BULLETPROOF_SIZE = 754;

  // ============================================================================
  // Curve Parameters
  // ============================================================================

  private static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");
  private static final BigInteger CURVE_ORDER = CURVE_PARAMS.getN();

  /**
   * Private constructor to prevent instantiation.
   */
  private Secp256k1Operations() {
    // Static utility class
  }

  /**
   * Gets the generator point G of the secp256k1 curve.
   *
   * @return The generator point G.
   */
  public static ECPoint getG() {
    return CURVE_PARAMS.getG();
  }

  /**
   * Gets the curve order n.
   *
   * @return The curve order.
   */
  public static BigInteger getCurveOrder() {
    return CURVE_ORDER;
  }

  /**
   * Checks if a scalar is a valid private key `(1 ≤ scalar &lt; n)`.
   *
   * @param scalar The scalar to check.
   *
   * @return {@code true} if valid, {@code false} otherwise.
   */
  public static boolean isValidPrivateKey(BigInteger scalar) {
    Objects.requireNonNull(scalar, "scalar must not be null");
    return scalar.compareTo(BigInteger.ONE) >= 0 && scalar.compareTo(CURVE_ORDER) < 0;
  }

  /**
   * Multiplies the generator point G by a scalar.
   *
   * @param scalar The scalar multiplier.
   *
   * @return The resulting point (scalar * G).
   */
  public static ECPoint multiplyG(BigInteger scalar) {
    Objects.requireNonNull(scalar, "scalar must not be null");
    return CURVE_PARAMS.getG().multiply(scalar).normalize();
  }

  /**
   * Multiplies a point by a scalar.
   *
   * @param point  The point to multiply.
   * @param scalar The scalar multiplier.
   *
   * @return The resulting point (scalar * point).
   */
  public static ECPoint multiply(ECPoint point, BigInteger scalar) {
    Objects.requireNonNull(point, "point must not be null");
    Objects.requireNonNull(scalar, "scalar must not be null");
    return point.multiply(scalar).normalize();
  }

  /**
   * Adds two points.
   *
   * @param p1 The first point.
   * @param p2 The second point.
   *
   * @return The sum (p1 + p2).
   */
  public static ECPoint add(ECPoint p1, ECPoint p2) {
    Objects.requireNonNull(p1, "p1 must not be null");
    Objects.requireNonNull(p2, "p2 must not be null");
    return p1.add(p2).normalize();
  }

  /**
   * Negates a point.
   *
   * @param point The point to negate.
   *
   * @return The negated point (-point).
   */
  // TODO: Unit test
  public static ECPoint negate(ECPoint point) {
    Objects.requireNonNull(point, "point must not be null");
    return point.negate().normalize();
  }

  /**
   * Checks if two points are equal by comparing their compressed encodings.
   *
   * @param p1 The first point.
   * @param p2 The second point.
   *
   * @return {@code true} if the points are equal, {@code false} otherwise.
   */
  public static boolean pointsEqual(ECPoint p1, ECPoint p2) {
    Objects.requireNonNull(p1, "p1 must not be null");
    Objects.requireNonNull(p2, "p2 must not be null");
    byte[] encoded1 = p1.getEncoded(true);
    byte[] encoded2 = p2.getEncoded(true);
    return Arrays.equals(encoded1, encoded2);
  }

  /**
   * Serializes a point to compressed format (33 bytes).
   *
   * @param point The point to serialize.
   *
   * @return The compressed encoding.
   */
  public static byte[] serializeCompressed(ECPoint point) {
    Objects.requireNonNull(point, "point must not be null");
    return point.getEncoded(true);
  }

  /**
   * Deserializes a point from compressed format.
   *
   * @param encoded The compressed encoding (33 bytes).
   *
   * @return The decoded point.
   */
  public static ECPoint deserialize(byte[] encoded) {
    Objects.requireNonNull(encoded, "encoded must not be null");
    return CURVE_PARAMS.getCurve().decodePoint(encoded).normalize();
  }

  /**
   * Validates that c1 and c2 are valid curve points and can be serialized.
   *
   * <p>This mirrors the C function {@code mpt_serialize_ec_pair} which validates
   * that both points can be serialized successfully.</p>
   *
   * @param c1 The first ciphertext component (33 bytes compressed).
   * @param c2 The second ciphertext component (33 bytes compressed).
   *
   * @throws IllegalStateException if either point is invalid or cannot be deserialized.
   */
  public static void validateEcPair(byte[] c1, byte[] c2) {
    Objects.requireNonNull(c1, "c1 must not be null");
    Objects.requireNonNull(c2, "c2 must not be null");

    try {
      ECPoint point1 = deserialize(c1);
      if (point1.isInfinity()) {
        throw new IllegalStateException("Serialization failed: c1 is point at infinity");
      }
    } catch (IllegalArgumentException e) {
      throw new IllegalStateException("Serialization failed: c1 is not a valid curve point", e);
    }

    try {
      ECPoint point2 = deserialize(c2);
      if (point2.isInfinity()) {
        throw new IllegalStateException("Serialization failed: c2 is point at infinity");
      }
    } catch (IllegalArgumentException e) {
      throw new IllegalStateException("Serialization failed: c2 is not a valid curve point", e);
    }
  }

  // ============================================================================
  // Scalar Arithmetic Operations (for Bulletproofs)
  // ============================================================================

  /**
   * Adds two scalars modulo the curve order.
   *
   * @param a The first scalar (32 bytes, big-endian).
   * @param b The second scalar (32 bytes, big-endian).
   *
   * @return The sum (a + b) mod n as a 32-byte array.
   */
  public static byte[] scalarAdd(byte[] a, byte[] b) {
    Objects.requireNonNull(a, "a must not be null");
    Objects.requireNonNull(b, "b must not be null");
    BigInteger aInt = new BigInteger(1, a);
    BigInteger bInt = new BigInteger(1, b);
    BigInteger result = aInt.add(bInt).mod(CURVE_ORDER);
    return toBytes32(result);
  }

  /**
   * Multiplies two scalars modulo the curve order.
   *
   * @param a The first scalar (32 bytes, big-endian).
   * @param b The second scalar (32 bytes, big-endian).
   *
   * @return The product (a * b) mod n as a 32-byte array.
   */
  public static byte[] scalarMultiply(byte[] a, byte[] b) {
    Objects.requireNonNull(a, "a must not be null");
    Objects.requireNonNull(b, "b must not be null");
    BigInteger aInt = new BigInteger(1, a);
    BigInteger bInt = new BigInteger(1, b);
    BigInteger result = aInt.multiply(bInt).mod(CURVE_ORDER);
    return toBytes32(result);
  }

  /**
   * Computes the modular inverse of a scalar.
   *
   * @param a The scalar to invert (32 bytes, big-endian).
   *
   * @return The inverse a^(-1) mod n as a 32-byte array.
   *
   * @throws IllegalArgumentException if a is zero.
   */
  public static byte[] scalarInverse(byte[] a) {
    Objects.requireNonNull(a, "a must not be null");
    BigInteger aInt = new BigInteger(1, a);
    if (aInt.equals(BigInteger.ZERO)) {
      throw new IllegalArgumentException("Cannot invert zero");
    }
    BigInteger result = aInt.modInverse(CURVE_ORDER);
    return toBytes32(result);
  }

  /**
   * Negates a scalar modulo the curve order.
   *
   * @param a The scalar to negate (32 bytes, big-endian).
   *
   * @return The negation (-a) mod n as a 32-byte array.
   */
  public static byte[] scalarNegate(byte[] a) {
    Objects.requireNonNull(a, "a must not be null");
    BigInteger aInt = new BigInteger(1, a);
    BigInteger result = CURVE_ORDER.subtract(aInt).mod(CURVE_ORDER);
    return toBytes32(result);
  }

  /**
   * Checks if a scalar is valid (non-zero and less than curve order).
   *
   * @param scalar The scalar to check (32 bytes, big-endian).
   *
   * @return {@code true} if valid, {@code false} otherwise.
   */
  public static boolean isValidScalar(byte[] scalar) {
    Objects.requireNonNull(scalar, "scalar must not be null");
    if (scalar.length != 32) {
      return false;
    }
    BigInteger scalarInt = new BigInteger(1, scalar);
    return scalarInt.compareTo(BigInteger.ZERO) > 0 && scalarInt.compareTo(CURVE_ORDER) < 0;
  }

  /**
   * Reduces a 32-byte hash to a valid scalar (mod curve order).
   */
  public static byte[] reduceToScalar(byte[] hash) {
    BigInteger hashInt = new BigInteger(1, hash);
    BigInteger reduced = hashInt.mod(Secp256k1Operations.getCurveOrder());
    return Secp256k1Operations.toBytes32(reduced);
  }

  public static int appendPoint(byte[] buffer, int offset, ECPoint point) {
    byte[] pointBytes = Secp256k1Operations.serializeCompressed(point);
    System.arraycopy(pointBytes, 0, buffer, offset, 33);
    return offset + 33;
  }


  /**
   * Returns the scalar representing (n - 1), which is equivalent to -1 mod n.
   *
   * @return A 32-byte array representing -1 mod n.
   */
  public static byte[] scalarMinusOne() {
    return toBytes32(CURVE_ORDER.subtract(BigInteger.ONE));
  }

  /**
   * Gets the infinity point (point at infinity / identity element).
   *
   * @return The infinity point.
   */
  public static ECPoint getInfinity() {
    return CURVE_PARAMS.getCurve().getInfinity();
  }

  // ////////////////////////////
  // Generic BigNumber Operations
  // ////////////////////////////

  // TODO: Extract unit test for these into dedicated test class.
  // TODO: Find other Secp and BigNumber operations and see if we can combine math into one place in the project.

  /**
   * Converts a BigInteger to a 32-byte big-endian array, padding with leading zeros if necessary.
   *
   * @param value The BigInteger to convert.
   *
   * @return A 32-byte array.
   */
  public static byte[] toBytes32(BigInteger value) {
    byte[] bytes = value.toByteArray();
    if (bytes.length == 32) {
      return bytes;
    } else if (bytes.length > 32) {
      // Remove leading zero byte if present (from sign bit)
      byte[] result = new byte[32];
      System.arraycopy(bytes, bytes.length - 32, result, 0, 32);
      return result;
    } else {
      // Pad with leading zeros
      byte[] result = new byte[32];
      System.arraycopy(bytes, 0, result, 32 - bytes.length, bytes.length);
      return result;
    }
  }

  /**
   * Converts an UnsignedLong value to a 32-byte scalar.
   *
   * @param value The UnsignedLong value.
   *
   * @return A 32-byte big-endian representation.
   */
  public static byte[] unsignedLongToScalar(UnsignedLong value) {
    Objects.requireNonNull(value, "value must not be null");
    byte[] result = new byte[32];
    long longValue = value.longValue();
    for (int i = 0; i < 8; i++) {
      result[31 - i] = (byte) ((longValue >> (i * 8)) & 0xFF);
    }
    return result;
  }

  /**
   * Returns the scalar representing zero.
   *
   * @return A 32-byte array of zeros.
   */
  public static byte[] scalarZero() {
    return new byte[32];
  }

  /**
   * Returns the scalar representing one.
   *
   * @return A 32-byte array representing 1.
   */
  public static byte[] scalarOne() {
    byte[] result = new byte[32];
    result[31] = 0x01;
    return result;
  }

  /**
   * Serializes a point to uncompressed format without the 0x04 prefix (64 bytes).
   *
   * <p>Returns the raw X and Y coordinates concatenated (32 bytes each).</p>
   *
   * @param point The point to serialize.
   *
   * @return The uncompressed encoding without prefix (64 bytes).
   *
   * @throws NullPointerException if point is null.
   */
  public static byte[] serializeUncompressedWithoutPrefix(ECPoint point) {
    Objects.requireNonNull(point, "point must not be null");
    byte[] uncompressedWithPrefix = point.getEncoded(false);
    byte[] result = new byte[64];
    System.arraycopy(uncompressedWithPrefix, 1, result, 0, 64);
    return result;
  }

  // ============================================================================
  // PublicKey / PrivateKey Conversion Utilities
  // ============================================================================

  /**
   * Converts a {@link PublicKey} to an {@link ECPoint}.
   *
   * <p>This method extracts the compressed public key bytes and deserializes them
   * to an EC point on the secp256k1 curve.</p>
   *
   * @param publicKey The public key to convert.
   *
   * @return The corresponding EC point.
   *
   * @throws NullPointerException if publicKey is null.
   */
  public static ECPoint toEcPoint(PublicKey publicKey) {
    Objects.requireNonNull(publicKey, "publicKey must not be null");
    return deserialize(publicKey.value().toByteArray());
  }

  /**
   * Converts a {@link PrivateKey} to a {@link BigInteger} scalar.
   *
   * <p>This method extracts the natural (unprefixed) private key bytes and converts
   * them to a BigInteger for use in elliptic curve operations.</p>
   *
   * @param privateKey The private key to convert.
   *
   * @return The private key as a BigInteger scalar.
   *
   * @throws NullPointerException if privateKey is null.
   */
  public static BigInteger toScalar(PrivateKey privateKey) {
    Objects.requireNonNull(privateKey, "privateKey must not be null");
    return new BigInteger(1, privateKey.naturalBytes().toByteArray());
  }
}
