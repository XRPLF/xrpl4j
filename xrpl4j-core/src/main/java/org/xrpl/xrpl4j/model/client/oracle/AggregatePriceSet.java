package org.xrpl.xrpl4j.model.client.oracle;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Lazy;

import java.math.BigDecimal;

/**
 * Statistics from collected oracle prices.
 */
@Beta
@Immutable
@JsonSerialize(as = ImmutableAggregatePriceSet.class)
@JsonDeserialize(as = ImmutableAggregatePriceSet.class)
public interface AggregatePriceSet {

  /**
   * Construct a {@code AggregatePriceSet} builder.
   *
   * @return An {@link ImmutableAggregatePriceSet.Builder}.
   */
  static ImmutableAggregatePriceSet.Builder builder() {
    return ImmutableAggregatePriceSet.builder();
  }

  /**
   * The simple mean price.
   *
   * @return A {@link String}.
   */
  @JsonProperty("mean")
  String meanString();

  /**
   * The simple mean price as a {@link BigDecimal}.
   *
   * @return A {@link BigDecimal}.
   */
  @Lazy
  @JsonIgnore
  default BigDecimal mean() {
    return new BigDecimal(meanString());
  }

  /**
   * The size of the data set to calculate the mean.
   *
   * @return An {@link UnsignedLong}.
   */
  UnsignedLong size();

  /**
   * The standard deviation.
   *
   * @return A {@link String}.
   */
  @JsonProperty("standard_deviation")
  String standardDeviationString();

  /**
   * The standard deviation as a {@link BigDecimal}.
   *
   * @return A {@link BigDecimal}.
   */
  @Lazy
  @JsonIgnore
  default BigDecimal standardDeviation() {
    return new BigDecimal(standardDeviationString());
  }
}
