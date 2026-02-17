package org.xrpl.xrpl4j.crypto.mpt.wrapper;

import com.google.common.io.BaseEncoding;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a Pedersen Commitment: C = amount * G + rho * H.
 *
 * <p>This class wraps the commitment point and provides convenient methods for
 * serialization in various formats needed for transactions and proofs.</p>
 */
public final class PedersenCommitment {

  private final ECPoint point;

  /**
   * Private constructor. Use factory methods to create instances.
   *
   * @param point The commitment point.
   */
  private PedersenCommitment(ECPoint point) {
    this.point = Objects.requireNonNull(point, "point must not be null");
  }

  /**
   * Creates a PedersenCommitment from an ECPoint.
   *
   * @param point The commitment point.
   *
   * @return A new PedersenCommitment instance.
   */
  public static PedersenCommitment fromPoint(ECPoint point) {
    return new PedersenCommitment(point);
  }

  /**
   * Creates a PedersenCommitment from compressed bytes (33 bytes).
   *
   * @param compressedBytes The 33-byte compressed point.
   *
   * @return A new PedersenCommitment instance.
   *
   * @throws IllegalArgumentException if the bytes are not a valid compressed point.
   */
  public static PedersenCommitment fromCompressedBytes(byte[] compressedBytes) {
    Objects.requireNonNull(compressedBytes, "compressedBytes must not be null");
    if (compressedBytes.length != 33) {
      throw new IllegalArgumentException("compressedBytes must be 33 bytes");
    }
    ECPoint point = Secp256k1Operations.deserialize(compressedBytes);
    return new PedersenCommitment(point);
  }

  /**
   * Returns the commitment as an ECPoint for use in proof generation/verification.
   *
   * @return The commitment point.
   */
  public ECPoint asEcPoint() {
    return point;
  }

  /**
   * Returns the commitment as 33-byte compressed format.
   *
   * @return The 33-byte compressed point.
   */
  public byte[] toCompressedBytes() {
    return Secp256k1Operations.serializeCompressed(point);
  }

  /**
   * Returns the commitment as hex string of the compressed format.
   *
   * @return The hex string of the 33-byte compressed point.
   */
  public String hexValue() {
    return BaseEncoding.base16().encode(toCompressedBytes());
  }

  /**
   * Returns the commitment in the 64-byte reversed format used in transactions.
   *
   * <p>This format is required because rippled does memcpy directly into secp256k1_pubkey.data
   * and expects the same reversed format as public keys. The X and Y coordinates are each reversed
   * (little-endian).</p>
   *
   * @return The 64-byte reversed format suitable for transaction fields.
   */
  public byte[] toReversedBytes64() {
    // Get uncompressed encoding (65 bytes with 04 prefix)
    byte[] uncompressed = point.getEncoded(false);

    // Skip the 04 prefix to get 64 bytes (X, Y)
    byte[] result = new byte[64];

    // Reverse X coordinate (first 32 bytes)
    for (int i = 0; i < 32; i++) {
      result[i] = uncompressed[32 - i]; // uncompressed[1..32] reversed
    }
    // Reverse Y coordinate (last 32 bytes)
    for (int i = 0; i < 32; i++) {
      result[32 + i] = uncompressed[64 - i]; // uncompressed[33..64] reversed
    }

    return result;
  }

  /**
   * Returns the commitment as hex string in the 64-byte reversed format used in transactions.
   *
   * @return The hex string of the 64-byte reversed format.
   */
  public String toReversedHex64() {
    return BaseEncoding.base16().encode(toReversedBytes64());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PedersenCommitment that = (PedersenCommitment) o;
    return Arrays.equals(toCompressedBytes(), that.toCompressedBytes());
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(toCompressedBytes());
  }

  @Override
  public String toString() {
    return "PedersenCommitment{" + hexValue() + "}";
  }
}

