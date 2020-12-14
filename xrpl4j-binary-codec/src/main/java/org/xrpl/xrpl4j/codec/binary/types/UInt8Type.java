package org.xrpl.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.codec.binary.serdes.BinaryParser;

/**
 * Codec for XRPL UInt8 type.
 */
public class UInt8Type extends UIntType<UInt8Type> {

  public UInt8Type() {
    this(UnsignedLong.ZERO);
  }

  public UInt8Type(UnsignedLong value) {
    super(value, 8);
  }

  @Override
  public UInt8Type fromParser(BinaryParser parser) {
    return new UInt8Type(parser.readUInt8());
  }

  @Override
  public UInt8Type fromJson(JsonNode value) {
    return new UInt8Type(UnsignedLong.valueOf(value.asText()));
  }

  @Override
  public JsonNode toJson() {
    return new IntNode(UnsignedLong.valueOf(toHex(), 16).intValue());
  }

}
