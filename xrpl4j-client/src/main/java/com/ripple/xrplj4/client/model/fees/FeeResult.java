package com.ripple.xrplj4.client.model.fees;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.model.jackson.modules.LedgerIndexSerializer;
import com.ripple.xrplj4.client.model.JsonRpcResult;
import org.immutables.value.Value.Immutable;

@Immutable
@JsonSerialize(as = ImmutableFeeResult.class)
@JsonDeserialize(as = ImmutableFeeResult.class)
public interface FeeResult extends JsonRpcResult {

  static ImmutableFeeResult.Builder builder() {
    return ImmutableFeeResult.builder();
  }

  @JsonProperty("current_ledger_size")
  String currentLedgerSize();

  @JsonProperty("current_queue_size")
  String currentQueueSize();

  FeeDrops drops();

  @JsonProperty("expected_ledger_size")
  String expectedLedgerSize();

  @JsonSerialize(using = LedgerIndexSerializer.class)
  @JsonProperty("ledger_current_index")
  UnsignedInteger ledgerCurrentIndex();

  FeeLevels levels();

  @JsonProperty("max_queue_size")
  String maxQueueSize();

}
