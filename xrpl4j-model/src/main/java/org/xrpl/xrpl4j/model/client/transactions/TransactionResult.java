package org.xrpl.xrpl4j.model.client.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Optional;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.jackson.modules.TransactionResultDeserializer;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.TransactionMetadata;

/**
 * The result of a tx rippled API method call.
 *
 * @param <TxnType> The type of {@link Transaction} that was requested.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableTransactionResult.class)
@JsonDeserialize(using = TransactionResultDeserializer.class)
public interface TransactionResult<TxnType extends Transaction> extends XrplResult {

  static <T extends Transaction> ImmutableTransactionResult.Builder<T> builder() {
    return ImmutableTransactionResult.builder();
  }

  /**
   * The {@link Transaction} that was returned as a result of the "tx" call.
   *
   * @return A {@link Transaction} of type {@link TxnType}.
   */
  @JsonUnwrapped
  TxnType transaction();

  /**
   * The SHA-512Half hash of the transaction in hexadecimal form.
   *
   * @return A {@link Hash256} containing the transaction hash.
   */
  Hash256 hash();

  /**
   * The ledger index of the ledger that includes this {@link Transaction}.
   *
   * @return An optionally-present {@link LedgerIndex}.
   */
  @JsonProperty("ledger_index")
  Optional<LedgerIndex> ledgerIndex();

  /**
   * {@code true} if this data is from a validated ledger version; If {@code false}, this data is not final.
   *
   * @return {@code true} if this data is from a validated ledger version; If {@code false}, this data is not final.
   */
  @Value.Default
  default boolean validated() {
    return false;
  }

  /**
   * Metadata about the transaction if this data is from a validated ledger version.
   *
   * @return metadata or empty for non-validated transactions.
   */
  @JsonProperty("meta")
  Optional<TransactionMetadata> metadata();

}
