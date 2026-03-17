package org.xrpl.xrpl4j.codec.binary.definitions;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: binary-codec
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.binary.BinaryCodecObjectMapperFactory;
import org.xrpl.xrpl4j.codec.binary.FieldHeader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Unit tests for {@link DefinitionsService}.
 */
class DefinitionsServiceTest {

  private DefinitionsService definitionsService;

  @BeforeEach
  void setUp() {
    ObjectMapper objectMapper = BinaryCodecObjectMapperFactory.getObjectMapper();
    DefinitionsProvider provider = new DefaultDefinitionsProvider(objectMapper);
    definitionsService = new DefinitionsService(provider, objectMapper);
  }

  @Test
  void getInstanceReturnsSingleton() {
    DefinitionsService instance1 = DefinitionsService.getInstance();
    DefinitionsService instance2 = DefinitionsService.getInstance();
    
    assertThat(instance1).isNotNull();
    assertThat(instance1).isSameAs(instance2);
  }

  @Test
  void getFieldNameReturnsCorrectName() {
    FieldHeader header = FieldHeader.builder()
      .typeCode(2)  // UInt32
      .fieldCode(4) // Sequence
      .build();
    
    String fieldName = definitionsService.getFieldName(header);
    assertThat(fieldName).isEqualTo("Sequence");
  }

  @Test
  void getFieldNameReturnsNullForInvalidHeader() {
    FieldHeader invalidHeader = FieldHeader.builder()
      .typeCode(999)
      .fieldCode(999)
      .build();
    
    String fieldName = definitionsService.getFieldName(invalidHeader);
    assertThat(fieldName).isNull();
  }

  @Test
  void getFieldInfoReturnsInfoForValidField() {
    Optional<FieldInfo> fieldInfo = definitionsService.getFieldInfo("Sequence");
    
    assertThat(fieldInfo).isPresent();
    assertThat(fieldInfo.get().nth()).isEqualTo(4);
    assertThat(fieldInfo.get().type()).isEqualTo("UInt32");
    assertThat(fieldInfo.get().isSerialized()).isTrue();
    assertThat(fieldInfo.get().isSigningField()).isTrue();
  }

  @Test
  void getFieldInfoReturnsEmptyForInvalidField() {
    Optional<FieldInfo> fieldInfo = definitionsService.getFieldInfo("InvalidFieldName");
    assertThat(fieldInfo).isEmpty();
  }

  @Test
  void getTypeOrdinalReturnsCorrectOrdinal() {
    Integer ordinal = definitionsService.getTypeOrdinal("UInt32");
    assertThat(ordinal).isEqualTo(2);
  }

  @Test
  void getTypeOrdinalReturnsNullForInvalidType() {
    Integer ordinal = definitionsService.getTypeOrdinal("InvalidType");
    assertThat(ordinal).isNull();
  }

  @Test
  void getFieldHeaderReturnsHeaderForValidField() {
    Optional<FieldHeader> header = definitionsService.getFieldHeader("Sequence");
    
    assertThat(header).isPresent();
    assertThat(header.get().typeCode()).isEqualTo(2);
    assertThat(header.get().fieldCode()).isEqualTo(4);
  }

  @Test
  void getFieldHeaderReturnsEmptyForInvalidField() {
    Optional<FieldHeader> header = definitionsService.getFieldHeader("InvalidFieldName");
    assertThat(header).isEmpty();
  }

  @Test
  void getFieldInstanceReturnsInstanceForValidField() {
    Optional<FieldInstance> instance = definitionsService.getFieldInstance("Sequence");
    
    assertThat(instance).isPresent();
    assertThat(instance.get().name()).isEqualTo("Sequence");
    assertThat(instance.get().type()).isEqualTo("UInt32");
    assertThat(instance.get().nth()).isEqualTo(4);
    assertThat(instance.get().header().typeCode()).isEqualTo(2);
    assertThat(instance.get().header().fieldCode()).isEqualTo(4);
    assertThat(instance.get().isSerialized()).isTrue();
    assertThat(instance.get().isSigningField()).isTrue();
  }

  @Test
  void getFieldInstanceReturnsEmptyForInvalidField() {
    Optional<FieldInstance> instance = definitionsService.getFieldInstance("InvalidFieldName");
    assertThat(instance).isEmpty();
  }

  @Test
  void mapFieldSpecializationForLedgerEntryType() {
    Optional<Integer> result = definitionsService.mapFieldSpecialization("LedgerEntryType", "AccountRoot");
    assertThat(result).isPresent();
    assertThat(result.get()).isGreaterThanOrEqualTo(0);
  }

  @Test
  void mapFieldSpecializationForTransactionResult() {
    Optional<Integer> result = definitionsService.mapFieldSpecialization("TransactionResult", "tesSUCCESS");
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(0);
  }

  @Test
  void mapFieldSpecializationForTransactionType() {
    Optional<Integer> result = definitionsService.mapFieldSpecialization("TransactionType", "Payment");
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(0);
  }

  @Test
  void mapFieldSpecializationForPermissionValue() {
    Optional<Integer> result = definitionsService.mapFieldSpecialization("PermissionValue", "Payment");
    assertThat(result).isPresent();
    // Payment transaction type is 0, so permission value should be 1
    assertThat(result.get()).isEqualTo(1);
  }

  @Test
  void mapFieldSpecializationForGranularPermission() {
    Optional<Integer> result = definitionsService.mapFieldSpecialization("PermissionValue", "TrustlineAuthorize");
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(65537);
  }

  @Test
  void mapFieldSpecializationReturnsEmptyForUnknownField() {
    Optional<Integer> result = definitionsService.mapFieldSpecialization("UnknownField", "SomeValue");
    assertThat(result).isEmpty();
  }

