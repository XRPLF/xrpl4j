package org.xrpl.xrpl4j.model.client.transactions;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.XrplRequestParams;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.util.Optional;

/**
 * Request parameters for the tx rippled API method.
 *
 * <p>The tx method may successfully find the {@link TransactionRequestParams#transaction()} even if it is included in
 * a ledger outside the range of {@link TransactionRequestParams#minLedger()} to
 * {@link TransactionRequestParams#maxLedger()}.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableTransactionRequestParams.class)
@JsonDeserialize(as = ImmutableTransactionRequestParams.class)
public interface TransactionRequestParams extends XrplRequestParams {

  static ImmutableTransactionRequestParams.Builder builder() {
    return ImmutableTransactionRequestParams.builder();
  }

  /**
   * Convenience constructor for creating {@link TransactionRequestParams} with only a transaction hash.
   *
   * @param transactionHash A {@link String} containing the transaction hash of the transaction to look up.
   * @return {@link TransactionRequestParams} with {@link TransactionRequestParams#transaction()} set to
   * {@code transactionHash}
   */
  static TransactionRequestParams of(Hash256 transactionHash) {
    return builder().transaction(transactionHash).build();
  }

  /**
   * The 256-bit hash of the transaction in hexadecimal form.
   */
  Hash256 transaction();

  /**
   * If true, return transaction data and metadata as binary serialized to hexadecimal strings. If false,
   * return transaction data and metadata as JSON. The default is false.
   */
  @Value.Default
  default boolean binary() {
    return false;
  }

  /**
   * Use this with {@link TransactionRequestParams#maxLedger()} to specify a range of up to 1000 ledger indexes,
   * starting with this ledger (inclusive).
   *
   * <p>If the server cannot find the transaction, it confirms whether it was able to search all the
   * ledgers in this range.
   */
  @JsonProperty("min_ledger")
  Optional<UnsignedLong> minLedger();

  /**
   * Use this with {@link TransactionRequestParams#minLedger()} to specify a range of up to 1000 ledger indexes,
   * ending with this ledger (inclusive).
   *
   * <p>If the server cannot find the transaction, it confirms whether it was able to search all the ledgers in the
   * requested range.
   */
  @JsonProperty("max_ledger")
  Optional<UnsignedLong> maxLedger();

}
