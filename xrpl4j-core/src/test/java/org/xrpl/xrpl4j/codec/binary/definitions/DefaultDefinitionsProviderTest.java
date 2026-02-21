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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.transactions.GranularPermission;

import java.util.Map;

/**
 * Unit test for {@link DefaultDefinitionsProvider}.
 */
class DefaultDefinitionsProviderTest {

  private DefaultDefinitionsProvider provider;
  private Definitions definitions;

  @BeforeEach
  void setUp() {
    ObjectMapper objectMapper = ObjectMapperFactory.create();
    provider = new DefaultDefinitionsProvider(objectMapper);
    definitions = provider.get();
  }

  @Test
  void getReturnsDefinitions() {
    assertThat(definitions).isNotNull();
    assertThat(definitions.types()).isNotEmpty();
    assertThat(definitions.fields()).isNotEmpty();
    assertThat(definitions.ledgerEntryTypes()).isNotEmpty();
    assertThat(definitions.transactionTypes()).isNotEmpty();
    assertThat(definitions.transactionResults()).isNotEmpty();
    assertThat(definitions.permissionValues()).isNotEmpty();
  }

  @Test
  void permissionValuesContainsAllGranularPermissions() {
    Map<String, Integer> permissionValues = definitions.permissionValues();

    // Verify all granular permissions are present with correct values
    assertThat(permissionValues).containsEntry("TrustlineAuthorize", 65537);
    assertThat(permissionValues).containsEntry("TrustlineFreeze", 65538);
    assertThat(permissionValues).containsEntry("TrustlineUnfreeze", 65539);
    assertThat(permissionValues).containsEntry("AccountDomainSet", 65540);
    assertThat(permissionValues).containsEntry("AccountEmailHashSet", 65541);
    assertThat(permissionValues).containsEntry("AccountMessageKeySet", 65542);
    assertThat(permissionValues).containsEntry("AccountTransferRateSet", 65543);
    assertThat(permissionValues).containsEntry("AccountTickSizeSet", 65544);
    assertThat(permissionValues).containsEntry("PaymentMint", 65545);
    assertThat(permissionValues).containsEntry("PaymentBurn", 65546);
    assertThat(permissionValues).containsEntry("MPTokenIssuanceLock", 65547);
    assertThat(permissionValues).containsEntry("MPTokenIssuanceUnlock", 65548);

    // Verify count matches GranularPermission enum
    int granularPermissionCount = GranularPermission.values().length;
    long granularPermissionsInMap = permissionValues.entrySet().stream()
      .filter(entry -> entry.getValue() >= 65537)
      .count();
    assertThat(granularPermissionsInMap).isEqualTo(granularPermissionCount);
  }

  @Test
  void permissionValuesContainsTransactionTypePermissions() {
    Map<String, Integer> permissionValues = definitions.permissionValues();
    Map<String, Integer> transactionTypes = definitions.transactionTypes();

    // Verify transaction type permissions are transaction type code + 1
    // For example, if Payment has code 0, its permission value should be 1
    transactionTypes.forEach((txType, txCode) -> {
      if (txCode >= 0) {
        // Skip Invalid (-1), all others should be present
        assertThat(permissionValues).containsEntry(txType, txCode + 1);
      }
    });
  }

  @Test
  void permissionValuesExcludesInvalidTransactionType() {
    Map<String, Integer> permissionValues = definitions.permissionValues();
    Map<String, Integer> transactionTypes = definitions.transactionTypes();

    // Find Invalid transaction type (should have code -1)
    transactionTypes.forEach((txType, txCode) -> {
      if (txCode == -1) {
        // Invalid should not be in permission values
        assertThat(permissionValues).doesNotContainKey(txType);
      }
    });
  }



  @Test
  void permissionValuesDoesNotContainNegativeTransactionCodes() {
    Map<String, Integer> permissionValues = definitions.permissionValues();

    // All permission values should be positive
    permissionValues.forEach((name, value) -> {
      assertThat(value).isGreaterThan(0);
    });
  }

  @Test
  void permissionValuesHasCorrectSize() {
    Map<String, Integer> permissionValues = definitions.permissionValues();
    Map<String, Integer> transactionTypes = definitions.transactionTypes();

    // Count valid transaction types (code >= 0)
    long validTransactionTypes = transactionTypes.values().stream()
      .filter(code -> code >= 0)
      .count();

    // Total should be granular permissions + valid transaction types
    int expectedSize = GranularPermission.values().length + (int) validTransactionTypes;
    assertThat(permissionValues).hasSize(expectedSize);
  }

  @Test
  void getReturnsSameInstanceOnMultipleCalls() {
    // Verify memoization - should return the same instance
    Definitions first = provider.get();
    Definitions second = provider.get();
    assertThat(first).isSameAs(second);
  }

  @Test
  void permissionValuesContainsCommonTransactionTypes() {
    Map<String, Integer> permissionValues = definitions.permissionValues();

    // Verify some common transaction types are present
    // Payment is typically transaction type 0, so permission should be 1
    assertThat(permissionValues).containsKey("Payment");
    assertThat(permissionValues.get("Payment")).isGreaterThan(0);

    // TrustSet is typically transaction type 20, so permission should be 21
    assertThat(permissionValues).containsKey("TrustSet");
    assertThat(permissionValues.get("TrustSet")).isGreaterThan(0);

    // OfferCreate is typically transaction type 7, so permission should be 8
    assertThat(permissionValues).containsKey("OfferCreate");
    assertThat(permissionValues.get("OfferCreate")).isGreaterThan(0);
  }

  @Test
  void granularPermissionsStartAt65537() {
    Map<String, Integer> permissionValues = definitions.permissionValues();

    // All granular permissions should be >= 65537
    for (GranularPermission permission : GranularPermission.values()) {
      Integer value = permissionValues.get(permission.value());
      assertThat(value).isNotNull();
      assertThat(value).isGreaterThanOrEqualTo(65537);
    }
  }

  @Test
  void transactionTypePermissionsAreLessThan65537() {
    Map<String, Integer> permissionValues = definitions.permissionValues();
    Map<String, Integer> transactionTypes = definitions.transactionTypes();

    // All transaction type permissions should be < 65537
    transactionTypes.forEach((txType, txCode) -> {
      if (txCode >= 0) {
        Integer permissionValue = permissionValues.get(txType);
        assertThat(permissionValue).isNotNull();
        assertThat(permissionValue).isLessThan(65537);
        assertThat(permissionValue).isEqualTo(txCode + 1);
      }
    });
  }
}
