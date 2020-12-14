package org.xrpl.xrpl4j.codec.binary.definitions;

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
