package com.ripple.xrpl4j.codec.binary.addresses;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class Base58 {
  public static final char[] ALPHABET = "rpshnaf39wBUDNEGHJKLM4PQRST7VWXYZ2bcdeCg65jkm8oFqi1tuvAxyz".toCharArray();

  private static final int[] INDEXES = new int[255];
  static {
    for (int i = 0; i < INDEXES.length; i++) {
      INDEXES[i] = -1;
    }
    for (int i = 0; i < ALPHABET.length; i++) {
      INDEXES[ALPHABET[i]] = i;
    }
  }

  /** Encodes the given bytes in base58. No checksum is appended. */
  public static String encode(byte[] input) {
    if (input.length == 0) {
      return "";
    }
    input = copyOfRange(input, 0, input.length);
    // Count leading zeroes.
    int zeroCount = 0;
    while (zeroCount < input.length && input[zeroCount] == 0) {
      ++zeroCount;
    }
    // The actual encoding.
    byte[] temp = new byte[input.length * 2];
    int j = temp.length;

    int startAt = zeroCount;
    while (startAt < input.length) {
      byte mod = divmod58(input, startAt);
      if (input[startAt] == 0) {
        ++startAt;
      }
      temp[--j] = (byte) ALPHABET[mod];
    }

    // Strip extra '1' if there are some after decoding.
    while (j < temp.length && temp[j] == ALPHABET[0]) {
      ++j;
    }
    // Add as many leading '1' as there were leading zeros.
    while (--zeroCount >= 0) {
      temp[--j] = (byte) ALPHABET[0];
    }

    byte[] output = copyOfRange(temp, j, temp.length);
    try {
      return new String(output, "US-ASCII");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);  // Cannot happen.
    }
  }

  public static byte[] decode(String input) {
    if (input.length() == 0) {
      return new byte[0];
    }
    byte[] input58 = new byte[input.length()];
    // Transform the String to a base58 byte sequence
    for (int i = 0; i < input.length(); ++i) {
      char c = input.charAt(i);

      int digit58 = -1;
      if (c < INDEXES.length) {
        digit58 = INDEXES[c];
      }
      if (digit58 < 0) {
        throw new EncodingFormatException("Illegal character " + c + " at " + i);
      }

      input58[i] = (byte) digit58;
    }
    // Count leading zeroes
    int zeroCount = 0;
    while (zeroCount < input58.length && input58[zeroCount] == 0) {
      ++zeroCount;
    }
    // The encoding
    byte[] temp = new byte[input.length()];
    int j = temp.length;

    int startAt = zeroCount;
    while (startAt < input58.length) {
      byte mod = divmod256(input58, startAt);
      if (input58[startAt] == 0) {
        ++startAt;
      }

      temp[--j] = mod;
    }
    // Do no add extra leading zeroes, move j to first non null byte.
    while (j < temp.length && temp[j] == 0) {
      ++j;
    }

    return copyOfRange(temp, j - zeroCount, temp.length);
  }

  public static BigInteger decodeToBigInteger(String input) throws EncodingFormatException {
    return new BigInteger(1, decode(input));
  }

  public static String encodeChecked(byte[] bytes, List<Version> versions) {
    int versionsLength = 0;
    for (Version version : versions) {
      versionsLength += version.getValues().length;
    }
    byte[] versionsBytes = new byte[versionsLength];
    for (int i = 0; i < versions.size(); i++) {
      for (int j = 0; j < versions.get(i).getValues().length; j++) {
        versionsBytes[i + j] = (byte) versions.get(i).getValues()[j];
      }
    }

    byte[] bytesAndVersions = new byte[bytes.length + versionsLength];
    System.arraycopy(versionsBytes, 0, bytesAndVersions, 0, versionsLength);
    System.arraycopy(bytes, 0, bytesAndVersions, versionsLength, bytes.length);

    byte[] checkSum = copyOfRange(Utils.doubleDigest(bytesAndVersions), 0, 4);
    byte[] output = new byte[bytesAndVersions.length + checkSum.length];
    System.arraycopy(bytesAndVersions, 0, output, 0, bytesAndVersions.length);
    System.arraycopy(checkSum, 0, output, bytesAndVersions.length, checkSum.length);

    return encode(output);
  }

  /**
   * Uses the checksum in the last 4 bytes of the decoded data to verify the rest are correct. The checksum is
   * removed from the returned data.
   *
   * @throws EncodingFormatException if the input is not base 58 or the checksum does not validate.
   */
  public static byte[] decodeChecked(String input) throws EncodingFormatException {
    byte tmp [] = decode(input);
    if (tmp.length < 4)
      throw new EncodingFormatException("Input must be longer than 3 characters.");
    byte[] bytes = copyOfRange(tmp, 0, tmp.length - 4);
    byte[] checksum = copyOfRange(tmp, tmp.length - 4, tmp.length);

    tmp = Utils.doubleDigest(bytes);
    byte[] hash = copyOfRange(tmp, 0, 4);
    if (!Arrays.equals(checksum, hash))
      throw new EncodingFormatException("Checksum does not validate");

    return bytes;
  }
  //
  // number -> number / 58, returns number % 58
  //

  private static byte divmod58(byte[] number, int startAt) {
    int remainder = 0;
    for (int i = startAt; i < number.length; i++) {
      int digit256 = (int) number[i] & 0xFF;
      int temp = remainder * 256 + digit256;

      number[i] = (byte) (temp / 58);

      remainder = temp % 58;
    }

    return (byte) remainder;
  }
  //
  // number -> number / 256, returns number % 256
  //

  private static byte divmod256(byte[] number58, int startAt) {
    int remainder = 0;
    for (int i = startAt; i < number58.length; i++) {
      int digit58 = (int) number58[i] & 0xFF;
      int temp = remainder * 58 + digit58;

      number58[i] = (byte) (temp / 256);

      remainder = temp % 256;
    }

    return (byte) remainder;
  }

  private static byte[] copyOfRange(byte[] source, int from, int to) {
    byte[] range = new byte[to - from];
    System.arraycopy(source, from, range, 0, range.length);

    return range;
  }
}
