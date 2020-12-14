package org.xrpl.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.codec.binary.serdes.BinaryParser;

/**
 * Codec for XRPL UInt16 type.
 */
public class UInt16Type extends UIntType<UInt16Type> {

  public UInt16Type() {
    this(UnsignedLong.ZERO);
  }

  public UInt16Type(UnsignedLong value) {
    super(value, 16);
  }

  @Override
  public UInt16Type fromParser(BinaryParser parser) {
    return new UInt16Type(parser.readUInt16());
  }

  @Override
  public UInt16Type fromJSON(JsonNode value) {
    return new UInt16Type(UnsignedLong.valueOf(value.asText()));
  }

  @Override
  public JsonNode toJSON() {
    return new IntNode(UnsignedLong.valueOf(toHex(), 16).intValue());
  }

}
