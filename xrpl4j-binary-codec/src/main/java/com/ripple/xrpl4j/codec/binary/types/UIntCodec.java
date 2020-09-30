package com.ripple.xrpl4j.codec.binary.types;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.math.BigInteger;

/**
 * Codec for UInt8, UInt16, etc. Converts between binary hex value and raw value.
 */
public class UIntCodec implements TypeCodec {

  private final int bitCount;

  private final int byteSize;

  public UIntCodec(int bitCount) {
    Preconditions.checkArgument(bitCount % Byte.SIZE == 0, "invalid bitCount");
    this.bitCount = bitCount;
    this.byteSize = bitCount / Byte.SIZE;
  }

  @Override
  public String typeName() {
    return "UInt" + bitCount;
  }

  @Override
  public String decode(String hex) {
    return checkSize(new BigInteger(hex, 16)).toString();
  }

  @Override
  public String encode(String text) {
    BigInteger value = checkSize(new BigInteger(text));
    return padded(value.toString(16)).toUpperCase();
  }

  private String padded(String hex) {
    int padding = byteSize * 2 - hex.length();
    return Strings.repeat("0", padding) + hex;
  }

  private BigInteger checkSize(BigInteger value) {
    Preconditions.checkArgument(value.bitLength() <= bitCount);
    return value;
  }

}
