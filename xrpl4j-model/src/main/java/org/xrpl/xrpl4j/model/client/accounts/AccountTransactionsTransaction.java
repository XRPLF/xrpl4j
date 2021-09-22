package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.jackson.modules.AccountTransactionsTransactionDeserializer;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Transaction;

/**
 * Represents a transaction that is returned as part of the result of an {@code account_tx} rippled method call.
 *
 * @param <T> The type of {@link Transaction} contained in this class.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAccountTransactionsTransaction.class)
@JsonDeserialize(
  as = ImmutableAccountTransactionsTransaction.class,
  using = AccountTransactionsTransactionDeserializer.class
)
public interface AccountTransactionsTransaction<T extends Transaction> {

  /**
   * Construct a builder for this class.
   *
   * @param <T> The type of {@link Transaction} to include in the builder.
   *
   * @return A new {@link ImmutableAccountTransactionsTransaction.Builder}.
   */
  static <T extends Transaction> ImmutableAccountTransactionsTransaction.Builder<T> builder() {
    return ImmutableAccountTransactionsTransaction.builder();
  }

  /**
   * The {@link Transaction}.
   *
   * @return A {@link T} with the transaction fields.
   */
  @JsonUnwrapped
  T transaction();

  /**
   * The transaction hash of this transaction.
   *
   * @return A {@link Hash256} containing the transaction hash.
   */
  Hash256 hash();

  /**
   * The index of the ledger that this transaction was included in.
   *
   * @return The {@link LedgerIndex} that this transaction was included in.
   */
  @JsonProperty("ledger_index")
  LedgerIndex ledgerIndex();

}
