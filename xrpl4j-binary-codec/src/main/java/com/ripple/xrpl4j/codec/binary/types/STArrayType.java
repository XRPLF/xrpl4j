package com.ripple.xrpl4j.codec.binary.types;

import static com.ripple.xrpl4j.codec.binary.types.STObjectType.OBJECT_END_MARKER_HEX;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import com.ripple.xrpl4j.codec.addresses.UnsignedByteArray;
import com.ripple.xrpl4j.codec.binary.BinaryCodecObjectMapperFactory;
import com.ripple.xrpl4j.codec.binary.definitions.FieldInstance;
import com.ripple.xrpl4j.codec.binary.serdes.BinaryParser;
import com.ripple.xrpl4j.codec.binary.serdes.BinarySerializer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;

/**
 * Codec for XRPL STArray type.
 */
public class STArrayType extends SerializedType<STArrayType> {

  public static final String ARRAY_END_MARKER_HEX = "F1";

  public static final String ARRAY_END_MARKER_NAME = "ArrayEndMarker";

  public STArrayType() {
    this(UnsignedByteArray.empty());
  }

  public STArrayType(UnsignedByteArray list) {
    super(list);
  }

  @Override
  public STArrayType fromParser(BinaryParser parser, OptionalInt lengthHint) {
    UnsignedByteArray byteArray = UnsignedByteArray.empty();
    BinarySerializer serializer = new BinarySerializer(byteArray);

    while (!parser.end()) {
      FieldInstance fieldInstance = parser.readField().get();
      if (fieldInstance.name().equals(ARRAY_END_MARKER_NAME)) {
        break;
      }
      SerializedType associatedValue = parser.readFieldValue(fieldInstance);
      serializer.writeFieldAndValue(fieldInstance, associatedValue);
      serializer.put(OBJECT_END_MARKER_HEX);
    }
    serializer.put(ARRAY_END_MARKER_HEX);
    return new STArrayType(byteArray);
  }

  @Override
  public STArrayType fromJSON(JsonNode node) {
    if (!node.isArray()) {
      throw new IllegalArgumentException("node is not an array");
    }
    UnsignedByteArray byteList = UnsignedByteArray.empty();
    BinarySerializer serializer = new BinarySerializer(byteList);

    Iterator<JsonNode> nodeIterator = node.elements();
    while (nodeIterator.hasNext()) {
      JsonNode child = nodeIterator.next();
      serializer.put(new STObjectType().fromJSON(child).value().hexValue());
    }
    serializer.put(ARRAY_END_MARKER_HEX);
    return new STArrayType(byteList);
  }

  @Override
  public JsonNode toJSON() {
    BinaryParser parser = new BinaryParser(this.toString());
    List<JsonNode> values = new ArrayList<>();
    while (!parser.end()) {
      FieldInstance field = parser.readField().orElseThrow(() -> new IllegalArgumentException("bad field encountered"));
      if (field.name().equals(ARRAY_END_MARKER_NAME)) {
        break;
      }
      STObjectType objectType = new STObjectType().fromParser(parser);
      ObjectNode child = new ObjectNode(BinaryCodecObjectMapperFactory.getObjectMapper().getNodeFactory(),
        ImmutableMap.of(field.name(), objectType.toJSON()));
      values.add(child);
    }
    return new ArrayNode(BinaryCodecObjectMapperFactory.getObjectMapper().getNodeFactory(), values);
  }
}
