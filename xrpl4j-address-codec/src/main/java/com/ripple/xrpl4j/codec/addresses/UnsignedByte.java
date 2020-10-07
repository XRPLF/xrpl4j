package com.ripple.xrpl4j.codec.addresses;

import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Unsigned byte where value can be 0 to 255.
 */
public class UnsignedByte {

  // since Java byte is signed need to use int to handle max value 255
  private final int value;

  public static UnsignedByte of(int value) {
    return new UnsignedByte(value);
  }

  public static UnsignedByte of(byte value) {
    return new UnsignedByte(value & 0xff);
  }

  public static UnsignedByte of(byte highBits, byte lowBits) {
    return new UnsignedByte((highBits << 4) + lowBits);
  }

  public static UnsignedByte of(String hex) {
    byte highBits = new BigInteger(hex.substring(0, 1), 16).byteValue();
    byte lowBits = new BigInteger(hex.substring(1, 2), 16).byteValue();
    return UnsignedByte.of(highBits, lowBits);
  }

  private UnsignedByte(int value) {
    Preconditions.checkArgument(value >= 0);
    Preconditions.checkArgument(value <= 255);
    this.value = value;
  }

  public int asInt() {
    return value;
  }

  public byte asByte() { return (byte) value; }

  public int getHighBits() {
    return value >> 4;
  }

  public int getLowBits() {
    return value & 0x0F;
  }

  public boolean isNthBitSet(int nth)
  {
    Preconditions.checkArgument(nth >= 1 && nth <= 8);
    return ((value >> (8 - nth)) & 1) == 1;
  }

  public UnsignedByte or(UnsignedByte arg)
  {
    return UnsignedByte.of(value | arg.value);
  }

  public String hexValue() {
    return BaseEncoding.base16().encode(new byte[] { (byte) asInt() });
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof UnsignedByte)) {
      return false;
    }
    UnsignedByte that = (UnsignedByte) o;
    return value == that.value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
