package com.ripple.xrpl4j.xrplj4.client.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.model.transactions.Flags;
import com.ripple.xrpl4j.model.transactions.Transaction;
import com.ripple.xrpl4j.xrplj4.client.model.JsonRpcResult;
import org.immutables.value.Value;

/**
 * The result of a tx rippled API method call.
 * @param <TxnType> The type of {@link Transaction} that was requested.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableTransactionResult.class)
@JsonDeserialize(as = ImmutableTransactionResult.class)
public interface TransactionResult<TxnType extends Transaction<? extends Flags>> extends JsonRpcResult {

  static <T extends Transaction<? extends Flags>> ImmutableTransactionResult.Builder<T> builder() {
    return ImmutableTransactionResult.builder();
  }

  /**
   * The {@link Transaction} that was returned as a result of the tx call.
   *
   * In JSON form, the {@link Transaction} fields are included as root object fields, and thus this field
   * needs the {@link JsonUnwrapped} annotation.
   */
  @JsonUnwrapped
  TxnType transaction();

  /**
   * The SHA-512 hash of the transaction in hexadecimal form.
   */
  String hash();

  /**
   * The ledger index of the ledger that includes this {@link Transaction}.
   */
  @JsonProperty("ledger_index")
  UnsignedInteger ledgerIndex();

  /**
   * True if this data is from a validated ledger version; if omitted or set to false, this data is not final.
   */
  @Value.Default
  default boolean validated() {
    return false;
  }

  // TODO: Add tx metadata if people need it
}
