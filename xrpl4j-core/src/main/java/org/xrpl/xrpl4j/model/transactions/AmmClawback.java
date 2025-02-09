package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.ledger.Issue;

/**
 * Object mapping for the {@code AMMClawback} transaction.
 */
@Value.Immutable
// @JsonSerialize(as = ImmutableDidSet.class)
// @JsonDeserialize(as = ImmutableDidSet.class)
public interface AmmClawback extends Transaction {
  @JsonProperty("Account")
  Address account();

  @JsonProperty("Asset")
  Issue asset();

  @JsonProperty("Asset2")
  Issue asset2();

  @JsonProperty("Amount")
  CurrencyAmount amount();
}
