package com.ripple.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.primitives.UnsignedLong;
import com.ripple.xrpl4j.codec.binary.serdes.BinaryParser;

import java.util.OptionalInt;

public class UInt8 extends UInt<UInt8> {

  @Override
  public UInt8 fromParser(BinaryParser parser, OptionalInt lengthHint) {
    return new UInt8(parser.readUInt8());
  }

  @Override
  public UInt8 fromJSON(JsonNode value) {
    return new UInt8(UnsignedLong.valueOf(value.asText()));
  }

  public UInt8() {
    this(UnsignedLong.ZERO);
  }

  public UInt8(UnsignedLong value) {
    super(value, 8);
  }

}
