package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

@Immutable
@JsonSerialize(as = ImmutableUnknownTransaction.class)
@JsonDeserialize(as = ImmutableUnknownTransaction.class)
public interface UnknownTransaction extends Transaction {

  /**
   * Construct a {@code UnknownTransaction} builder.
   *
   * @return An {@link ImmutableUnknownTransaction.Builder}.
   */
  static ImmutableUnknownTransaction.Builder builder() {
    return ImmutableUnknownTransaction.builder();
  }

  /**
   * This has to be a {@link String} because {@link Transaction#transactionType()} is a {@link TransactionType},
   * which only has an UNKNOWN variant. Because this method is also annotated with {@link JsonProperty} of
   * "TransactionType", this essentially overrides the "TransactionType" field in JSON, but {@link #transactionType()}
   * will always be {@link TransactionType#UNKNOWN} and this field will contain the actual "TransactionType" field.
   *
   * @return A {@link String} containing the transaction type from JSON.
   */
  @JsonProperty("TransactionType")
  String unknownTransactionType();

  @Override
  @JsonIgnore
  default TransactionType transactionType() {
    return Transaction.super.transactionType();
  }

  @JsonProperty("Flags")
  @Value.Default
  default TransactionFlags flags() {
    return TransactionFlags.EMPTY;
  }

}
