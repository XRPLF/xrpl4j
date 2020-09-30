package com.ripple.xrpl4j.codec.binary;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

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
    List<UnsignedByte> result = new ArrayList<>();
    for(int i = 0; i < hex.length(); i+=2) {
      result.add(UnsignedByte.of(hex.substring(i, i + 2)));
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
}
