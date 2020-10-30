package com.ripple.xrplj4.client.model.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import java.util.Optional;

import com.ripple.xrplj4.client.model.JsonRpcResult;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableAccountInfoResult.class)
@JsonDeserialize(as = ImmutableAccountInfoResult.class)
public interface AccountInfoResult extends JsonRpcResult {

  static ImmutableAccountInfoResult.Builder builder() {
    return ImmutableAccountInfoResult.builder();
  }

  @JsonProperty("account_data")
  AccountRoot accountData();

  @JsonProperty("signer_lists")
  Optional<SignerList> signerLists();

  @JsonProperty("ledger_current_index")
  Optional<UnsignedInteger> ledgerCurrentIndex();

  @JsonProperty("ledger_index")
  Optional<UnsignedInteger> ledgerIndex();

  @JsonProperty("queue_data")
  Optional<QueueData> queueData();

  boolean validated();

  String status();

}
