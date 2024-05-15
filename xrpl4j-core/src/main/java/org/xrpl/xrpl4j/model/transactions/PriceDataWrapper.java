package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;

@Immutable
@JsonSerialize(as = ImmutablePriceDataWrapper.class)
@JsonDeserialize(as = ImmutablePriceDataWrapper.class)
public interface PriceDataWrapper {

  static PriceDataWrapper of(PriceData priceData) {
    return ImmutablePriceDataWrapper.builder().priceData(priceData).build();
  }

  @JsonProperty("PriceData")
  PriceData priceData();

}
