package com.ripple.xrpl4j.codec.binary.types;

import com.google.common.primitives.UnsignedLong;
import com.ripple.xrpl4j.codec.addresses.ByteUtils;
import com.ripple.xrpl4j.codec.addresses.UnsignedByteArray;

abstract class UIntType<T extends UIntType<T>> extends SerializedType<T> {

  private final UnsignedLong value;

  public UIntType(UnsignedLong value, int bitSize) {
    super(UnsignedByteArray.fromHex(ByteUtils.padded(value.toString(16), bitSizeToHexLength(bitSize))));
    this.value = value;
  }

  UnsignedLong valueOf() {
    return value;
  }

  private static int bitSizeToHexLength(int bitSize) {
    return bitSize / 4;
  }

}
