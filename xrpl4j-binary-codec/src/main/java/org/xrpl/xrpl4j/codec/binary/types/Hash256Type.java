package org.xrpl.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.databind.JsonNode;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.serdes.BinaryParser;

/**
 * Codec for XRPL Hash256 type.
 */
public class Hash256Type extends HashType<Hash256Type> {

  public static final int WIDTH = 32;

  public Hash256Type() {
    this(UnsignedByteArray.ofSize(WIDTH));
  }

  public Hash256Type(UnsignedByteArray list) {
    super(list, WIDTH);
  }

  @Override
  public Hash256Type fromParser(BinaryParser parser) {
    return new Hash256Type(parser.read(WIDTH));
  }

  @Override
  public Hash256Type fromJson(JsonNode node) {
    return new Hash256Type(UnsignedByteArray.fromHex(node.asText()));
  }
}
