package com.ripple.xrpl4j.codec.binary;

import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;

public class UnsignedByte {

  private final int value;

  public static UnsignedByte of(int value) {
    return new UnsignedByte(value);
  }

  public static UnsignedByte of(byte highBits, byte lowBits) {
    return new UnsignedByte((highBits << 4) + lowBits);
  }

  private UnsignedByte(int value) {
    Preconditions.checkArgument(value >= 0);
    Preconditions.checkArgument(value <= 255);
    this.value = value;
  }

  public byte value() {
    return (byte) value;
  }

  public String hexValue() {
    return BaseEncoding.base16().encode(new byte[] { value() });
  }

}
