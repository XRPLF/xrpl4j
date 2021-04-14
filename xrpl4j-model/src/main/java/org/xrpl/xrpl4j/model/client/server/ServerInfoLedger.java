package org.xrpl.xrpl4j.model.client.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.math.BigDecimal;

/**
 * Information about a recent ledger, as represented in {@link ServerInfoResult}s.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableServerInfoLedger.class)
@JsonDeserialize(as = ImmutableServerInfoLedger.class)
public interface ServerInfoLedger {

  static ImmutableServerInfoLedger.Builder builder() {
    return ImmutableServerInfoLedger.builder();
  }

  /**
   * The time since the ledger was closed, in seconds.
   *
   * @return An {@link UnsignedInteger} representing the age, in seconds.
   */
  UnsignedInteger age();

  /**
   * Unique hash for the ledger, as hexadecimal.
   *
   * @return A {@link Hash256} containing the ledger hash.
   */
  Hash256 hash();

  /**
   * Minimum amount of XRP (not drops) necessary for every account to keep in reserve.
   *
   * @return An {@link UnsignedInteger} representing the amount of XRP to reserve.
   */
  @JsonProperty("reserve_base_xrp")
  UnsignedInteger reserveBaseXrp();

  /**
   * Amount of XRP (not drops) added to the account reserve for each object an account owns in the ledger.
   *
   * @return An {@link UnsignedInteger} representing the amount of XRP added.
   */
  @JsonProperty("reserve_inc_xrp")
  UnsignedInteger reserveIncXrp();

  /**
   * The ledger index of the ledger.
   *
   * @return A {@link LedgerIndex} indicating the sequence of the latest ledger.
   */
  @JsonProperty("seq")
  LedgerIndex sequence();

  /**
   * The base XRP cost of transaction.
   *
   * @return A {@link BigDecimal} representing base fee amount in XRP.
   */
  @JsonProperty("base_fee_xrp")
  BigDecimal baseFeeXrp();

}
