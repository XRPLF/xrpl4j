package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.immutables.value.Value.Auxiliary;
import org.immutables.value.Value.Derived;
import org.immutables.value.Value.Immutable;

/**
 * {@link CurrencyAmount} type for MPT amounts.
 */
@Immutable
@JsonSerialize(as = ImmutableMpTokenAmount.class)
@JsonDeserialize(as = ImmutableMpTokenAmount.class)
public interface MpTokenAmount extends CurrencyAmount {

  /**
   * Construct a {@code MpTokenAmount} builder.
   *
   * @return An {@link ImmutableMpTokenAmount.Builder}.
   */
  static ImmutableMpTokenAmount.Builder builder() {
    return ImmutableMpTokenAmount.builder();
  }

  /**
   * Construct a {@code MpTokenAmount} builder, setting {@link #value()} to the string representation of the
   * supplied {@link UnsignedLong}.
   *
   * @return An {@link ImmutableMpTokenAmount.Builder}.
   */
  static ImmutableMpTokenAmount.Builder builder(UnsignedLong value) {
    return ImmutableMpTokenAmount.builder()
      .value(value.toString());
  }

  @JsonProperty("mpt_issuance_id")
  MpTokenIssuanceId mptIssuanceId();

  String value();

  /**
   * The amount value, as an {@link UnsignedLong}.
   *
   * @return An {@link UnsignedLong}.
   */
  @Value.Auxiliary
  @JsonIgnore
  default UnsignedLong unsignedLongValue() {
    return isNegative() ?
      UnsignedLong.valueOf(value().substring(1)) :
      UnsignedLong.valueOf(value());
  }

  /**
   * Indicates whether this amount is positive or negative.
   *
   * @return {@code true} if this amount is negative; {@code false} otherwise (i.e., if the value is 0 or positive).
   */
  @Derived
  @JsonIgnore // <-- This is not actually part of the binary serialization format, so exclude from JSON
  @Auxiliary
  default boolean isNegative() {
    return value().startsWith("-");
  }

}
