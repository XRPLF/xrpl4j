package com.ripple.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;

import java.util.Optional;

/**
 * Create or modify a trust line linking two accounts.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableTrustSet.class)
@JsonDeserialize(as = ImmutableTrustSet.class)
public interface TrustSet extends Transaction<Flags.TrustSetFlags> {

  static ImmutableTrustSet.Builder builder() {
    return ImmutableTrustSet.builder();
  }

  /**
   * The {@link IssuedCurrencyAmount} defining the trust line to create or modify.
   */
  @JsonProperty("LimitAmount")
  IssuedCurrencyAmount limitAmount();

  /**
   * Value incoming balances on this trust line at the ratio of this number per 1,000,000,000 units.
   * A value of 0 is shorthand for treating balances at face value.
   */
  @JsonProperty("QualityIn")
  Optional<UnsignedInteger> qualityIn();

  /**
   * Value outgoing balances on this trust line at the ratio of this number per 1,000,000,000 units.
   * A value of 0 is shorthand for treating balances at face value.
   */
  @JsonProperty("QualityOut")
  Optional<UnsignedInteger> qualityOut();

}
