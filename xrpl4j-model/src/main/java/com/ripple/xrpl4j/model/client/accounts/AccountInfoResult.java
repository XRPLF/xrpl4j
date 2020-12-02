package com.ripple.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.model.client.rippled.XrplResult;
import com.ripple.xrpl4j.model.ledger.AccountRootObject;
import org.immutables.value.Value;

import java.util.Optional;

/**
 * The result of an account_info rippled API call, containing information about a given account on the XRP Ledger.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAccountInfoResult.class)
@JsonDeserialize(as = ImmutableAccountInfoResult.class)
public interface AccountInfoResult extends XrplResult {

  static ImmutableAccountInfoResult.Builder builder() {
    return ImmutableAccountInfoResult.builder();
  }

  /**
   * The {@link AccountRootObject} ledger object with this account's information, as stored in the ledger.
   */
  @JsonProperty("account_data")
  AccountRootObject accountData();

  /**
   * (Omitted if ledger_index is provided instead) The ledger index of the current in-progress ledger,
   * which was used when retrieving this information.
   */
  @JsonProperty("ledger_current_index")
  Optional<UnsignedInteger> ledgerCurrentIndex();

  /**
   * (Omitted if ledger_current_index is provided instead) The ledger index of the ledger version used when
   * retrieving this information. The information does not contain any changes from ledger versions newer than this one.
   */
  @JsonProperty("ledger_index")
  Optional<UnsignedInteger> ledgerIndex();

  /**
   * (Omitted unless queue specified as true and querying the current open ledger.)
   * Information about queued transactions sent by this account. This information describes the state of
   * the local rippled server, which may be different from other servers in the peer-to-peer XRP Ledger network.
   * Some fields may be omitted because the values are calculated "lazily" by the queuing mechanism.
   */
  @JsonProperty("queue_data")
  Optional<QueueData> queueData();

  /**
   * True if this data is from a validated ledger version; if false, this data is not final.
   */
  @Value.Default
  default boolean validated() {
    return false;
  }

  /**
   * The value "success" indicates the request was successfully received and understood by the server.
   */
  String status();

}
