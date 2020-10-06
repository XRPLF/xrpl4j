package com.ripple.xrpl4j.codec.binary.types;

import com.google.common.base.Preconditions;
import com.ripple.xrpl4j.codec.binary.serdes.UnsignedByteList;

public abstract class HashType<T extends HashType<T>> extends SerializedType<T> {
  
  private final int width;


  public HashType(UnsignedByteList bytes, int width) {
    super(bytes);
    Preconditions.checkArgument(bytes.getLength() == width, "Invalid hash length " + bytes.getLength());
    this.width = width;
  }

  @Override
  public String toString() {
    return this.toHex();
  }

  /**
   * Returns four bits at the specified depth within a hash
   *
   * @param depth The depth of the four bits
   * @returns The number represented by the four bits
   */
  public int nibblet(int depth) {
    int byteIx = depth > 0 ? (depth / 2) | 0 : 0;
    int b = this.value().get(byteIx).asInt();
    if (depth % 2 == 0) {
      b = (b & 0xf0) >>> 4;
    } else {
      b = b & 0x0f;
    }
    return b;
  }

}
