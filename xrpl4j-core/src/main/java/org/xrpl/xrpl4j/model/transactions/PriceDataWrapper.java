package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;

/**
 * Wrapper object for {@link PriceData}, because the PriceData field is an STArray which, in JSON, has elements of
 * wrapped objects.
 */
@Immutable
@JsonSerialize(as = ImmutablePriceDataWrapper.class)
@JsonDeserialize(as = ImmutablePriceDataWrapper.class)
public interface PriceDataWrapper {

  static PriceDataWrapper of(PriceData priceData) {
    return ImmutablePriceDataWrapper.builder().priceData(priceData).build();
  }

  /**
   * The price data.
   *
   * @return A {@link PriceData}.
   */
  @JsonProperty("PriceData")
  PriceData priceData();

}
