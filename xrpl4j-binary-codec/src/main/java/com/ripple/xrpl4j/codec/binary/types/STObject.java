package com.ripple.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.ripple.xrpl4j.codec.binary.ObjectMapperFactory;
import com.ripple.xrpl4j.codec.binary.definitions.DefinitionsService;
import com.ripple.xrpl4j.codec.binary.enums.FieldInstance;
import com.ripple.xrpl4j.codec.binary.serdes.BinaryParser;
import com.ripple.xrpl4j.codec.binary.serdes.BinarySerializer;
import com.ripple.xrpl4j.codec.binary.serdes.UnsignedByteList;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

public class STObject extends SerializedType<STObject> {

  private static final String OBJECT_END_MARKER = "ObjectEndMarker";
  private static final String OBJECT_END_MARKER_BYTES = "E1";
  private static final String ST_OBJECT = "STObject";

  public STObject() {
    this(new UnsignedByteList());
  }

  public STObject(UnsignedByteList list) {
    super(list);
  }

  @Override
  public int compareTo(STObject o) {
    return 0; // FIXME how to sort?
  }

  @Override
  public STObject fromParser(BinaryParser parser, OptionalInt lengthHint) {
    UnsignedByteList list = new UnsignedByteList();
    BinarySerializer serializer = new BinarySerializer(list);

    while(!parser.end()) {
      FieldInstance field = parser.readField().orElseThrow(() -> new IllegalArgumentException("bad field encountered"));
      if (field.name().equals(OBJECT_END_MARKER)) {
        break;
      }

      SerializedType associatedValue = parser.readFieldValue(field);
      serializer.writeFieldAndValue(field, associatedValue);
      if (field.type().equals(ST_OBJECT)) {
        serializer.put(OBJECT_END_MARKER_BYTES);
      }
    }
    return new STObject(list);
  }

  @Override
  public STObject fromJSON(JsonNode node) {
    UnsignedByteList byteList = new UnsignedByteList();
    BinarySerializer serializer = new BinarySerializer(byteList);

    // TODO handle xADDRESS to classic address / tag mapping
    List<FieldWithValue<JsonNode>> fields = new ArrayList<>();
    for(String fieldName : Lists.newArrayList(node.fieldNames())) {
      JsonNode fieldNode = node.get(fieldName);
      DefinitionsService.getInstance().getFieldInstance(fieldName)
          .filter(FieldInstance::isSerialized)
          .ifPresent(fieldInstance -> fields.add(FieldWithValue.builder()
              .field(fieldInstance)
              .value(fieldNode)
              .build()));
    }
    fields.stream()
        .sorted()
        .forEach(value -> {
          serializer.writeFieldAndValue(value.field(), value.value());
          if (value.field().type().equals(ST_OBJECT)) {
            serializer.put(OBJECT_END_MARKER_BYTES);
          }
        });

    return new STObject(byteList);
  }

  public JsonNode toJSON() {
    BinaryParser parser = new BinaryParser(this.toString());
    Map<String, JsonNode> objectMap = new LinkedHashMap<>();
    while(!parser.end()) {
      FieldInstance field = parser.readField().orElseThrow(() -> new IllegalArgumentException("bad field encountered"));
      if (field.name().equals(OBJECT_END_MARKER)) {
        break;
      }
      objectMap.put(field.name(), parser.readFieldValue(field).toJSON());
    }
    return new ObjectNode(ObjectMapperFactory.getObjectMapper().getNodeFactory(), objectMap);
  }

}
