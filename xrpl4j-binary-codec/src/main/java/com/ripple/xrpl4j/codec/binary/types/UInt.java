package com.ripple.xrpl4j.codec.binary.types;

import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.codec.binary.serdes.UnsignedByteList;

abstract class UInt<T extends UInt<T>> extends SerializedType<T> {

  private final UnsignedInteger value;

  public UInt(UnsignedInteger value) {
    super(new UnsignedByteList(value.toString(16)));
    this.value = value;
  }

  UnsignedInteger valueOf() {
    return value;
  }

  @Override
  public int compareTo(T o) {
    return this.value.compareTo(o.valueOf());
  }

}
