package com.ripple.xrpl4j.codec.binary;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UnsignedByteArray {

  private final List<UnsignedByte> unsignedBytes;

  public static UnsignedByteArray of(byte[] bytes) {
    List<UnsignedByte> unsignedBytes = new ArrayList<>(bytes.length);
    for (int i = 0; i < bytes.length; i++) {
      unsignedBytes.add(i, UnsignedByte.of(bytes[i]));
    }
    return new UnsignedByteArray(unsignedBytes);
  }

  public UnsignedByteArray(List<UnsignedByte> unsignedBytes) {
    Objects.requireNonNull(unsignedBytes);
    this.unsignedBytes = unsignedBytes;
  }

  public List<UnsignedByte> getUnsignedBytes() {
    return unsignedBytes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof UnsignedByteArray)) {
      return false;
    }
    UnsignedByteArray that = (UnsignedByteArray) o;
    return getUnsignedBytes().equals(that.getUnsignedBytes());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getUnsignedBytes());
  }
}
