package org.xrpl.xrpl4j.model.client.transactions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.jackson.modules.TransactionResultDeserializer;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.TransactionMetadata;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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

  /**
   * XRP Ledger represents dates using a custom epoch called Ripple Epoch. This is a constant for
   * the start of that epoch.
   */
  long RIPPLE_EPOCH = 946684800;

  /**
   * Construct a builder for this class.
   *
   * @param <T> The actual type of {@link Transaction}.
   *
   * @return An {@link ImmutableTransactionResult.Builder}
   */
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
   * The ledger index of the ledger that includes this {@link Transaction}.
   *
   * @return An optionally-present {@link LedgerIndex}.
   */
  @JsonProperty("ledger_index")
  Optional<LedgerIndex> ledgerIndex();

  /**
   * Get {@link #ledgerIndex()}, or throw an {@link IllegalStateException} if {@link #ledgerIndex()} is empty.
   *
   * @return The value of {@link #ledgerIndex()}.
   * @throws IllegalStateException If {@link #ledgerIndex()} is empty.
   */
  @JsonIgnore
  @Value.Auxiliary
  default LedgerIndex ledgerIndexSafe() {
    return ledgerIndex()
      .orElseThrow(() -> new IllegalStateException("Result did not contain a ledgerIndex."));
  }

  /**
   * The identifying hash of the {@link Transaction}.
   *
   * @return The {@link Hash256} of {@link #transaction()}.
   */
  Hash256 hash();

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
  @JsonProperty("metaData")
  Optional<TransactionMetadata> metadata();

  /**
   * The approximate close time (using Ripple Epoch) of the ledger containing this transaction.
   * This is an undocumented field.
   *
   * @return An optionally-present {@link UnsignedLong}.
   */
  @JsonProperty("date")
  Optional<UnsignedLong> closeDate();

  /**
   * The approximate close time in UTC offset.
   * This is derived from undocumented field.
   *
   * @return An optionally-present {@link ZonedDateTime}.
   */
  @JsonIgnore
  @Value.Auxiliary
  default Optional<ZonedDateTime> closeDateHuman() {
    return closeDate().map(secondsSinceRippleEpoch ->
      Instant.ofEpochSecond(RIPPLE_EPOCH + secondsSinceRippleEpoch.longValue()).atZone(ZoneId.of("UTC"))
    );
  }

}
