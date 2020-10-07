package com.ripple.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.Lists;
import com.ripple.xrpl4j.codec.addresses.UnsignedByteArray;
import com.ripple.xrpl4j.codec.binary.ObjectMapperFactory;
import com.ripple.xrpl4j.codec.binary.definitions.DefinitionsService;
import com.ripple.xrpl4j.codec.binary.definitions.FieldInstance;
import com.ripple.xrpl4j.codec.binary.serdes.BinaryParser;
import com.ripple.xrpl4j.codec.binary.serdes.BinarySerializer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

/**
 * Codec for XRPL STObject type.
 */
public class STObjectType extends SerializedType<STObjectType> {

  public static final String OBJECT_END_MARKER_HEX = "E1";
  private static final String OBJECT_END_MARKER = "ObjectEndMarker";
  private static final String ST_OBJECT = "STObject";

  private DefinitionsService definitionsService = DefinitionsService.getInstance();

  public STObjectType() {
    this(UnsignedByteArray.empty());
  }

  public STObjectType(UnsignedByteArray list) {
    super(list);
  }

  @Override
  public STObjectType fromParser(BinaryParser parser, OptionalInt lengthHint) {
    UnsignedByteArray byteArray = UnsignedByteArray.empty();
    BinarySerializer serializer = new BinarySerializer(byteArray);

    while(!parser.end()) {
      FieldInstance field = parser.readField().orElseThrow(() -> new IllegalArgumentException("bad field encountered"));
      if (field.name().equals(OBJECT_END_MARKER)) {
        break;
      }

      SerializedType associatedValue = parser.readFieldValue(field);
      serializer.writeFieldAndValue(field, associatedValue);
      if (field.type().equals(ST_OBJECT)) {
        serializer.put(OBJECT_END_MARKER_HEX);
      }
    }
    return new STObjectType(byteArray);
  }

  @Override
  public STObjectType fromJSON(JsonNode node) {
    UnsignedByteArray byteList = UnsignedByteArray.empty();
    BinarySerializer serializer = new BinarySerializer(byteList);

    List<FieldWithValue<JsonNode>> fields = new ArrayList<>();
    for(String fieldName : Lists.newArrayList(node.fieldNames())) {
      JsonNode fieldNode = node.get(fieldName);
      definitionsService.getFieldInstance(fieldName)
          .filter(FieldInstance::isSerialized)
          .ifPresent(fieldInstance -> fields.add(FieldWithValue.builder()
              .field(fieldInstance)
              .value(mapSpecializedValues(fieldName, fieldNode))
              .build()));
    }
    fields.stream()
        .sorted()
        .forEach(value -> {
          try {
            serializer.writeFieldAndValue(value.field(), value.value());
          } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("invalid json", e);
          }
          if (value.field().type().equals(ST_OBJECT)) {
            serializer.put(OBJECT_END_MARKER_HEX);
          }
        });

    return new STObjectType(byteList);
  }

  /**
   * Maps (if necessar) a JSON node for the given fieldName to it's canonical value.
   * Some fields (e.g. TransactionType) can be specified in JSON as an ordinal value or an enum (e.g. OfferCreate).
   * Enum values need to be converted to the ordinal value for binary serialization.
   *
   * @param fieldName name of the JSON field.
   * @param fieldNode JSON value for the field.
   * @return either the original fieldNode or a remapped node if it's one of these special cases.
   */
  private JsonNode mapSpecializedValues(String fieldName, JsonNode fieldNode) {
    return definitionsService.mapFieldSpecialization(fieldName, fieldNode.asText())
        .map(value -> new TextNode("" + value))
        .map(JsonNode.class::cast)
        .orElse(fieldNode);
  }

  public JsonNode toJSON() {
    BinaryParser parser = new BinaryParser(this.toString());
    Map<String, JsonNode> objectMap = new LinkedHashMap<>();
    while(!parser.end()) {
      FieldInstance field = parser.readField().orElseThrow(() -> new IllegalArgumentException("bad field encountered"));
      if (field.name().equals(OBJECT_END_MARKER)) {
        break;
      }
      JsonNode value = parser.readFieldValue(field).toJSON();
      JsonNode mapped = definitionsService.mapFieldRawValueToSpecialization(field.name(), value.asText())
          .map(TextNode::new)
          .map(JsonNode.class::cast)
          .orElse(value);
      objectMap.put(field.name(), mapped);
    }
    return new ObjectNode(ObjectMapperFactory.getObjectMapper().getNodeFactory(), objectMap);
  }

}
