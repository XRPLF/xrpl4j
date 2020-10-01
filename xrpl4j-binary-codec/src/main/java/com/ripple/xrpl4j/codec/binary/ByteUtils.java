package com.ripple.xrpl4j.codec.binary;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedInteger;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ByteUtils {

  public static byte[] toByteArray(int value, int byteSize) {
    BigInteger bigInteger = checkSize(byteSize * Byte.SIZE, BigInteger.valueOf(value));
    return Arrays.copyOfRange(bigInteger.toByteArray(), 0, byteSize);
  }

  public static List<UnsignedByte> parse(String hex) {
    String padded = padded(hex);
    List<UnsignedByte> result = new ArrayList<>();
    for(int i = 0; i < padded.length(); i+=2) {
      result.add(UnsignedByte.of(padded.substring(i, i + 2)));
    }
    return result;
  }

  public static BigInteger checkSize(int expectedBits, BigInteger value) {
    Preconditions.checkArgument(value.bitLength() <= expectedBits);
    return value;
  }

  public static String coalesce(List<UnsignedByte> segments) {
    return Joiner.on("").join(segments.stream().map(UnsignedByte::hexValue).collect(Collectors.toList()));
  }

  public static UnsignedInteger coalesceToInt(List<UnsignedByte> segments) {
    return UnsignedInteger.valueOf(new BigInteger(coalesce(segments), 16));
  }

  private static String padded(String hex) {
    if (hex.length() % 2 == 0) {
      return hex;
    }
    return "0" + hex;
  }

}
