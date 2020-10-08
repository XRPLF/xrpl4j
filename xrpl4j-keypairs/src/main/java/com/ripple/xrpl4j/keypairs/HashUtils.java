package com.ripple.xrpl4j.keypairs;

import static java.util.Arrays.copyOfRange;

import com.google.common.hash.Hashing;
import com.ripple.xrpl4j.codec.addresses.UnsignedByteArray;

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

}
