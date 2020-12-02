package com.ripple.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.primitives.UnsignedLong;
import com.ripple.xrpl4j.codec.addresses.ByteUtils;
import com.ripple.xrpl4j.codec.addresses.UnsignedByteArray;

/**
 * Base codec for XRPL UInt types.
 */
abstract class UIntType<T extends UIntType<T>> extends SerializedType<T> {

  private final UnsignedLong value;

  public UIntType(UnsignedLong value, int bitSize) {
    super(UnsignedByteArray.fromHex(ByteUtils.padded(value.toString(16), bitSizeToHexLength(bitSize))));
    this.value = value;
  }

  private static int bitSizeToHexLength(int bitSize) {
    return bitSize / 4;
  }

  UnsignedLong valueOf() {
    return value;
  }

  @Override
  public JsonNode toJSON() {
    return new TextNode(UnsignedLong.valueOf(toHex(), 16).toString());
  }
}
