package com.ripple.xrplj4.client.model.fees;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableFeeLevels.class)
@JsonDeserialize(as = ImmutableFeeLevels.class)
public interface FeeLevels {

  static ImmutableFeeLevels.Builder builder() {
    return ImmutableFeeLevels.builder();
  }

  @JsonProperty("median_level")
  String medianLevel();

  @JsonProperty("minimum_level")
  String minimumLevel();

  @JsonProperty("open_ledger_level")
  String openLedgerLevel();

  @JsonProperty("reference_level")
  String referenceLevel();

}
