package com.ripple.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.primitives.UnsignedLong;
import com.ripple.xrpl4j.codec.binary.serdes.BinaryParser;

import java.util.OptionalInt;

public class UInt64Type extends UIntType<UInt64Type> {

  public UInt64Type() {
    this(UnsignedLong.ZERO);
  }

  public UInt64Type(UnsignedLong value) {
    super(value, 64);
  }

  @Override
  public UInt64Type fromParser(BinaryParser parser, OptionalInt lengthHint) {
    return new UInt64Type(parser.readUInt64());
  }
  @Override
  public UInt64Type fromJSON(JsonNode value) {
    return new UInt64Type(UnsignedLong.valueOf(value.asText()));
  }

}
