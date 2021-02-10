package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import java.util.Optional;
import org.immutables.value.Value.Immutable;

/**
 * Transaction metadata is a section of data that gets added to a transaction after it is processed.
 * Any transaction that gets included in a ledger has metadata, regardless of whether it is successful.
 * The transaction metadata describes the outcome of the transaction in detail.
 * @see "https://xrpl.org/transaction-metadata.html"
 */
@Immutable
@JsonSerialize(as = ImmutableTransactionMetadata.class)
@JsonDeserialize(as = ImmutableTransactionMetadata.class)
public interface TransactionMetadata {

  static ImmutableTransactionMetadata.Builder builder() {
    return ImmutableTransactionMetadata.builder();
  }

  /**
   * The transaction's position within the ledger that included it. This is zero-indexed.
   * For example, the value 2 means it was the 3rd transaction in that ledger.
   *
   * @return index of transaction within ledger.
   */
  @JsonProperty("TransactionIndex")
  UnsignedInteger transactionIndex();

  /**
   * A result code indicating whether the transaction succeeded or how it failed.
   *
   * @return transaction result code.
   */
  @JsonProperty("TransactionResult")
  String transactionResult();

  /**
   * The Currency Amount actually received by the Destination account.
   * Use this field to determine how much was delivered, regardless of whether the transaction is a partial payment.
   * Omitted for non-Payment transactions.
   *
   * @return delivered amount for payments, otherwise empty for non-payments.
   */
  @JsonProperty("delivered_amount")
  Optional<CurrencyAmount> deliveredAmount();

  // TODO: map the AffectedNodes object graph if needed

}
