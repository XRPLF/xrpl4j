package com.ripple.xrplj4.client.model.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableAccountInfoResponse.class)
@JsonDeserialize(as = ImmutableAccountInfoResponse.class)
public interface AccountInfoResponse {

  @JsonProperty("account_data")
  AccountInfoData accountData();

  @JsonProperty("ledger_current_index")
  UnsignedInteger ledgerCurrentIndex();

  String status();

  boolean validated();

}
