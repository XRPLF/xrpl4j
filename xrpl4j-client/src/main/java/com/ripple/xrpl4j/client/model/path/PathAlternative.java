package com.ripple.xrpl4j.client.model.path;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ripple.xrpl4j.model.transactions.CurrencyAmount;
import com.ripple.xrpl4j.model.transactions.PathStep;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutablePathAlternative.class)
@JsonDeserialize(as = ImmutablePathAlternative.class)
public interface PathAlternative {

  @JsonProperty("paths_computed")
  List<List<PathStep>> pathsComputed();

  @JsonProperty("source_amount")
  CurrencyAmount sourceAmount();

}
