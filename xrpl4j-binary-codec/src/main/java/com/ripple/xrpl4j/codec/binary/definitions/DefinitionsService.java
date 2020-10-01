package com.ripple.xrpl4j.codec.binary.definitions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.ripple.xrpl4j.codec.binary.FieldHeader;
import com.ripple.xrpl4j.codec.binary.ObjectMapperFactory;
import com.ripple.xrpl4j.codec.binary.enums.FieldInfo;
import com.ripple.xrpl4j.codec.binary.enums.FieldInstance;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DefinitionsService {

  private static final DefinitionsService INSTANCE = new DefinitionsService(DefinitionsProvider.getInstance(),
      ObjectMapperFactory.getObjectMapper());

  private final Definitions definitions;

  private final Map<String, FieldInfo> fieldInfoMap;

  private final Map<FieldHeader, String> fieldIdNameMap;

  private final Map<String, Integer> typeOrdinalMap;

  public static DefinitionsService getInstance() {
    return INSTANCE;
  }

  DefinitionsService(DefinitionsProvider definitionsProvider, ObjectMapper mapper) {
    this.definitions = definitionsProvider.get();
    this.typeOrdinalMap = ImmutableMap.copyOf(definitions.types());

    Map<String, FieldInfo> tempFieldInfoMap = new HashMap<>();
    Map<FieldHeader, String> tempFieldIdNameMap = new HashMap<>();
    this.definitions.fields().forEach(field -> {
      try {
        String fieldName = field.get(0).textValue();
        FieldInfo metadata = mapper.readValue(field.get(1).toString(), FieldInfo.class);
        FieldHeader fieldHeader = FieldHeader.builder().fieldCode(metadata.nth())
            .typeCode(typeOrdinalMap.get(metadata.type()))
            .build();
        tempFieldInfoMap.put(fieldName, metadata);
        tempFieldIdNameMap.put(fieldHeader, fieldName);
      } catch (JsonProcessingException e) {
        // FIXME logging
        throw new RuntimeException(e);
      }
    });
    this.fieldInfoMap = ImmutableMap.copyOf(tempFieldInfoMap);
    this.fieldIdNameMap = ImmutableMap.copyOf(tempFieldIdNameMap);
  }

  public String getFieldName(FieldHeader fieldHeader) {
    return fieldIdNameMap.get(fieldHeader);
  }

  public Optional<FieldInfo> getFieldInfo(String fieldName) {
    return Optional.ofNullable(fieldInfoMap.get(fieldName));
  }

  public Integer getTypeOrdinal(String typeName) {
    return typeOrdinalMap.get(typeName);
  }

  public Optional<FieldHeader> getFieldHeader(String fieldName) {
    return getFieldInfo(fieldName).map(info -> {
      Integer typeCode = getTypeOrdinal(info.type());
      Preconditions.checkNotNull(typeCode, typeCode + " is not a valid type");
      return FieldHeader.builder().typeCode(typeCode).fieldCode(info.nth()).build();
    });
  }

  public Optional<FieldInstance> getFieldInstance(String fieldName) {
    return DefinitionsService.getInstance().getFieldInfo(fieldName)
        .flatMap(info -> getFieldHeader(fieldName)
            .map(header ->
                FieldInstance.builder()
                    .header(header)
                    .isSerialized(info.isSerialized())
                    .isSigningField(info.isSigningField())
                    .isVariableLengthEncoded(info.isVLEncoded())
                    .nth(info.nth())
                    .name(fieldName)
                    .type(info.type())
                    .build()));
  }

}