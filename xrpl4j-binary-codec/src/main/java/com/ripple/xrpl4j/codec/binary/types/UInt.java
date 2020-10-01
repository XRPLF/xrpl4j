package com.ripple.xrpl4j.codec.binary.types;

import com.google.common.primitives.UnsignedLong;
import com.ripple.xrpl4j.codec.binary.ByteUtils;
import com.ripple.xrpl4j.codec.binary.serdes.UnsignedByteList;

abstract class UInt<T extends UInt<T>> extends SerializedType<T> {

  private final UnsignedLong value;

  public UInt(UnsignedLong value, int bitSize) {
    super(new UnsignedByteList(ByteUtils.padded(value.toString(16), bitSizeToHexLength(bitSize))));
    this.value = value;
  }

  UnsignedLong valueOf() {
    return value;
  }

  private static int bitSizeToHexLength(int bitSize) {
    return bitSize / 4;
  }

}
