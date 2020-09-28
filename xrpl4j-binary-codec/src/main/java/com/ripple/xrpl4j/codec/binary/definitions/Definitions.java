package com.ripple.xrpl4j.codec.binary.definitions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

import java.util.List;
import java.util.Map;

@Immutable
@JsonSerialize(as = ImmutableDefinitions.class)
@JsonDeserialize(as = ImmutableDefinitions.class)
public interface Definitions {

  @JsonProperty("TYPES")
  Map<String, Integer> types();

  @JsonProperty("FIELDS")
  List<List<JsonNode>> fields();

  @JsonProperty("TRANSACTION_TYPES")
  Map<String, Integer> transactionTypes();

  @JsonProperty("TRANSACTION_RESULTS")
  Map<String, Integer> transactionResults();

}
