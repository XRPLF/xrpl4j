package com.ripple.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.databind.JsonNode;
import com.ripple.xrpl4j.codec.binary.serdes.BinaryParser;
import com.ripple.xrpl4j.codec.binary.serdes.UnsignedByteList;

import java.util.OptionalInt;

public class Hash256Type extends HashType<Hash256Type> {

  public static final int WIDTH = 32;

  public Hash256Type() {
    this(new UnsignedByteList(WIDTH));
  }

  public Hash256Type(UnsignedByteList list) {
    super(list, WIDTH);
  }

  @Override
  public Hash256Type fromParser(BinaryParser parser, OptionalInt lengthHint) {
    return new Hash256Type(parser.read(WIDTH));
  }

  @Override
  public Hash256Type fromJSON(JsonNode node) {
    return new Hash256Type(new UnsignedByteList(node.asText()));
  }
}
