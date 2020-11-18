package com.ripple.xrpl4j.client.model.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ripple.xrpl4j.client.rippled.JsonRpcRequestParams;
import com.ripple.xrpl4j.model.jackson.modules.LedgerIndexSerializer;
import com.ripple.xrpl4j.model.transactions.Address;
import org.bouncycastle.asn1.eac.UnsignedInteger;
import org.immutables.value.Value;

import java.util.Optional;

/**
 * Request parameters for the account_channels rippled method.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAccountChannelsRequestParams.class)
@JsonDeserialize(as = ImmutableAccountChannelsRequestParams.class)
public interface AccountChannelsRequestParams extends JsonRpcRequestParams {

  static ImmutableAccountChannelsRequestParams.Builder builder() {
    return ImmutableAccountChannelsRequestParams.builder();
  }

  /**
   * The unique {@link Address} for the account. The request returns channels where this account is the channel's
   * owner/source.
   */
  Address account();

  /**
   * The unique {@link Address} for the destination account. If provided, the response results are filtered
   * by channels whose destination is this account.
   */
  @JsonProperty("destination_account")
  Optional<Address> destinationAccount();

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
   * Limit the number of transactions to retrieve. Cannot be less than 10 or more than 400. The default is 200.
   */
  Optional<UnsignedInteger> limit();

  /**
   * Value from a previous paginated response. Resume retrieving data where that response left off.
   */
  Optional<String> marker();
}
