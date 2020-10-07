package com.ripple.xrpl4j.codec.binary.definitions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.ripple.xrpl4j.codec.binary.FieldHeader;
import com.ripple.xrpl4j.codec.binary.ObjectMapperFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for accessing XRPL type and field metadata from definitions.json.
 */
public class DefinitionsService {

  private static final DefinitionsService INSTANCE = new DefinitionsService(DefinitionsProvider.getInstance(),
      ObjectMapperFactory.getObjectMapper());

  private final Definitions definitions;

  private final Map<String, FieldInfo> fieldInfoMap;

  private final Map<FieldHeader, String> fieldIdNameMap;

  private final Map<String, Integer> typeOrdinalMap;

  private final Map<Integer, String> transactionTypeReverseLookupMap;

  private final Map<Integer, String> transactionResultReverseLookupNap;

  private final Map<Integer, String> ledgerEntryTypeReverseLookupMap;

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
        throw new IllegalArgumentException("invalid json", e);
      }
    });
    this.fieldInfoMap = ImmutableMap.copyOf(tempFieldInfoMap);
    this.fieldIdNameMap = ImmutableMap.copyOf(tempFieldIdNameMap);
    this.transactionTypeReverseLookupMap = inverse(definitions.transactionTypes());
    this.transactionResultReverseLookupNap = inverse(definitions.transactionResults());
    this.ledgerEntryTypeReverseLookupMap = inverse(definitions.ledgerEntryTypes());
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
                    .isVariableLengthEncoded(info.isVariableLengthEncoded())
                    .nth(info.nth())
                    .name(fieldName)
                    .type(info.type())
                    .build()));
  }

  public Optional<Integer> mapFieldSpecialization(String fieldName, String value) {
    if (fieldName == null) {
      return Optional.empty();
    }
    switch (fieldName) {
      case "LedgerEntryType":
        return Optional.ofNullable(definitions.ledgerEntryTypes().get(value));
      case "TransactionResult":
        return Optional.ofNullable(definitions.transactionResults().get(value));
      case "TransactionType":
        return Optional.ofNullable(definitions.transactionTypes().get(value));
      default:
        return Optional.empty();
    }
  }

  public Optional<String> mapFieldRawValueToSpecialization(String fieldName, String value) {
    if (fieldName == null) {
      return Optional.empty();
    }
    switch (fieldName) {
      case "LedgerEntryType":
        return Optional.ofNullable(ledgerEntryTypeReverseLookupMap.get(Integer.valueOf(value)));
      case "TransactionResult":
        return Optional.ofNullable(transactionResultReverseLookupNap.get(Integer.valueOf(value)));
      case "TransactionType":
        return Optional.ofNullable(transactionTypeReverseLookupMap.get(Integer.valueOf(value)));
      default:
        return Optional.empty();
    }
  }

  private Map<Integer, String> inverse(Map<String, Integer> map) {
    return map.entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
  }

}