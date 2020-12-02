package com.ripple.xrpl4j.model.client.path;

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

  /**
   * A {@link List} of {@link List}s of {@link PathStep}s containing the different payment paths available.
   */
  @JsonProperty("paths_computed")
  List<List<PathStep>> pathsComputed();

  /**
   * {@link CurrencyAmount} that the source would have to send along this path for the destination to receive the
   * desired amount.
   */
  @JsonProperty("source_amount")
  CurrencyAmount sourceAmount();

}
