package org.xrpl.xrpl4j.codec.addresses;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.primitives.UnsignedLong;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ByteUtils {

  /**
   * Converts an int to a byte array of the expected size, left padded with 0 bytes.
   *
   * @param value    The int value to convert.
   * @param byteSize The final size of the byte[].
   *
   * @return A byte[] with the converted value, left padded with 0 bytes.
   */
  public static byte[] toByteArray(int value, int byteSize) {
    BigInteger bigInteger = checkSize(byteSize * Byte.SIZE, BigInteger.valueOf(value));
    return toLeftPaddedByteArray(byteSize, bigInteger);
  }

  /**
   * Converts a {@link BigInteger} to a byte array of the expected size, left padded with 0 bytes.
   *
   * @param value    The {@link BigInteger} to convert.
   * @param byteSize The final size of the byte[].
   *
   * @return A byte[] with the converted value, left padded with 0 bytes.
   */
  public static byte[] toByteArray(BigInteger value, int byteSize) {
    BigInteger bigInteger = checkSize(byteSize * Byte.SIZE, value);
    return toLeftPaddedByteArray(byteSize, bigInteger);
  }

  /**
   * Converts a {@link BigInteger} to a byte array of a given size. If size of the byte array is bigger than the number
   * of bytes in the given {@link BigInteger}, then the result array will have leading zeroes.
   *
   * @param byteSize   The final size of the byte[].
   * @param bigInteger The {@link BigInteger} to convert.
   *
   * @return A left padded byte array with the converted value.
   */
  private static byte[] toLeftPaddedByteArray(int byteSize, BigInteger bigInteger) {
    byte[] target = new byte[byteSize];
    byte[] source = bigInteger.toByteArray();
    for (int i = 0; i < source.length && i < target.length; i++) {
      target[byteSize - i - 1] = source[source.length - i - 1];
    }
    return target;
  }

  /**
   * Parses a hexadecimal {@link String} to a {@link List} of {@link UnsignedByte}s.
   *
   * @param hex A hexadecimal {@link String} to parse.
   *
   * @return A {@link List} of {@link UnsignedByte}s containing the parsed hex.
   */
  public static List<UnsignedByte> parse(String hex) {
    String padded = padded(hex);
    List<UnsignedByte> result = new ArrayList<>();
    for (int i = 0; i < padded.length(); i += 2) {
      result.add(UnsignedByte.of(padded.substring(i, i + 2)));
    }
    return result;
  }

  /**
   * Checks that the given {@link BigInteger} value has a bitsize (binary) less than or equal to the
   * {@code expectedBits}.
   *
   * <p>This is used to make sure a value can fit in a field with a fixed bit size.</p>
   *
   * @param expectedBits The expected number of bits in {@code value}
   * @param value        The {@link BigInteger} to check.
   *
   * @return The {@link BigInteger} {@code value}.
   */
  public static BigInteger checkSize(int expectedBits, BigInteger value) {
    Preconditions.checkArgument(value.bitLength() <= expectedBits,
      "value has " + value.bitLength() + " bits but should be <= " + expectedBits);
    return value;
  }

  /**
   * Converts a {@link List} of {@link UnsignedByte}s to a hexadecimal {@link String}.
   *
   * @param segments A {@link List} of {@link UnsignedByte}s to convert.
   *
   * @return A hexadecimal {@link String}.
   */
  public static String toHex(List<UnsignedByte> segments) {
    return Joiner.on("").join(segments.stream().map(UnsignedByte::hexValue).collect(Collectors.toList()));
  }

  /**
   * Converts an {@link UnsignedByteArray} to an {@link UnsignedLong}.
   *
   * @param segments The {@link UnsignedByteArray} to convert.
   *
   * @return The {@link UnsignedLong} value of {@code segments}.
   */
  public static UnsignedLong toUnsignedLong(UnsignedByteArray segments) {
    return UnsignedLong.valueOf(segments.hexValue(), 16);
  }

  /**
   * Pads a hex string to one that has an even number of characters. For example "F" becomes "0F" and
   * "FF" remains "FF" since it already as an even number.
   *
   * @param hex The hex {@link String} to pad.
   *
   * @return A left padded hex {@link String}.
   */
  public static String padded(String hex) {
    return hex.length() % 2 == 0 ? hex : "0" + hex;
  }

  /**
   * Pads a hex string to one that has the expected number of characters. For example {@code padded("FF", 4)} yields
   * "00FF"
   *
   * @param hex       The hex {@link String} to pad.
   * @param hexLength The length of the final padded hex {@link String}.
   *
   * @return A left padded hex {@link String} with the given length.
   */
  public static String padded(String hex, int hexLength) {
    return Strings.repeat("0", hexLength - hex.length()) + hex;
  }

}
