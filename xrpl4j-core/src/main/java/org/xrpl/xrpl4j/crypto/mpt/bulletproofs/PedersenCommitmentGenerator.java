package org.xrpl.xrpl4j.crypto.mpt.bulletproofs;

import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.crypto.HashingUtils;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Generates Pedersen Commitments of the form C = v * G + r * H.
 *
 * <p>This implementation uses a "Nothing-Up-My-Sleeve" (NUMS) construction for the H generator,
 * ensuring that the discrete logarithm of H with respect to G is unknown. This is critical
 * for the binding property of the commitments.</p>
 *
 * <p>The NUMS generators are derived deterministically using SHA-256 hash-to-curve with
 * the domain separation tag "MPT_BULLETPROOF_V1_NUMS".</p>
 *
 * @see <a href="ConfidentialMPT_20260201.pdf">Spec Section 3.3.5</a>
 */
public class PedersenCommitmentGenerator {

  /**
   * Domain separator for NUMS generator derivation.
   */
  private static final String DOMAIN_SEPARATOR = "MPT_BULLETPROOF_V1_NUMS";

  /**
   * Curve label for NUMS generator derivation.
   */
  private static final String CURVE_LABEL = "secp256k1";

  /**
   * Compressed point size in bytes.
   */
  private static final int COMPRESSED_POINT_SIZE = 33;

  /**
   * Scalar size in bytes.
   */
  private static final int SCALAR_SIZE = 32;

  /**
   * Cached H generator point (lazily initialized).
   */
  private ECPoint cachedH;

  /**
   * Constructs a new PedersenCommitmentGenerator.
   */
  public PedersenCommitmentGenerator() {
  }

  /**
   * Creates a Pedersen Commitment: C = amount * G + rho * H.
   *
   * <p>Handles the edge case where amount = 0, in which case C = rho * H.</p>
   *
   * @param amount The value to commit to (64-bit unsigned).
   * @param rho    The blinding factor (32 bytes, must be a valid scalar).
   *
   * @return The commitment as a 33-byte compressed point.
   *
   * @throws IllegalArgumentException if rho is not a valid scalar.
   */
  public byte[] generateCommitment(UnsignedLong amount, byte[] rho) {
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(rho, "rho must not be null");

    if (rho.length != SCALAR_SIZE) {
      throw new IllegalArgumentException("rho must be 32 bytes");
    }

    if (!Secp256k1Operations.isValidScalar(rho)) {
      throw new IllegalArgumentException("rho must be a valid scalar (0 < rho < curve order)");
    }

    // Get the H generator
    ECPoint H = getHGenerator();

    // Compute rho * H (blinding term)
    BigInteger rhoInt = new BigInteger(1, rho);
    ECPoint rH = Secp256k1Operations.multiply(H, rhoInt);

    ECPoint commitment;

    // Handle zero amount case: C = rho * H
    if (amount.equals(UnsignedLong.ZERO)) {
      commitment = rH;
    } else {
      // Compute amount * G (value term)
      ECPoint mG = Secp256k1Operations.multiplyG(amount.bigIntegerValue());
      // Combine: C = mG + rH
      commitment = Secp256k1Operations.add(mG, rH);
    }

    return Secp256k1Operations.serializeCompressed(commitment);
  }

  /**
   * Gets the H generator point for Pedersen commitments.
   *
   * <p>This derives a NUMS point using the label "H" at index 0. The discrete logarithm
   * of H with respect to G is unknown, which is required for the binding property.</p>
   *
   * @return The H generator point.
   */
  private ECPoint getHGenerator() {
    if (cachedH == null) {
      cachedH = hashToPointNums("H".getBytes(StandardCharsets.UTF_8), 0);
    }
    return cachedH;
  }

  /**
   * Deterministically derives a NUMS (Nothing-Up-My-Sleeve) generator point.
   *
   * <p>Uses SHA-256 try-and-increment to find a valid x-coordinate. This ensures the
   * discrete logarithm of the resulting point is unknown.</p>
   *
   * @param label The domain/vector label (e.g., "G" or "H").
   * @param index The vector index.
   *
   * @return The derived generator point.
   *
   * @throws IllegalStateException if no valid point is found (extremely unlikely).
   */
  private ECPoint hashToPointNums(byte[] label, int index) {
    byte[] domainBytes = DOMAIN_SEPARATOR.getBytes(StandardCharsets.UTF_8);
    byte[] curveBytes = CURVE_LABEL.getBytes(StandardCharsets.UTF_8);

    // Convert index to big-endian 4 bytes
    byte[] indexBe = intToBigEndian(index);

    // Try-and-increment loop
    for (long ctr = 0; ctr < 0xFFFFFFFFL; ctr++) {
      byte[] ctrBe = intToBigEndian((int) ctr);

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

      System.arraycopy(ctrBe, 0, hashInput, offset, 4);

      // Compute SHA-256 hash
      byte[] hash = HashingUtils.sha256(hashInput).toByteArray();

      // Construct compressed point candidate: 0x02 || hash
      byte[] compressed = new byte[COMPRESSED_POINT_SIZE];
      compressed[0] = 0x02; // Force even Y
      System.arraycopy(hash, 0, compressed, 1, SCALAR_SIZE);

      // Try to parse as a valid curve point
      try {
        ECPoint point = Secp256k1Operations.deserialize(compressed);
        if (point != null && !point.isInfinity()) {
          return point;
        }
      } catch (Exception e) {
        // Invalid point, continue to next counter
      }
    }

    throw new IllegalStateException("Failed to derive NUMS point (extremely unlikely)");
  }

  /**
   * Converts an integer to big-endian 4 bytes.
   *
   * @param value The integer value.
   *
   * @return The 4-byte big-endian representation.
   */
  private byte[] intToBigEndian(int value) {
    return new byte[] {
      (byte) (value >> 24),
      (byte) (value >> 16),
      (byte) (value >> 8),
      (byte) value
    };
  }
}

