package com.ripple.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.ripple.xrpl4j.codec.addresses.UnsignedByteArray;
import com.ripple.xrpl4j.codec.binary.BinaryCodecObjectMapperFactory;
import com.ripple.xrpl4j.codec.binary.serdes.BinaryParser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;

/**
 * Codec for XRPL Path type inside a PathSet.
 */
public class PathType extends SerializedType<PathType> {

  /**
   * Constants for separating Paths in a PathSet
   */
  public static final String PATHSET_END_HEX = "00";
  public static final String PATH_SEPARATOR_HEX = "FF";

  public PathType() {
    this(UnsignedByteArray.empty());
  }

  public PathType(UnsignedByteArray list) {
    super(list);
  }

  @Override
  public PathType fromParser(BinaryParser parser, OptionalInt lengthHint) {
    UnsignedByteArray byteArray = UnsignedByteArray.empty();

    while (!parser.end()) {
      byteArray.append(new HopType().fromParser(parser).value());
      String nextByte = parser.peek().hexValue();
      if (nextByte.equals(PATH_SEPARATOR_HEX) || nextByte.equals(PATHSET_END_HEX)) {
        break;
      }
    }
    return new PathType(byteArray);
  }

  @Override
  public PathType fromJSON(JsonNode node) throws JsonProcessingException {
    if (!node.isArray()) {
      throw new IllegalArgumentException("node is not an object");
    }
    UnsignedByteArray byteArray = UnsignedByteArray.empty();
    Iterator<JsonNode> nodeIterator = node.elements();
    while (nodeIterator.hasNext()) {
      JsonNode child = nodeIterator.next();
      byteArray.append(new HopType().fromJSON(child).value());
    }
    return new PathType(byteArray);
  }

  @Override
  public JsonNode toJSON() {
    List<JsonNode> values = new ArrayList<>();
    BinaryParser parser = new BinaryParser(this.toHex());
    while (!parser.end()) {
      values.add(new HopType().fromParser(parser).toJSON());
    }
    return new ArrayNode(BinaryCodecObjectMapperFactory.getObjectMapper().getNodeFactory(), values);
  }
}
