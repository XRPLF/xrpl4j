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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;

import java.util.List;
import java.util.Map;

/**
 * Type to represent a the top-level object in definitions.json file.
 */
@Immutable
@JsonSerialize(as = ImmutableDefinitions.class)
@JsonDeserialize(as = ImmutableDefinitions.class)
public interface Definitions {

  /**
   * Type definitions map (type name to ordinal value).
   *
   * @return A {@link Map} of types.
   */
  @JsonProperty("TYPES")
  Map<String, Integer> types();

  /**
   * Fields definitions list.
   *
   * @return {@link List} of type {@link List} of type {@link JsonNode} containing all field definitions.
   */
  @JsonProperty("FIELDS")
  List<List<JsonNode>> fields();

  /**
   * Ledger types mappings (transaction type to ordinal value).
   *
   * @return {@link Map} keyed by {@link String} with {@link Integer} values for all ledger entry types.
   */
  @JsonProperty("LEDGER_ENTRY_TYPES")
  Map<String, Integer> ledgerEntryTypes();

  /**
   * Transaction types mappings (transaction type to ordinal value).
   *
   * @return {@link Map} keyed by {@link String} with {@link Integer} values for all transaction types.
   */
  @JsonProperty("TRANSACTION_TYPES")
  Map<String, Integer> transactionTypes();

  /**
   * Transaction results mappings (transaction result to ordinal value).
   *
   * @return {@link Map} keyed by {@link String} with {@link Integer} values for all transaction results.
   */
  @JsonProperty("TRANSACTION_RESULTS")
  Map<String, Integer> transactionResults();

}
