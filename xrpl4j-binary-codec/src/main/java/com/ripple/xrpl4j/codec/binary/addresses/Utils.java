package com.ripple.xrpl4j.codec.binary.addresses;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {

  private static final MessageDigest digest;
  static {
    try {
      digest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);  // Can't happen.
    }
  }

  /**
   * See {@link Utils#doubleDigest(byte[], int, int)}.
   */
  public static byte[] doubleDigest(byte[] input) {
    return doubleDigest(input, 0, input.length);
  }

  /**
   * Calculates the SHA-256 hash of the given byte range, and then hashes the resulting hash again. This is
   * standard procedure in XRPL. The resulting hash is in big endian form.
   */
  public static byte[] doubleDigest(byte[] input, int offset, int length) {
    synchronized (digest) {
      digest.reset();
      digest.update(input, offset, length);
      byte[] first = digest.digest();
      return digest.digest(first);
    }
  }

}
