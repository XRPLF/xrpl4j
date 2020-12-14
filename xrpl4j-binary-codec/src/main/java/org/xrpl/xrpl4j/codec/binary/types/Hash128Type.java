package org.xrpl.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.databind.JsonNode;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.serdes.BinaryParser;

/**
 * Codec for XRPL Hash128 type.
 */
public class Hash128Type extends HashType<Hash128Type> {

  public static final int WIDTH = 16;

  public Hash128Type() {
    this(UnsignedByteArray.ofSize(WIDTH));
  }

  public Hash128Type(UnsignedByteArray list) {
    super(list, WIDTH);
  }

  @Override
  public Hash128Type fromParser(BinaryParser parser) {
    return new Hash128Type(parser.read(WIDTH));
  }

  @Override
  public Hash128Type fromJson(JsonNode node) {
    return new Hash128Type(UnsignedByteArray.fromHex(node.asText()));
  }
}
