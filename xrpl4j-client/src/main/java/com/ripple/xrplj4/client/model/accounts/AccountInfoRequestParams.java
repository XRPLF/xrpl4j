package com.ripple.xrplj4.client.model.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ripple.xrpl4j.model.jackson.modules.LedgerIndexSerializer;
import com.ripple.xrpl4j.model.transactions.Address;
import com.ripple.xrplj4.client.rippled.JsonRpcRequestParam;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableAccountInfoRequestParams.class)
@JsonDeserialize(as = ImmutableAccountInfoRequestParams.class)
public interface AccountInfoRequestParams extends JsonRpcRequestParam {

  static ImmutableAccountInfoRequestParams.Builder builder() {
    return ImmutableAccountInfoRequestParams.builder();
  }

  static AccountInfoRequestParams of(Address account) {
    return builder()
        .account(account)
        .build();
  }

  Address account();

  @JsonProperty("ledger_hash")
  Optional<String> ledgerHash();

  @JsonSerialize(using = LedgerIndexSerializer.class)
  @JsonProperty("ledger_index")
  @Value.Default
  default String ledgerIndex() {
    return "current";
  };

  @Value.Default
  default boolean strict() {
    return true;
  }

  @Value.Default
  default boolean queue() {
    return false;
  }

  @Value.Default
  @JsonProperty("signer_lists")
  default boolean signerLists() {
    return false;
  }

}
