package com.ripple.xrpl4j.codec.binary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.ripple.xrpl4j.codec.addresses.ByteUtils;
import com.ripple.xrpl4j.codec.addresses.UnsignedByte;
import com.ripple.xrpl4j.codec.binary.definitions.Definitions;
import com.ripple.xrpl4j.codec.binary.definitions.DefinitionsProvider;
import com.ripple.xrpl4j.codec.binary.definitions.FieldInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FieldHeaderCodec {

  private static final FieldHeaderCodec INSTANCE = new FieldHeaderCodec(DefinitionsProvider.getInstance().get(),
      BinaryCodecObjectMapperFactory.getObjectMapper());

  private final Definitions definitions;

  private final Map<String, FieldInfo> fieldMetadataMap;

  private final Map<FieldHeader, String> fieldIdNameMap;

  private final Map<String, Integer> typeOrdinalMap;


  public static FieldHeaderCodec getInstance() {
    return INSTANCE;
  }

  public FieldHeaderCodec(Definitions definitions, ObjectMapper mapper) {
    this.definitions = definitions;
    this.fieldMetadataMap = new HashMap<>();
    this.fieldIdNameMap = new HashMap<>();
    this.typeOrdinalMap = definitions.types();
    this.definitions.fields().forEach(field -> {
      try {
        String fieldName = field.get(0).textValue();
        FieldInfo metadata = mapper.readValue(field.get(1).toString(), FieldInfo.class);
        FieldHeader fieldHeader = FieldHeader.builder().fieldCode(metadata.nth())
            .typeCode(typeOrdinalMap.get(metadata.type()))
            .build();
        fieldMetadataMap.put(fieldName, metadata);
        fieldIdNameMap.put(fieldHeader, fieldName);
      } catch (JsonProcessingException e) {
        throw new IllegalArgumentException("invalid json", e);
      }
    });
  }

  public String encode(String fieldName) {
    return encode(getFieldId(fieldName));
  }

  public String decode(String hex) {
    FieldHeader fieldHeader = decodeFieldId(hex);
    return fieldIdNameMap.get(fieldHeader);
  }

  protected FieldHeader decodeFieldId(String hex) {
    Preconditions.checkNotNull(hex, "hex cannot be null");
    Preconditions.checkArgument(hex.length() >= 2, "hex must be at least 2 characters");
    List<UnsignedByte> segments = ByteUtils.parse(hex);
    Preconditions.checkArgument(segments.size() <= 3, "hex value is too large");
    if (segments.size() == 1) {
      UnsignedByte first = segments.get(0);
      return FieldHeader.builder()
          .typeCode(first.getHighBits())
          .fieldCode(first.getLowBits())
          .build();
    }
    if (segments.size() == 2) {
      UnsignedByte first = segments.get(0);
      UnsignedByte second = segments.get(1);
      if (first.getHighBits() == 0) {
        return FieldHeader.builder()
            .fieldCode(first.getLowBits())
            .typeCode(second.asInt())
            .build();
      }
      else {
        return FieldHeader.builder()
            .typeCode(first.getHighBits())
            .fieldCode(second.asInt())
            .build();
      }
    }
    return FieldHeader.builder()
        .typeCode(segments.get(1).asInt())
        .fieldCode(segments.get(2).asInt())
        .build();
  }

  protected FieldHeader getFieldId(String fieldName) {
    FieldInfo metadata = fieldMetadataMap.get(fieldName);
    Preconditions.checkNotNull(metadata, fieldName + " is not a valid field name");
    Integer typeCode = typeOrdinalMap.get(metadata.type());
    Preconditions.checkNotNull(typeCode, typeCode + " is not a valid type");
    return FieldHeader.builder().typeCode(typeCode).fieldCode(metadata.nth()).build();
  }

  protected String encode(FieldHeader fieldHeader) {
    List<UnsignedByte> segments = new ArrayList<>();
    int typeCode = fieldHeader.typeCode();
    int fieldCode = fieldHeader.fieldCode();
    if (typeCode < 16) {
      if (fieldCode < 16) {
        // single byte case where high bits contain type code, low bits contain field code
        segments.add(UnsignedByte.of((byte) typeCode, (byte) fieldCode));
      }
      else {
        // 2 byte case where first byte contains type code + filler, second byte contains field code
        segments.add(UnsignedByte.of((byte) typeCode, (byte) 0));
        segments.add(UnsignedByte.of(fieldCode));
      }
    }
    else {
      if (fieldCode < 16) {
        // 2 byte case where first byte contains filler+field code, second byte contains typeCode
        segments.add(UnsignedByte.of((byte) 0, (byte) fieldCode));
        segments.add(UnsignedByte.of(typeCode));
      }
      else {
        // 3 byte case where first byte is filler, 2nd byte is type code, third byte is field code
        segments.add(UnsignedByte.of((byte) 0));
        segments.add(UnsignedByte.of(typeCode));
        segments.add(UnsignedByte.of(fieldCode));
      }
    }
    return ByteUtils.toHex(segments);
  }

}



