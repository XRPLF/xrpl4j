package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.jackson.modules.AccountTransactionsTransactionDeserializer;
import org.xrpl.xrpl4j.model.jackson.modules.XrplWebSocketTransactionDeserializer;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Transaction;

@Value.Immutable
@JsonSerialize(as = ImmutableXrplWebSocketTransaction.class)
@JsonDeserialize(
    as = ImmutableXrplWebSocketTransaction.class,
    using = XrplWebSocketTransactionDeserializer.class
)
public interface XrplWebSocketTransaction {

  /**
   * Construct a {@code XrplFinalizedTransaction} builder.
   *
   * @return An {@link ImmutableXrplFinalizedTransaction.Builder}.
   */
  static ImmutableXrplWebSocketTransaction.Builder builder() {
    return ImmutableXrplWebSocketTransaction.builder();
  }

  @JsonUnwrapped
  Transaction transaction();

  /**
   * The transaction hash of this transaction.
   *
   * @return A {@link Hash256} containing the transaction hash.
   */
  Hash256 hash();
}
