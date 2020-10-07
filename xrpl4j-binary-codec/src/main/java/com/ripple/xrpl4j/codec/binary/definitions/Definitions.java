package com.ripple.xrpl4j.codec.binary.definitions;

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
   * Type definitions map (type name -> ordinal value)
   *
   * @return
   */
  @JsonProperty("TYPES")
  Map<String, Integer> types();

  /**
   * Fields definitions list.
   *
   * @return
   */
  @JsonProperty("FIELDS")
  List<List<JsonNode>> fields();

  /**
   * Ledger types mappings (transaction type -> ordinal value)
   *
   * @return
   */
  @JsonProperty("LEDGER_ENTRY_TYPES")
  Map<String, Integer> ledgerEntryTypes();

  /**
   * Transaction types mappings (transaction type -> ordinal value)
   *
   * @return
   */
  @JsonProperty("TRANSACTION_TYPES")
  Map<String, Integer> transactionTypes();

  /**
   * Transaction results mappings (transaction result -> ordinal value)
   *
   * @return
   */
  @JsonProperty("TRANSACTION_RESULTS")
  Map<String, Integer> transactionResults();

}
