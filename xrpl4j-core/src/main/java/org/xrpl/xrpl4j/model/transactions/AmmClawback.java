package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.AmmClawbackFlags;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;
import org.xrpl.xrpl4j.model.ledger.Issue;

import java.util.Optional;

/**
 * Object mapping for the {@code AMMClawback} transaction.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAmmClawback.class)
@JsonDeserialize(as = ImmutableAmmClawback.class)
@Beta
public interface AmmClawback extends Transaction {

  @JsonProperty("Holder")
  Address holder();

  @JsonProperty("Asset")
  Issue asset();

  @JsonProperty("Asset2")
  Issue asset2();

  @JsonProperty("Amount")
  Optional<CurrencyAmount> amount();

  @JsonProperty("Flags")
  @Value.Default
  default AmmClawbackFlags flags() {
    return AmmClawbackFlags.UNSET;
  }

  static ImmutableAmmClawback.Builder builder() {
    return ImmutableAmmClawback.builder();
  }
}
