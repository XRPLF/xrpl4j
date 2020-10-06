package com.ripple.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.databind.JsonNode;
import com.ripple.xrpl4j.codec.binary.serdes.BinaryParser;
import com.ripple.xrpl4j.codec.binary.serdes.UnsignedByteList;

import java.util.OptionalInt;

public class Hash128Type extends HashType<Hash128Type> {

  public static final int WIDTH = 16;

  public Hash128Type() {
    this(new UnsignedByteList(WIDTH));
  }

  public Hash128Type(UnsignedByteList list) {
    super(list, WIDTH);
  }

  @Override
  public Hash128Type fromParser(BinaryParser parser, OptionalInt lengthHint) {
    return new Hash128Type(parser.read(WIDTH));
  }

  @Override
  public Hash128Type fromJSON(JsonNode node) {
    return new Hash128Type(new UnsignedByteList(node.asText()));
  }
}
