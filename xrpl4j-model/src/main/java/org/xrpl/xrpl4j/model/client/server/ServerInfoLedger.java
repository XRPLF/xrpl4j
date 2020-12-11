package org.xrpl.xrpl4j.model.client.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

@Value.Immutable
@JsonSerialize(as = ImmutableServerInfoLedger.class)
@JsonDeserialize(as = ImmutableServerInfoLedger.class)
public interface ServerInfoLedger {

  static ImmutableServerInfoLedger.Builder builder() {
    return ImmutableServerInfoLedger.builder();
  }

  /**
   * The time since the ledger was closed, in seconds.
   */
  UnsignedInteger age();

  /**
   * Unique hash for the ledger, as hexadecimal.
   */
  Hash256 hash();

  /**
   * Minimum amount of XRP (not drops) necessary for every account to keep in reserve
   */
  @JsonProperty("reserve_base_xrp")
  UnsignedInteger reserveBaseXrp();

  /**
   * Amount of XRP (not drops) added to the account reserve for each object an account owns in the ledger.
   */
  @JsonProperty("reserve_inc_xrp")
  UnsignedInteger reserveIncXrp();

  /**
   * The ledger index of the latest validate ledger.
   */
  @JsonProperty("seq")
  LedgerIndex sequence();

}
