package org.xrpl.xrpl4j.crypto.mpt;

import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

/**
 * Utility class for secp256k1 elliptic curve operations using BouncyCastle.
 */
public class Secp256k1Operations {

  private static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");
  private static final BigInteger CURVE_ORDER = CURVE_PARAMS.getN();

  /**
   * Gets the generator point G of the secp256k1 curve.
   *
   * @return The generator point G.
   */
  public ECPoint getG() {
    return CURVE_PARAMS.getG();
  }

  /**
   * Gets the curve order n.
   *
   * @return The curve order.
   */
  public BigInteger getCurveOrder() {
    return CURVE_ORDER;
  }

  /**
   * Checks if a scalar is a valid private key `(1 ≤ scalar &lt; n)`.
   *
   * @param scalar The scalar to check.
   *
   * @return {@code true} if valid, {@code false} otherwise.
   */
  public boolean isValidPrivateKey(BigInteger scalar) {
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
  public ECPoint multiplyG(BigInteger scalar) {
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
  public ECPoint multiply(ECPoint point, BigInteger scalar) {
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
  public ECPoint add(ECPoint p1, ECPoint p2) {
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
  public ECPoint negate(ECPoint point) {
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
  public boolean pointsEqual(ECPoint p1, ECPoint p2) {
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
  public byte[] serializeCompressed(ECPoint point) {
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
  public ECPoint deserialize(byte[] encoded) {
    Objects.requireNonNull(encoded, "encoded must not be null");
    return CURVE_PARAMS.getCurve().decodePoint(encoded).normalize();
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
  public byte[] scalarAdd(byte[] a, byte[] b) {
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
  public byte[] scalarMultiply(byte[] a, byte[] b) {
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
  public byte[] scalarInverse(byte[] a) {
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
  public byte[] scalarNegate(byte[] a) {
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
  public boolean isValidScalar(byte[] scalar) {
    Objects.requireNonNull(scalar, "scalar must not be null");
    if (scalar.length != 32) {
      return false;
    }
    BigInteger scalarInt = new BigInteger(1, scalar);
    return scalarInt.compareTo(BigInteger.ZERO) > 0 && scalarInt.compareTo(CURVE_ORDER) < 0;
  }

  /**
   * Returns the scalar representing (n - 1), which is equivalent to -1 mod n.
   *
   * @return A 32-byte array representing -1 mod n.
   */
  public byte[] scalarMinusOne() {
    return toBytes32(CURVE_ORDER.subtract(BigInteger.ONE));
  }

  /**
   * Gets the infinity point (point at infinity / identity element).
   *
   * @return The infinity point.
   */

  public ECPoint getInfinity() {
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
  public byte[] toBytes32(BigInteger value) {
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
  public byte[] unsignedLongToScalar(UnsignedLong value) {
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
  public byte[] scalarZero() {
    return new byte[32];
  }

  /**
   * Returns the scalar representing one.
   *
   * @return A 32-byte array representing 1.
   */
  public byte[] scalarOne() {
    byte[] result = new byte[32];
    result[31] = 0x01;
    return result;
  }
}
