package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.Flags;

import java.util.Optional;

/**
 * Create or modify a trust line linking two accounts.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableTrustSet.class)
@JsonDeserialize(as = ImmutableTrustSet.class)
public interface TrustSet extends Transaction {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableTrustSet.Builder}.
   */
  static ImmutableTrustSet.Builder builder() {
    return ImmutableTrustSet.builder();
  }

  /**
   * Set of {@link Flags.TrustSetFlags}s for this {@link TrustSet}, which have been properly combined to yield a
   * {@link Flags} object containing the {@link Long} representation of the set bits.
   *
   * <p>The value of the flags can either be set manually, or constructed using {@link Flags.TrustSetFlags.Builder}.
   *
   * @return The {@link Flags.PaymentFlags} for this transaction.
   */
  @JsonProperty("Flags")
  @Value.Default
  default Flags.TrustSetFlags flags() {
    return Flags.TrustSetFlags.builder().build();
  }

  /**
   * The {@link IssuedCurrencyAmount} defining the trust line to create or modify.
   *
   * @return An {@link IssuedCurrencyAmount} containing the amount of the trust line.
   */
  @JsonProperty("LimitAmount")
  IssuedCurrencyAmount limitAmount();

  /**
   * Value incoming balances on this trust line at the ratio of this number per 1,000,000,000 units.
   * A value of 0 is shorthand for treating balances at face value.
   *
   * @return An {@link Optional} of type {@link UnsignedInteger} defining the inbound quality.
   */
  @JsonProperty("QualityIn")
  Optional<UnsignedInteger> qualityIn();

  /**
   * Value outgoing balances on this trust line at the ratio of this number per 1,000,000,000 units.
   * A value of 0 is shorthand for treating balances at face value.
   *
   * @return An {@link Optional} of type {@link UnsignedInteger} defining the outbound quality.
   */
  @JsonProperty("QualityOut")
  Optional<UnsignedInteger> qualityOut();

}
