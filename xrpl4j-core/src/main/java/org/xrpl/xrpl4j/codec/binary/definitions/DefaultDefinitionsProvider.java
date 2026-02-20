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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.io.Resources;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DefaultDefinitionsProvider implements DefinitionsProvider {

  private final Supplier<Definitions> supplier;

  /**
   * Required-args Constructor.
   *
   * @param objectMapper An {@link ObjectMapper}.
   */
  @SuppressWarnings("UnstableApiUsage")
  public DefaultDefinitionsProvider(final ObjectMapper objectMapper) {
    Objects.requireNonNull(objectMapper);

    this.supplier = Suppliers.memoize(() -> {
      try {
        Definitions baseDefinitions = objectMapper.readerFor(Definitions.class)
          .readValue(Resources.getResource(DefaultDefinitionsProvider.class, "/definitions.json"));

        // Generate PERMISSION_VALUES dynamically from TRANSACTION_TYPES
        Map<String, Integer> permissionValues = generatePermissionValues(baseDefinitions);

        // Return a new Definitions object with the generated PERMISSION_VALUES
        return ImmutableDefinitions.builder()
          .from(baseDefinitions)
          .permissionValues(permissionValues)
          .build();
      } catch (IOException e) {
        throw new IllegalStateException("Cannot read definition.json file", e);
      }
    });
  }

  /**
   * Generate PERMISSION_VALUES mapping from TRANSACTION_TYPES.
   * This follows the same logic as xrpl.js:
   * - Granular permissions start at 65537
   * - Transaction type permissions are transaction type code + 1
   *
   * @param definitions The base {@link Definitions} loaded from definitions.json
   * @return A {@link Map} of permission names to their numeric values
   */
  private Map<String, Integer> generatePermissionValues(Definitions definitions) {
    Map<String, Integer> permissionValues = new HashMap<>();

    // Add granular permissions (starting at 65537)
    permissionValues.put("TrustlineAuthorize", 65537);
    permissionValues.put("TrustlineFreeze", 65538);
    permissionValues.put("TrustlineUnfreeze", 65539);
    permissionValues.put("AccountDomainSet", 65540);
    permissionValues.put("AccountEmailHashSet", 65541);
    permissionValues.put("AccountMessageKeySet", 65542);
    permissionValues.put("AccountTransferRateSet", 65543);
    permissionValues.put("AccountTickSizeSet", 65544);
    permissionValues.put("PaymentMint", 65545);
    permissionValues.put("PaymentBurn", 65546);
    permissionValues.put("MPTokenIssuanceLock", 65547);
    permissionValues.put("MPTokenIssuanceUnlock", 65548);

    // Add transaction type permissions (transaction type code + 1)
    // Exclude Invalid (-1) only
    definitions.transactionTypes().forEach((txType, txCode) -> {
      // Skip Invalid (-1)
      if (txCode >= 0) {
        permissionValues.put(txType, txCode + 1);
      }
    });

    return permissionValues;
  }

  @Override
  public Definitions get() {
    return supplier.get();
  }

}
