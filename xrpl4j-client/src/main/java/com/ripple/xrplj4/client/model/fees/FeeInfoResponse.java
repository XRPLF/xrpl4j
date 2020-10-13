package com.ripple.xrplj4.client.model.fees;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value.Immutable;

@Immutable
@JsonSerialize(as = ImmutableFeeInfoResponse.class)
@JsonDeserialize(as = ImmutableFeeInfoResponse.class)
public interface FeeInfoResponse {

  static ImmutableFeeInfoResponse.Builder builder() {
    return ImmutableFeeInfoResponse.builder();
  }

  FeeDrops drops();

  @JsonProperty("ledger_current_index")
  UnsignedInteger currentLedgerIndex();

}
