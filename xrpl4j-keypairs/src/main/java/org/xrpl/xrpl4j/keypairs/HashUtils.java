package org.xrpl.xrpl4j.keypairs;

import static java.util.Arrays.copyOfRange;

import com.google.common.hash.Hashing;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByte;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

/**
 * Hashing utilities for XRPL related hashing algorithms.
 */
public class HashUtils {

  /**
   * Compute a SHA-512 hash of the given bytes and return the first half of the result.
   *
   * @param bytes The bytes to half hash.
   * @return An {@link UnsignedByteArray} containing the first half of the SHA-512 hash of bytes.
   */
  static UnsignedByteArray sha512Half(UnsignedByteArray bytes) {
    return sha512Half(bytes.toByteArray());
  }

  /**
   * Compute a SHA-512 hash of the given bytes and return the first half of the result.
   *
   * @param bytes The bytes to half hash.
   * @return An {@link UnsignedByteArray} containing the first half of the SHA-512 hash of bytes.
   */
  static UnsignedByteArray sha512Half(byte[] bytes) {
    return UnsignedByteArray.of(copyOfRange(Hashing.sha512().hashBytes(bytes).asBytes(), 0, 32));
  }

  /**
   * Add an Unsigned 32 bit integer to an {@link UnsignedByteArray}.
   *
   * @param bytes An {@link UnsignedByteArray} to append to.
   * @param i     An {@link Integer} with 4 signed bytes to add to bytes.
   * @return The resulting {@link UnsignedByteArray}.
   */
  public static UnsignedByteArray addUInt32(UnsignedByteArray bytes, Integer i) {
    bytes.append(UnsignedByte.of((byte) ((i >>> 24)) & 0xFF));
    bytes.append(UnsignedByte.of((byte) ((i >>> 16)) & 0xFF));
    bytes.append(UnsignedByte.of((byte) ((i >>> 8)) & 0xFF));
    bytes.append(UnsignedByte.of((byte) ((i) & 0xFF)));
    return bytes;
  }
}
