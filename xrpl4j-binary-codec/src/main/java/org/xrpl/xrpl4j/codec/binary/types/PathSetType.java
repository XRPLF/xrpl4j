package org.xrpl.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByte;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.BinaryCodecObjectMapperFactory;
import org.xrpl.xrpl4j.codec.binary.serdes.BinaryParser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Codec for XRPL PathSet type.
 */
public class PathSetType extends SerializedType<PathSetType> {

  /**
   * Constants for separating Paths in a PathSet.
   */
  public static final String PATHSET_END_HEX = "00";
  public static final String PATH_SEPARATOR_HEX = "FF";

  public PathSetType() {
    this(UnsignedByteArray.empty());
  }

  public PathSetType(UnsignedByteArray list) {
    super(list);
  }

  @Override
  public PathSetType fromParser(BinaryParser parser) {
    UnsignedByteArray byteArray = UnsignedByteArray.empty();
    while (parser.hasMore()) {
      byteArray.append(new PathType().fromParser(parser).value());
      UnsignedByteArray nextByte = parser.read(1);
      byteArray.append(nextByte);
      if (nextByte.hexValue().equals(PATHSET_END_HEX)) {
        break;
      }
    }
    return new PathSetType(byteArray);
  }

  @Override
  public PathSetType fromJson(JsonNode node) throws JsonProcessingException {
    if (!node.isArray()) {
      throw new IllegalArgumentException("node is not an array");
    }

    UnsignedByteArray byteArray = UnsignedByteArray.empty();

    Iterator<JsonNode> nodeIterator = node.elements();
    while (nodeIterator.hasNext()) {
      JsonNode child = nodeIterator.next();
      byteArray.append(new PathType().fromJson(child).value());
      byteArray.append(UnsignedByteArray.fromHex(PATH_SEPARATOR_HEX));
    }

    byteArray.set(byteArray.length() - 1, UnsignedByte.of(PATHSET_END_HEX));
    return new PathSetType(byteArray);
  }

  @Override
  public JsonNode toJson() {
    BinaryParser parser = new BinaryParser(this.toString());
    List<JsonNode> values = new ArrayList<>();

    while (parser.hasMore()) {
      values.add(new PathType().fromParser(parser).toJson());
      parser.skip(1);
    }
    return new ArrayNode(BinaryCodecObjectMapperFactory.getObjectMapper().getNodeFactory(), values);
  }
}
