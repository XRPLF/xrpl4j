package org.xrpl.xrpl4j.model.client.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.jackson.modules.TransactionResultDeserializer;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.util.Optional;

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
   * The {@link Transaction} that was returned as a result of the tx call.
   */
  @JsonUnwrapped
  TxnType transaction();

  /**
   * The SHA-512Half hash of the transaction in hexadecimal form.
   */
  Hash256 hash();

  /**
   * The ledger index of the ledger that includes this {@link Transaction}.
   */
  @JsonProperty("ledger_index")
  Optional<LedgerIndex> ledgerIndex();

  /**
   * True if this data is from a validated ledger version; if omitted or set to false, this data is not final.
   */
  @Value.Default
  default boolean validated() {
    return false;
  }

  // TODO: Add tx metadata if people need it
}
