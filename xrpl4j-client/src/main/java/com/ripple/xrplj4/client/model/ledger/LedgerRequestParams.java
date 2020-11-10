package com.ripple.xrplj4.client.model.ledger;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ripple.xrpl4j.model.jackson.modules.LedgerIndexSerializer;
import com.ripple.xrplj4.client.rippled.JsonRpcRequestParams;
import org.immutables.value.Value;

import java.util.Optional;

/**
 * Request parameters for the ledger JSON RPC API method.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableLedgerRequestParams.class)
@JsonDeserialize(as = ImmutableLedgerRequestParams.class)
public interface LedgerRequestParams extends JsonRpcRequestParams {

  static ImmutableLedgerRequestParams.Builder builder() {
    return ImmutableLedgerRequestParams.builder();
  }

  /**
   * A 20-byte hex string for the ledger version to use.
   */
  @JsonProperty("ledger_hash")
  Optional<String> ledgerHash();

  /**
   * The ledger index of the ledger to use, or a shortcut string to choose a ledger automatically.
   */
  @JsonProperty("ledger_index")
  @JsonSerialize(using = LedgerIndexSerializer.class)
  @Value.Default
  default String ledgerIndex() {
    return "current";
  }

  /**
   * If true, return full information on the entire ledger. Ignored if you did not specify {@link #ledgerHash()}.
   * Defaults to false. (Equivalent to enabling transactions, accounts, and expand.)
   *
   * Caution: This is a very large amount of data -- on the order of several hundred megabytes!
   *
   * Note: You must be a rippled Admin to set to true.
   */
  @Value.Default
  default boolean full() {
    return false;
  }

  /**
   * If true, return information on accounts in the ledger. Ignored if you did not specify {@link #ledgerHash()}.
   * Defaults to false.
   *
   * Caution: This returns a very large amount of data!
   *
   * Note: You must be a rippled Admin to set to true.
   */
  @Value.Default
  default boolean accounts() {
    return false;
  }

  /**
   * If true, return information on transactions in the specified ledger version. Defaults to false.
   * Ignored if you did not specify {@link #ledgerHash()}.
   */
  @Value.Default
  default boolean transactions() {
    return false;
  }

  /**
   * Provide full JSON-formatted information for transaction/account information instead of only hashes.
   * Defaults to false. Ignored unless you request {@link #transactions()}, {@link #accounts()}, or both.
   */
  @Value.Default
  default boolean expand() {
    return false;
  }

  /**
   * If true, include the owner_funds field in the metadata of {@link OfferCreate} transactions in the response.
   * Defaults to false. Ignored unless {@link #transactions()} and {@link #expand()} are true.
   */
  @JsonProperty("owner_funds")
  @Value.Default
  default boolean ownerFunds() {
    return false;
  }

  /**
   * If true, and {@link #transactions()} and {@link #expand()} are both also true, return transaction information
   * in binary format (hexadecimal string) instead of JSON format.
   */
  @Value.Default
  default boolean binary() {
    return false;
  }

  /**
   * If true, and the command is requesting the current ledger, includes an array of queued transactions in the results.
   */
  @Value.Default
  default boolean queue() {
    return false;
  }

}
