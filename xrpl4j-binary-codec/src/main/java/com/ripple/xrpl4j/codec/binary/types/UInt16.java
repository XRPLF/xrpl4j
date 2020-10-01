package com.ripple.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.codec.binary.serdes.BinaryParser;

import java.util.OptionalInt;

public class UInt16 extends UInt<UInt16> {

  public UInt16() {
    super(UnsignedInteger.ZERO);
  }

  public UInt16(UnsignedInteger value) {
    super(value);
  }

  @Override
  public UInt16 fromParser(BinaryParser parser, OptionalInt lengthHint) {
    return new UInt16(parser.readUInt16());
  }
  @Override
  public UInt16 fromJSON(JsonNode value) {
    return new UInt16(UnsignedInteger.valueOf(value.asText()));
  }

}
