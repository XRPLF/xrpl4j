package org.xrpl.xrpl4j.crypto.confidential;

import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.codec.addresses.ByteUtils;
import org.xrpl.xrpl4j.crypto.HashingUtils;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
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
  // Size Calculation Methods
  // ============================================================================

  /**
   * Calculates the expected proof size for a same-plaintext multi proof.
   *
   * <p>Port of {@code secp256k1_mpt_prove_same_plaintext_multi_size} from proof_same_plaintext_multi.c.</p>
   *
   * <p>Formula: (1 Tm + 2N Tr) * 33 + (1 sm + N sr) * 32 = ((1 + 2*n) * 33) + ((1 + n) * 32)</p>
   *
   * @param numCiphertexts The number of ciphertexts (recipients).
   *
   * @return The expected proof size in bytes.
   */
  public static int samePlaintextMultiProofSize(int numCiphertexts) {
    return ((1 + 2 * numCiphertexts) * 33) + ((1 + numCiphertexts) * 32);
  }

  // ============================================================================
  // Curve Parameters
  // ============================================================================

  private static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");
  private static final BigInteger CURVE_ORDER = CURVE_PARAMS.getN();

  // ============================================================================
  // NUMS Generator Constants (for Pedersen commitments)
  // ============================================================================

  private static final String NUMS_DOMAIN_SEPARATOR = "MPT_BULLETPROOF_V1_NUMS";
  private static final String NUMS_CURVE_LABEL = "secp256k1";

  /**
   * Cached H generator point (lazily initialized, thread-safe via volatile).
   */
  private static volatile ECPoint cachedH;

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
   * Gets the H generator point for Pedersen commitments.
   *
   * <p>Port of {@code secp256k1_mpt_get_h_generator} which derives a NUMS (Nothing-Up-My-Sleeve)
   * point using the label "H" at index 0. The discrete logarithm of H with respect to G is
   * unknown, which is required for the binding property of Pedersen commitments.</p>
   *
   * <p>This method is thread-safe and caches the result for efficiency.</p>
   *
   * @return The H generator point.
   */
  public static ECPoint getH() {
    if (cachedH == null) {
      synchronized (Secp256k1Operations.class) {
        if (cachedH == null) {
          cachedH = hashToPointNums("H".getBytes(StandardCharsets.UTF_8), 0);
        }
      }
    }
    return cachedH;
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
   * @param first  The first scalar (32 bytes, big-endian).
   * @param second The second scalar (32 bytes, big-endian).
   *
   * @return The sum (first + second) mod n as a 32-byte array.
   */
  public static byte[] scalarAdd(byte[] first, byte[] second) {
    Objects.requireNonNull(first, "first must not be null");
    Objects.requireNonNull(second, "second must not be null");
    BigInteger firstInt = new BigInteger(1, first);
    BigInteger secondInt = new BigInteger(1, second);
    BigInteger result = firstInt.add(secondInt).mod(CURVE_ORDER);
    return toBytes32(result);
  }

  /**
   * Multiplies two scalars modulo the curve order.
   *
   * @param first  The first scalar (32 bytes, big-endian).
   * @param second The second scalar (32 bytes, big-endian).
   *
   * @return The product (first * second) mod n as a 32-byte array.
   */
  public static byte[] scalarMultiply(byte[] first, byte[] second) {
    Objects.requireNonNull(first, "first must not be null");
    Objects.requireNonNull(second, "second must not be null");
    BigInteger firstInt = new BigInteger(1, first);
    BigInteger secondInt = new BigInteger(1, second);
    BigInteger result = firstInt.multiply(secondInt).mod(CURVE_ORDER);
    return toBytes32(result);
  }

  /**
   * Computes the modular inverse of a scalar.
   *
   * @param scalar The scalar to invert (32 bytes, big-endian).
   *
   * @return The inverse scalar^(-1) mod n as a 32-byte array.
   *
   * @throws IllegalArgumentException if scalar is zero.
   */
  public static byte[] scalarInverse(byte[] scalar) {
    Objects.requireNonNull(scalar, "scalar must not be null");
    BigInteger scalarInt = new BigInteger(1, scalar);
    if (scalarInt.equals(BigInteger.ZERO)) {
      throw new IllegalArgumentException("Cannot invert zero");
    }
    BigInteger result = scalarInt.modInverse(CURVE_ORDER);
    return toBytes32(result);
  }

  /**
   * Negates a scalar modulo the curve order.
   *
   * @param scalar The scalar to negate (32 bytes, big-endian).
   *
   * @return The negation (-scalar) mod n as a 32-byte array.
   */
  public static byte[] scalarNegate(byte[] scalar) {
    Objects.requireNonNull(scalar, "scalar must not be null");
    BigInteger scalarInt = new BigInteger(1, scalar);
    BigInteger result = CURVE_ORDER.subtract(scalarInt).mod(CURVE_ORDER);
    return toBytes32(result);
  }

  /**
   * Subtracts two scalars modulo the curve order.
   *
   * @param first The first scalar (32 bytes, big-endian).
   * @param second The second scalar (32 bytes, big-endian).
   *
   * @return The difference (first - second) mod n as a 32-byte array.
   */
  public static byte[] scalarSub(byte[] first, byte[] second) {
    Objects.requireNonNull(first, "first must not be null");
    Objects.requireNonNull(second, "second must not be null");
    BigInteger firstInt = new BigInteger(1, first);
    BigInteger secondInt = new BigInteger(1, second);
    BigInteger result = firstInt.subtract(secondInt).mod(CURVE_ORDER);
    return toBytes32(result);
  }

  /**
   * Checks if a scalar is zero.
   *
   * @param scalar The scalar to check (32 bytes, big-endian).
   *
   * @return {@code true} if the scalar is zero, {@code false} otherwise.
   */
  public static boolean isScalarZero(byte[] scalar) {
    Objects.requireNonNull(scalar, "scalar must not be null");
    for (byte b : scalar) {
      if (b != 0) {
        return false;
      }
    }
    return true;
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

  /**
   * Appends a compressed EC point to a byte buffer at the given offset.
   *
   * @param buffer The destination byte buffer.
   * @param offset The offset at which to write the compressed point.
   * @param point  The EC point to serialize and append.
   *
   * @return The new offset after writing (offset + 33).
   */
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

  // ============================================================================
  // NUMS Point Derivation (for Pedersen commitments)
  // ============================================================================

  /**
   * Generates a vector of N independent NUMS generators.
   *
   * <p>Port of {@code secp256k1_mpt_get_generator_vector} from commitments.c.</p>
   *
   * <p>Used to populate the G_i and H_i vectors for Bulletproofs. Each point
   * is derived using the NUMS hash-to-point method with the given label and
   * sequential indices.</p>
   *
   * @param label The label string (e.g., "G" or "H").
   * @param count Number of generators to derive.
   *
   * @return Array of count generator points.
   *
   * @throws NullPointerException  if label is null.
   * @throws IllegalStateException if any point derivation fails.
   */
  public static ECPoint[] getGeneratorVector(String label, int count) {
    Objects.requireNonNull(label, "label must not be null");
    byte[] labelBytes = label.getBytes(StandardCharsets.UTF_8);
    ECPoint[] vec = new ECPoint[count];
    for (int i = 0; i < count; i++) {
      vec[i] = hashToPointNums(labelBytes, i);
    }
    return vec;
  }

  /**
   * Gets the U generator point for IPA (Inner Product Argument).
   *
   * <p>Port of the U generator derivation from bulletproof_aggregated.c.</p>
   *
   * <p>This is a NUMS point derived using the label "BP_U" at index 0.</p>
   *
   * @return The U generator point.
   */
  public static ECPoint getU() {
    return hashToPointNums("BP_U".getBytes(StandardCharsets.UTF_8), 0);
  }

  /**
   * Deterministically derives a NUMS (Nothing-Up-My-Sleeve) generator point.
   *
   * <p>Port of {@code secp256k1_mpt_hash_to_point_nums} from commitments.c.</p>
   *
   * <p>Uses SHA-256 try-and-increment to find a valid x-coordinate. This ensures the
   * discrete logarithm of the resulting point is unknown.</p>
   *
   * @param label The domain/vector label (e.g., "H").
   * @param index The vector index.
   *
   * @return The derived generator point.
   *
   * @throws IllegalStateException if no valid point is found (extremely unlikely).
   */
  public static ECPoint hashToPointNums(byte[] label, int index) {
    byte[] domainBytes = NUMS_DOMAIN_SEPARATOR.getBytes(StandardCharsets.UTF_8);
    byte[] curveBytes = NUMS_CURVE_LABEL.getBytes(StandardCharsets.UTF_8);
    byte[] indexBe = ByteUtils.toByteArray(index, 4);

    // Try-and-increment loop
    for (long ctr = 0; ctr < 0xFFFFFFFFL; ctr++) {
      // Build hash input: domainSeparator || curveLabel || label || index || counter
      int inputLen = domainBytes.length + curveBytes.length +
        (label != null ? label.length : 0) + 4 + 4;
      byte[] hashInput = new byte[inputLen];
      int offset = 0;

      System.arraycopy(domainBytes, 0, hashInput, offset, domainBytes.length);
      offset += domainBytes.length;
      System.arraycopy(curveBytes, 0, hashInput, offset, curveBytes.length);
      offset += curveBytes.length;
      if (label != null && label.length > 0) {
        System.arraycopy(label, 0, hashInput, offset, label.length);
        offset += label.length;
      }
      System.arraycopy(indexBe, 0, hashInput, offset, 4);
      offset += 4;
      byte[] ctrBe = ByteUtils.toByteArray((int) ctr, 4);
      System.arraycopy(ctrBe, 0, hashInput, offset, 4);

      byte[] hash = HashingUtils.sha256(hashInput).toByteArray();

      // Construct compressed point candidate: 0x02 || hash
      byte[] compressed = new byte[33];
      compressed[0] = 0x02;
      System.arraycopy(hash, 0, compressed, 1, 32);

      try {
        ECPoint point = deserialize(compressed);
        if (point != null && !point.isInfinity()) {
          return point;
        }
      } catch (Exception e) {
        // Invalid point, continue to next counter
      }
    }

    throw new IllegalStateException("Failed to derive NUMS point (extremely unlikely)");
  }
}
