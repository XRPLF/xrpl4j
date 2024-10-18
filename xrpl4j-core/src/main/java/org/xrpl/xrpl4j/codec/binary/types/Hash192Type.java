package org.xrpl.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.databind.JsonNode;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.serdes.BinaryParser;

/**
 * Codec for XRPL Hash192 type.
 */
public class Hash192Type extends HashType<Hash192Type> {

  public static final int WIDTH = 24;

  public Hash192Type() {
    this(UnsignedByteArray.ofSize(WIDTH));
  }

  public Hash192Type(UnsignedByteArray list) {
    super(list, WIDTH);
  }

  @Override
  public Hash192Type fromParser(BinaryParser parser) {
    return new Hash192Type(parser.read(WIDTH));
  }

  @Override
  public Hash192Type fromJson(JsonNode node) {
    return new Hash192Type(UnsignedByteArray.fromHex(node.asText()));
  }
}
