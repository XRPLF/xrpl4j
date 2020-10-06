package com.ripple.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.databind.JsonNode;
import com.ripple.xrpl4j.codec.binary.serdes.BinaryParser;
import com.ripple.xrpl4j.codec.binary.serdes.UnsignedByteList;

import java.util.OptionalInt;

public class Hash160Type extends HashType<Hash160Type> {

  public static final int WIDTH = 20;

  public Hash160Type() {
    this(new UnsignedByteList(WIDTH));
  }

  public Hash160Type(UnsignedByteList list) {
    super(list, WIDTH);
  }

  @Override
  public Hash160Type fromParser(BinaryParser parser, OptionalInt lengthHint) {
    return new Hash160Type(parser.read(WIDTH));
  }

  @Override
  public Hash160Type fromJSON(JsonNode node) {
    return new Hash160Type(new UnsignedByteList(node.asText()));
  }
}
