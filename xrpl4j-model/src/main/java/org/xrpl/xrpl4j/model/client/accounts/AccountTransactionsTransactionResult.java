package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.TransactionMetadata;

import java.util.Optional;

/**
 * The transaction that gets returned as part of a response to the account_tx rippled method.
 *
 * @param <T> The type of {@link Transaction}.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAccountTransactionsTransactionResult.class)
@JsonDeserialize(as = ImmutableAccountTransactionsTransactionResult.class)
public interface AccountTransactionsTransactionResult<T extends Transaction> extends XrplResult {

  /**
   * Construct a builder for this class.
   *
   * @param <T> The type of {@link Transaction} contained in this result.
   *
   * @return An {@link ImmutableAccountTransactionsTransactionResult.Builder}.
   */
  static <T extends Transaction> ImmutableAccountTransactionsTransactionResult.Builder<T> builder() {
    return ImmutableAccountTransactionsTransactionResult.builder();
  }

  /**
   * The {@link Transaction}.
   *
   * @return A {@link T} with the transaction fields.
   * @deprecated This field will be removed in a future release. The {@link Transaction} can be found in {@link
   *   #resultTransaction()}, and the transaction's {@code hash} and {@code ledgerIndex} can be found in
   *   {@code resultTransaction().hash()} and {@code resultTransaction().ledgerIndex()}, respectively.
   */
  @Deprecated
  @Value.Derived
  @JsonIgnore
  default T transaction() {
    return resultTransaction().transaction();
  }

  @JsonProperty("tx")
  AccountTransactionsTransaction<T> resultTransaction();

  /**
   * Metadata about the transaction if this data is from a validated ledger version.
   *
   * @return {@link TransactionMetadata} or empty for non-validated transactions.
   */
  @JsonProperty("meta")
  Optional<TransactionMetadata> metadata();

  /**
   * Whether or not this transaction came from a validated ledger.
   *
   * @return {@code true} if from a validated ledger, otherwise {@code false}.
   */
  @Value.Default
  default boolean validated() {
    return false;
  }
}
