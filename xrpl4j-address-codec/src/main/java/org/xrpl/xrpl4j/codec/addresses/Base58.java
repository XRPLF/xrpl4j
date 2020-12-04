package org.xrpl.xrpl4j.codec.addresses;

import static java.util.Arrays.copyOfRange;

import org.xrpl.xrpl4j.codec.addresses.exceptions.EncodingFormatException;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Utility class for encoding and decoding in Base58.  Includes methods for encoding and decoding with a 4 byte
 * checksum, which is necessary for XRPL Address encoding.
 */
public class Base58 {
  public static final char[] ALPHABET = "rpshnaf39wBUDNEGHJKLM4PQRST7VWXYZ2bcdeCg65jkm8oFqi1tuvAxyz".toCharArray();

  private static final int[] INDEXES = new int[255];

  static {
    Arrays.fill(INDEXES, -1);
    for (int i = 0; i < ALPHABET.length; i++) {
      INDEXES[ALPHABET[i]] = i;
    }
  }

  /**
   * Encodes the given bytes to a Base58 {@link String}
   *
   * @param input A byte array to encode.
   * @return The bytes encoded to a Base58 {@link String}
   */
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
    return new String(output, StandardCharsets.US_ASCII);
  }

  /**
   * Decodes a Base58 encoded {@link String} to a byte array.
   *
   * @param input The Base58 {@link String}.
   * @return A byte array containing the decoded Base58 {@link String}.
   */
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

  /**
   * Encodes the given byte array to a Base58 {@link String} with a 4 byte checksum appended.
   *
   * @param bytes The byte array to encode.
   * @return A {@link String} containing the Base58Check encoded bytes.
   */
  public static String encodeChecked(byte[] bytes) {

    byte[] checkSum = copyOfRange(Utils.doubleDigest(bytes), 0, 4);
    byte[] output = new byte[bytes.length + checkSum.length];
    System.arraycopy(bytes, 0, output, 0, bytes.length);
    System.arraycopy(checkSum, 0, output, bytes.length, checkSum.length);

    return encode(output);
  }

  /**
   * Decodes the given Base58Check encoded {@link String} to a byte array, and validates the checksum.
   *
   * @param input A Base58Check encoded {@link String}.
   * @return A byte array containing the decoded value.
   * @throws EncodingFormatException If the input is not Base58 encoded or the checksum does not validate.
   */
  public static byte[] decodeChecked(String input) throws EncodingFormatException {
    byte[] tmp = decode(input);
    if (tmp.length < 4) {
      throw new EncodingFormatException("Input must be longer than 3 characters.");
    }
    byte[] bytes = copyOfRange(tmp, 0, tmp.length - 4);
    byte[] checksum = copyOfRange(tmp, tmp.length - 4, tmp.length);

    tmp = Utils.doubleDigest(bytes);
    byte[] hash = copyOfRange(tmp, 0, 4);
    if (!Arrays.equals(checksum, hash)) {
      throw new EncodingFormatException("Checksum does not validate");
    }

    return bytes;
  }

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
}
