package org.xrpl.xrpl4j.codec.binary.definitions;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: binary-codec
 * %%
 * Copyright (C) 2020 - 2026 XRPL Foundation and its contributors
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
import com.google.common.primitives.UnsignedInteger;
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

  /**
   * AccountSet flag definitions (flag name to integer value).
   *
   * @return A {@link Map} of AccountSet flag names to their integer values.
   */
  @JsonProperty("ACCOUNT_SET_FLAGS")
  Map<String, UnsignedInteger> accountSetFlags();

  /**
   * Ledger entry flag definitions grouped by ledger entry type (flag name to integer value).
   *
   * @return A nested {@link Map} keyed by ledger entry type name, then flag name to integer value.
   */
  @JsonProperty("LEDGER_ENTRY_FLAGS")
  Map<String, Map<String, UnsignedInteger>> ledgerEntryFlags();

  /**
   * Transaction flag definitions grouped by transaction type (flag name to integer value).
   *
   * @return A nested {@link Map} keyed by transaction type name, then flag name to integer value.
   */
  @JsonProperty("TRANSACTION_FLAGS")
  Map<String, Map<String, UnsignedInteger>> transactionFlags();

  /**
   * Ledger entry format definitions (field name/optionality lists per ledger entry type).
   *
   * @return A {@link Map} keyed by ledger entry type name to a {@link List} of {@link FieldFormat}.
   */
  @JsonProperty("LEDGER_ENTRY_FORMATS")
  Map<String, List<FieldFormat>> ledgerEntryFormats();

  /**
   * Transaction format definitions (field name/optionality lists per transaction type).
   *
   * @return A {@link Map} keyed by transaction type name to a {@link List} of {@link FieldFormat}.
   */
  @JsonProperty("TRANSACTION_FORMATS")
  Map<String, List<FieldFormat>> transactionFormats();

  /**
   * Hash of the definitions, as produced by the rippled server_definitions RPC.
   *
   * @return A {@link String} hash.
   */
  @JsonProperty("hash")
  String hash();

}
