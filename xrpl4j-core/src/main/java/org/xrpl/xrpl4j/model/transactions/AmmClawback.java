package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;
import org.xrpl.xrpl4j.model.ledger.Issue;

/**
 * Object mapping for the {@code AMMClawback} transaction.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAmmClawback.class)
@JsonDeserialize(as = ImmutableAmmClawback.class)
public interface AmmClawback extends Transaction {
  @JsonProperty("Account")
  Address account();

  @JsonProperty("Asset")
  Issue asset();

  @JsonProperty("Asset2")
  Issue asset2();

  @JsonProperty("Amount")
  CurrencyAmount amount();

  @JsonProperty("Holder")
  Address holder();

  @JsonProperty("Flags")
  @Value.Default
  default TransactionFlags flags() {
    return TransactionFlags.EMPTY;
  }

  static ImmutableAmmClawback.Builder builder() {
    return ImmutableAmmClawback.builder();
  }
}
