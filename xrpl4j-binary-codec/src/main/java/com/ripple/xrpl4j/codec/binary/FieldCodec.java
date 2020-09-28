package com.ripple.xrpl4j.codec.binary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.ripple.xrpl4j.codec.binary.definitions.Definitions;
import com.ripple.xrpl4j.codec.binary.definitions.FieldMetadata;
import com.ripple.xrpl4j.codec.binary.types.TypeCodec;
import com.ripple.xrpl4j.codec.binary.types.UInt16Codec;
import com.ripple.xrpl4j.codec.binary.types.UInt32Codec;
import com.ripple.xrpl4j.codec.binary.types.UIntCodec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FieldCodec {

  private final Definitions definitions;

  private final Map<String, FieldMetadata> fieldMetadataMap;

  private final Map<String, Integer> typeOrdinalMap;

  private final Map<String, TypeCodec> typeCodecs = ImmutableMap.<String, TypeCodec>builder()
      .put("UInt32", new UIntCodec(32))
      .put("UInt16", new UIntCodec(16))
      .put("UInt8", new UIntCodec(8))
      .build();

  public FieldCodec(Definitions definitions, ObjectMapper mapper) {
    this.definitions = definitions;
    this.fieldMetadataMap = new HashMap<>();
    this.typeOrdinalMap = definitions.types();
    this.definitions.fields().forEach(field -> {
      try {
        fieldMetadataMap.put(field.get(0).textValue(),
            mapper.readValue(field.get(1).toString(), FieldMetadata.class));
      } catch (JsonProcessingException e) {
        // FIXME logging
      }
    });
  }

  public String encode(String fieldName) {
    return encode(getFieldId(fieldName));
  }

  protected FieldId getFieldId(String fieldName) {
    FieldMetadata metadata = fieldMetadataMap.get(fieldName);
    Preconditions.checkNotNull(metadata, fieldName + " is not a valid field name");
    Integer typeCode = typeOrdinalMap.get(metadata.type());
    Preconditions.checkNotNull(typeCode, typeCode + " is not a valid type");
    return FieldId.builder().typeCode(typeCode).fieldCode(metadata.nth()).build();
  }

  private Map<String, Integer> getSpecializationMapper(String fieldName) {
    if (fieldName.equalsIgnoreCase("TransactionType")) {
      return definitions.transactionTypes();
    }
    else if (fieldName.equalsIgnoreCase("TransactionResult")) {
      return definitions.transactionResults();
    }
    else {
      return new HashMap<>();
    }
  }

  protected String encode(FieldId fieldId) {
    List<UnsignedByte> segments = new ArrayList<>();
    int typeCode = fieldId.typeCode();
    int fieldCode = fieldId.fieldCode();
    if (typeCode < 16) {
      if (fieldCode < 16) {
        segments.add(UnsignedByte.of((byte) typeCode, (byte) fieldCode));
      }
      else {
        segments.add(UnsignedByte.of((byte) typeCode, (byte) 0));
        segments.add(UnsignedByte.of(fieldCode));
      }
    }
    else {
      if (fieldCode < 16) {
        segments.add(UnsignedByte.of((byte) 0, (byte) fieldCode));
        segments.add(UnsignedByte.of(typeCode));
      }
      else {
        // 3 bytes
        segments.add(UnsignedByte.of((byte) 0));
        segments.add(UnsignedByte.of(typeCode));
        segments.add(UnsignedByte.of(fieldCode));
      }
    }
    return ByteUtils.coalesce(segments);

  }

}



