package com.ripple.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.primitives.UnsignedLong;
import com.ripple.xrpl4j.codec.binary.serdes.BinaryParser;

import java.util.OptionalInt;

public class UInt64 extends UInt<UInt64> {

  public UInt64() {
    this(UnsignedLong.ZERO);
  }

  public UInt64(UnsignedLong value) {
    super(value, 64);
  }

  @Override
  public UInt64 fromParser(BinaryParser parser, OptionalInt lengthHint) {
    return new UInt64(parser.readUInt64());
  }
  @Override
  public UInt64 fromJSON(JsonNode value) {
    return new UInt64(UnsignedLong.valueOf(value.asText()));
  }

}