  @Test
  void mapFieldSpecializationReturnsEmptyForNullFieldName() {
    Optional<Integer> result = definitionsService.mapFieldSpecialization(null, "SomeValue");
    assertThat(result).isEmpty();
  }

  @Test
  void mapFieldSpecializationReturnsEmptyForUnknownValue() {
    Optional<Integer> result = definitionsService.mapFieldSpecialization("TransactionType", "UnknownTxType");
    assertThat(result).isEmpty();
  }

  @Test
  void mapFieldRawValueToSpecializationForLedgerEntryType() {
    Optional<String> result = definitionsService.mapFieldRawValueToSpecialization("LedgerEntryType", "97");
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo("AccountRoot");
  }

  @Test
  void mapFieldRawValueToSpecializationForTransactionResult() {
    Optional<String> result = definitionsService.mapFieldRawValueToSpecialization("TransactionResult", "0");
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo("tesSUCCESS");
  }

  @Test
  void mapFieldRawValueToSpecializationForTransactionType() {
    Optional<String> result = definitionsService.mapFieldRawValueToSpecialization("TransactionType", "0");
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo("Payment");
  }

  @Test
  void mapFieldRawValueToSpecializationForPermissionValue() {
    Optional<String> result = definitionsService.mapFieldRawValueToSpecialization("PermissionValue", "1");
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo("Payment");
  }

  @Test
  void mapFieldRawValueToSpecializationForGranularPermission() {
    Optional<String> result = definitionsService.mapFieldRawValueToSpecialization("PermissionValue", "65537");
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo("TrustlineAuthorize");
  }

  @Test
  void mapFieldRawValueToSpecializationReturnsEmptyForUnknownField() {
    Optional<String> result = definitionsService.mapFieldRawValueToSpecialization("UnknownField", "123");
    assertThat(result).isEmpty();
  }

  @Test
  void mapFieldRawValueToSpecializationReturnsEmptyForNullFieldName() {
    Optional<String> result = definitionsService.mapFieldRawValueToSpecialization(null, "123");
    assertThat(result).isEmpty();
  }

  @Test
  void mapFieldRawValueToSpecializationReturnsEmptyForUnknownValue() {
    Optional<String> result = definitionsService.mapFieldRawValueToSpecialization("TransactionType", "99999");
    assertThat(result).isEmpty();
  }

  @Test
  void testCommonFieldsExist() {
    // Test some common fields to ensure definitions are loaded correctly
    assertThat(definitionsService.getFieldInfo("Account")).isPresent();
    assertThat(definitionsService.getFieldInfo("Amount")).isPresent();
    assertThat(definitionsService.getFieldInfo("Destination")).isPresent();
    assertThat(definitionsService.getFieldInfo("Fee")).isPresent();
    assertThat(definitionsService.getFieldInfo("Flags")).isPresent();
    assertThat(definitionsService.getFieldInfo("SigningPubKey")).isPresent();
    assertThat(definitionsService.getFieldInfo("TransactionType")).isPresent();
  }

  @Test
  void testCommonTypesExist() {
    // Test some common types to ensure definitions are loaded correctly
    assertThat(definitionsService.getTypeOrdinal("UInt8")).isNotNull();
    assertThat(definitionsService.getTypeOrdinal("UInt16")).isNotNull();
    assertThat(definitionsService.getTypeOrdinal("UInt32")).isNotNull();
    assertThat(definitionsService.getTypeOrdinal("UInt64")).isNotNull();
    assertThat(definitionsService.getTypeOrdinal("Amount")).isNotNull();
    assertThat(definitionsService.getTypeOrdinal("Blob")).isNotNull();
    assertThat(definitionsService.getTypeOrdinal("AccountID")).isNotNull();
    assertThat(definitionsService.getTypeOrdinal("STObject")).isNotNull();
    assertThat(definitionsService.getTypeOrdinal("STArray")).isNotNull();
  }

  @Test
  void testConstructorWithInvalidJsonThrowsException() {
    ObjectMapper objectMapper = BinaryCodecObjectMapperFactory.getObjectMapper();

    // Create a custom DefinitionsProvider that returns invalid JSON in the fields array
    DefinitionsProvider invalidProvider = new DefinitionsProvider() {
      @Override
      public Definitions get() {
        // Create an array with invalid field metadata that will cause JsonProcessingException
        // Add an invalid JSON object that cannot be parsed as FieldInfo
        ObjectNode invalidMetadata = JsonNodeFactory.instance.objectNode();
        invalidMetadata.put("invalid_key", "invalid_value"); // Missing required fields like "nth" and "type"

        List<JsonNode> invalidField = Arrays.asList(
          JsonNodeFactory.instance.textNode("InvalidField"),
          invalidMetadata
        );

        List<List<JsonNode>> fieldsArray = new ArrayList<>();
        fieldsArray.add(invalidField);

        return ImmutableDefinitions.builder()
          .putTypes("UInt8", 1)
          .ledgerEntryTypes(java.util.Collections.emptyMap())
          .fields(fieldsArray)
          .transactionResults(java.util.Collections.emptyMap())
          .transactionTypes(java.util.Collections.emptyMap())
          .permissionValues(java.util.Collections.emptyMap())
          .build();
      }
    };

    // Verify that constructing DefinitionsService with invalid JSON throws IllegalArgumentException
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> new DefinitionsService(invalidProvider, objectMapper)
    );

    assertThat(exception.getMessage()).isEqualTo("invalid json");
    assertThat(exception.getCause()).isNotNull();
  }
}