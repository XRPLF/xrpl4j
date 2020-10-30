package com.ripple.xrplj4.client.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.model.transactions.Flags;
import com.ripple.xrpl4j.model.transactions.Transaction;
import com.ripple.xrplj4.client.model.JsonRpcResult;
import com.ripple.xrplj4.client.rippled.JsonRpcResponse;
import org.immutables.value.Value.Immutable;

@Immutable
@JsonSerialize(as = ImmutableSubmissionResult.class)
@JsonDeserialize(as = ImmutableSubmissionResult.class)
public interface SubmissionResult<T extends Transaction<? extends Flags.TransactionFlags>> extends JsonRpcResult {

  static <T extends Transaction<? extends Flags.TransactionFlags>> ImmutableSubmissionResult.Builder<T> builder() {
    return ImmutableSubmissionResult.builder();
  }

  @JsonProperty("engine_result")
  String engineResult();

  @JsonProperty("engine_result_message")
  String engineResultMessage();

  @JsonProperty("tx_blob")
  String transactionBlob();

  @JsonProperty("tx_json")
  T transaction();

  boolean accepted();

  @JsonProperty("account_sequence_available")
  UnsignedInteger accountSequenceAvailable();

  @JsonProperty("account_sequence_next")
  UnsignedInteger accountSequenceNext();

  boolean applied();

  boolean broadcast();

  boolean kept();

  boolean queued();

  @JsonProperty("open_ledger_cost")
  String openLedgerCost();

  @JsonProperty("validated_ledger_index")
  UnsignedInteger validatedLedgerIndex();


}
