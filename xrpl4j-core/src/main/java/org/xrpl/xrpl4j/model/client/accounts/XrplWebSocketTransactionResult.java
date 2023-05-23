package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.Transaction;

@Value.Immutable
@JsonSerialize(as = ImmutableXrplWebSocketTransactionResult.class)
@JsonDeserialize(as = ImmutableXrplWebSocketTransactionResult.class)
public interface XrplWebSocketTransactionResult {

  /**
   * Construct a {@code XrplWebSocketTransactionResult} builder.
   *
   * @return An {@link ImmutableXrplWebSocketTransactionResult.Builder}.
   */
  static ImmutableXrplWebSocketTransactionResult.Builder builder() {
    return ImmutableXrplWebSocketTransactionResult.builder();
  }

  @JsonProperty("transaction")
  XrplWebSocketTransaction transaction();

  @JsonProperty("engine_result")
  String status();

  @JsonProperty("ledger_index")
  UnsignedInteger ledgerIndex();
}