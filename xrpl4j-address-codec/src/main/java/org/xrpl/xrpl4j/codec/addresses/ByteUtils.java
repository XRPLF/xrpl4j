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
   * @param value
   * @param byteSize
   * @return
   */
  public static byte[] toByteArray(int value, int byteSize) {
    BigInteger bigInteger = checkSize(byteSize * Byte.SIZE, BigInteger.valueOf(value));
    return toLeftPaddedByteArray(byteSize, bigInteger);
  }

  /**
   * Converts a BigInteger to a byte array of the expected size, left padded with 0 bytes.
   *
   * @param value
   * @param byteSize
   * @return
   */
  public static byte[] toByteArray(BigInteger value, int byteSize) {
    BigInteger bigInteger = checkSize(byteSize * Byte.SIZE, value);
    return toLeftPaddedByteArray(byteSize, bigInteger);
  }

  /**
   * Converts a BigInteger to a byte array of a given size. If size of the byte array is bigger than the number
   * of bytes in the given BigInteger, then the result array will have leading zeroes.
   *
   * @param byteSize
   * @param bigInteger
   * @return
   */
  private static byte[] toLeftPaddedByteArray(int byteSize, BigInteger bigInteger) {
    byte[] target = new byte[byteSize];
    byte[] source = bigInteger.toByteArray();
    for (int i = 0; i < source.length && i < target.length; i++) {
      target[byteSize - i - 1] = source[source.length - i - 1];
    }
    return target;
  }

  public static List<UnsignedByte> parse(String hex) {
    String padded = padded(hex);
    List<UnsignedByte> result = new ArrayList<>();
    for (int i = 0; i < padded.length(); i += 2) {
      result.add(UnsignedByte.of(padded.substring(i, i + 2)));
    }
    return result;
  }

  /**
   * Checks that the given BigInteger value has a bitsize (binary) less than or equal to the expectedBits.
   * This is used to make sure a value can fit in a field with a fixed bit size.
   *
   * @param expectedBits
   * @param value
   * @return
   */
  public static BigInteger checkSize(int expectedBits, BigInteger value) {
    Preconditions.checkArgument(value.bitLength() <= expectedBits,
      "value has " + value.bitLength() + " bits but should be <= " + expectedBits);
    return value;
  }

  public static String toHex(List<UnsignedByte> segments) {
    return Joiner.on("").join(segments.stream().map(UnsignedByte::hexValue).collect(Collectors.toList()));
  }

  public static UnsignedLong toUnsignedLong(UnsignedByteArray segments) {
    return UnsignedLong.valueOf(segments.hexValue(), 16);
  }

  /**
   * Pads a hex string to one that has an even number of characters. For example "F" becomes "0F" and
   * "FF" remains "FF" since it already as an even number.
   *
   * @param hex
   * @return
   */
  public static String padded(String hex) {
    return hex.length() % 2 == 0 ? hex : "0" + hex;
  }

  /**
   * Pads a hex string to one that has the expected number of characters. For example {@code padded("FF", 4)} yields
   * "00FF"
   *
   * @param hex
   * @param hexLength
   * @return
   */
  public static String padded(String hex, int hexLength) {
    return Strings.repeat("0", hexLength - hex.length()) + hex;
  }

}
