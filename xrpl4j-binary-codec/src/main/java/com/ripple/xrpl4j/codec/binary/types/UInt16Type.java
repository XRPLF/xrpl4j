package com.ripple.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.primitives.UnsignedLong;
import com.ripple.xrpl4j.codec.binary.serdes.BinaryParser;

import java.util.OptionalInt;

public class UInt16Type extends UIntType<UInt16Type> {

  public UInt16Type() {
    this(UnsignedLong.ZERO);
  }

  public UInt16Type(UnsignedLong value) {
    super(value, 16);
  }

  @Override
  public UInt16Type fromParser(BinaryParser parser, OptionalInt lengthHint) {
    return new UInt16Type(parser.readUInt16());
  }
  @Override
  public UInt16Type fromJSON(JsonNode value) {
    return new UInt16Type(UnsignedLong.valueOf(value.asText()));
  }

}
