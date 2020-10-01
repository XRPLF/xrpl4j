package com.ripple.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.codec.binary.serdes.BinaryParser;

import java.util.OptionalInt;

public class UInt32 extends UInt<UInt32> {

  public UInt32() {
    super(UnsignedInteger.ZERO);
  }

  public UInt32(UnsignedInteger value) {
    super(value);
  }

  @Override
  public UInt32 fromParser(BinaryParser parser, OptionalInt lengthHint) {
    return new UInt32(parser.readUInt32());
  }
  @Override
  public UInt32 fromJSON(JsonNode value) {
    return new UInt32(UnsignedInteger.valueOf(value.asText()));
  }

}
