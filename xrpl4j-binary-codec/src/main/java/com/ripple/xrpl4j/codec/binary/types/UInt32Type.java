package com.ripple.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.primitives.UnsignedLong;
import com.ripple.xrpl4j.codec.binary.serdes.BinaryParser;

import java.util.OptionalInt;

/**
 * Codec for XRPL UInt32 type.
 */
public class UInt32Type extends UIntType<UInt32Type> {

  public UInt32Type() {
    this(UnsignedLong.ZERO);
  }

  public UInt32Type(UnsignedLong value) {
    super(value, 32);
  }

  @Override
  public UInt32Type fromParser(BinaryParser parser, OptionalInt lengthHint) {
    return new UInt32Type(parser.readUInt32());
  }
  @Override
  public UInt32Type fromJSON(JsonNode value) {
    return new UInt32Type(UnsignedLong.valueOf(value.asText()));
  }

}
