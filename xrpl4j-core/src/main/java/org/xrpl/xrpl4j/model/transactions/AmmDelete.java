package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;
import org.xrpl.xrpl4j.model.ledger.Issue;

/**
 * Object mapping for the AMMDelete transaction.
 */
@Immutable
@JsonSerialize(as = ImmutableAmmDelete.class)
@JsonDeserialize(as = ImmutableAmmDelete.class)
public interface AmmDelete extends Transaction {

  /**
   * Construct a {@code AmmDelete} builder.
   *
   * @return An {@link ImmutableAmmDelete.Builder}.
   */
  static ImmutableAmmDelete.Builder builder() {
    return ImmutableAmmDelete.builder();
  }

  /**
   * Set of {@link TransactionFlags}s for this {@link AmmDelete}, which only allows the
   * {@code tfFullyCanonicalSig} flag, which is deprecated.
   *
   * @return Always {@link TransactionFlags#EMPTY}.
   */
  @JsonProperty("Flags")
  @Value.Default
  default TransactionFlags flags() {
    return TransactionFlags.EMPTY;
  }

  /**
   * The definition for one of the assets in the AMM's pool.
   *
   * @return An {@link Issue}.
   */
  @JsonProperty("Asset")
  Issue asset();

  /**
   * The definition for the other asset in the AMM's pool.
   *
   * @return An {@link Issue}.
   */
  @JsonProperty("Asset2")
  Issue asset2();

}
