package org.xrpl.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.databind.JsonNode;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.serdes.BinaryParser;

import java.util.regex.Pattern;

/**
 * Codec for XRPL Hash160 type.
 */
public class Hash160Type extends HashType<Hash160Type> {

  public static final int WIDTH = 20;
  protected static final Pattern HEX_REGEX = Pattern.compile("^[A-Z0-9]{40}$");

  public Hash160Type() {
    this(UnsignedByteArray.ofSize(WIDTH));
  }

  public Hash160Type(UnsignedByteArray list) {
    super(list, WIDTH);
  }

  @Override
  public Hash160Type fromParser(BinaryParser parser) {
    return new Hash160Type(parser.read(WIDTH));
  }

  @Override
  public Hash160Type fromJSON(JsonNode node) {
    return new Hash160Type(UnsignedByteArray.fromHex(node.asText()));
  }
}
