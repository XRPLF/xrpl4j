package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;
import org.xrpl.xrpl4j.model.ledger.Asset;

/**
 * Object mapping for the AMMVote transaction.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAmmVote.class)
@JsonDeserialize(as = ImmutableAmmVote.class)
public interface AmmVote extends Transaction {

  /**
   * Construct a {@code AmmVote} builder.
   *
   * @return An {@link ImmutableAmmVote.Builder}.
   */
  static ImmutableAmmVote.Builder builder() {
    return ImmutableAmmVote.builder();
  }

  /**
   * Set of {@link TransactionFlags}s for this {@link AmmVote}.
   *
   * <p>The value of the flags cannot be set manually, but exists for JSON serialization/deserialization only and for
   * proper signature computation in rippled.
   *
   * @return Always {@link TransactionFlags} with {@code tfFullyCanonicalSig} set.
   */
  @JsonProperty("Flags")
  @Value.Derived
  default TransactionFlags flags() {
    return new TransactionFlags.Builder().build();
  }

  /**
   * The definition for one of the assets in the AMM's pool.
   *
   * @return An {@link Asset}.
   */
  @JsonProperty("Asset")
  Asset asset();

  /**
   * The definition for the other asset in the AMM's pool.
   *
   * @return An {@link Asset}.
   */
  @JsonProperty("Asset2")
  Asset asset2();

  /**
   * The proposed fee to vote for.
   *
   * @return A {@link TradingFee}.
   */
  @JsonProperty("TradingFee")
  TradingFee tradingFee();


}
