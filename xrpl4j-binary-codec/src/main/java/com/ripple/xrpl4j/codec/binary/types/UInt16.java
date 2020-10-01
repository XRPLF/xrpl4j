package com.ripple.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.primitives.UnsignedLong;
import com.ripple.xrpl4j.codec.binary.serdes.BinaryParser;

import java.util.OptionalInt;

public class UInt16 extends UInt<UInt16> {

  public UInt16() {
    this(UnsignedLong.ZERO);
  }

  public UInt16(UnsignedLong value) {
    super(value, 16);
  }

  @Override
  public UInt16 fromParser(BinaryParser parser, OptionalInt lengthHint) {
    return new UInt16(parser.readUInt16());
  }
  @Override
  public UInt16 fromJSON(JsonNode value) {
    return new UInt16(UnsignedLong.valueOf(value.asText()));
  }

}
