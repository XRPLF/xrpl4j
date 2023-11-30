package org.xrpl.xrpl4j.model.client.ledger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.ledger.LedgerObject;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.util.Optional;

/**
 * The result of a {@code ledger_entry} RPC call.
 *
 * @param <T> The type of {@link LedgerObject} contained in the result.
 */
@Immutable
@JsonSerialize(as = ImmutableLedgerEntryResult.class)
@JsonDeserialize(as = ImmutableLedgerEntryResult.class)
public interface LedgerEntryResult<T extends LedgerObject> extends XrplResult {

  /**
   * Construct a {@code LedgerEntryResult} builder.
   *
   * @return An {@link ImmutableLedgerEntryResult.Builder}.
   */
  static <T extends LedgerObject> ImmutableLedgerEntryResult.Builder<T> builder() {
    return ImmutableLedgerEntryResult.builder();
  }

  /**
   * The ledger entry returned, as a {@link T}.
   *
   * @return A {@link T}.
   */
  T node();

  /**
   * Unique identifying hash of the entire ledger.
   *
   * @return A {@link Hash256} containing the ledger hash.
   */
  @JsonProperty("ledger_hash")
  Optional<Hash256> ledgerHash();

  /**
   * Get {@link #ledgerHash()}, or throw an {@link IllegalStateException} if {@link #ledgerHash()} is empty.
   *
   * @return The value of {@link #ledgerHash()}.
   *
   * @throws IllegalStateException If {@link #ledgerHash()} is empty.
   */
  @JsonIgnore
  @Value.Auxiliary
  default Hash256 ledgerHashSafe() {
    return ledgerHash()
      .orElseThrow(() -> new IllegalStateException("Result did not contain a ledgerHash."));
  }

  /**
   * The {@link LedgerIndex} of this ledger.
   *
   * @return The {@link LedgerIndex} of this ledger.
   */
  @JsonProperty("ledger_index")
  Optional<LedgerIndex> ledgerIndex();

  /**
   * Get {@link #ledgerIndex()}, or throw an {@link IllegalStateException} if {@link #ledgerIndex()} is empty.
   *
   * @return The value of {@link #ledgerIndex()}.
   *
   * @throws IllegalStateException If {@link #ledgerIndex()} is empty.
   */
  @JsonIgnore
  @Value.Auxiliary
  default LedgerIndex ledgerIndexSafe() {
    return ledgerIndex()
      .orElseThrow(() -> new IllegalStateException("Result did not contain a ledgerIndex."));
  }

  /**
   * The {@link LedgerIndex} of this ledger, if the ledger is the current ledger. Only present on a current ledger
   * response.
   *
   * @return A {@link LedgerIndex} if this result is for the current ledger, otherwise {@link Optional#empty()}.
   */
  @JsonProperty("ledger_current_index")
  Optional<LedgerIndex> ledgerCurrentIndex();

  /**
   * Get {@link #ledgerCurrentIndex()}, or throw an {@link IllegalStateException} if {@link #ledgerCurrentIndex()} is
   * empty.
   *
   * @return The value of {@link #ledgerCurrentIndex()}.
   *
   * @throws IllegalStateException If {@link #ledgerCurrentIndex()} is empty.
   */
  @JsonIgnore
  @Value.Auxiliary
  default LedgerIndex ledgerCurrentIndexSafe() {
    return ledgerCurrentIndex()
      .orElseThrow(() -> new IllegalStateException("Result did not contain a ledgerCurrentIndex."));
  }

  /**
   * True if this data is from a validated ledger version; if false, this data is not final.
   *
   * @return {@code true} if this data is from a validated ledger version, otherwise {@code false}.
   */
  @Value.Default
  default boolean validated() {
    return false;
  }

  /**
   * The ID of the ledger entry returned.
   *
   * @return The {@link Hash256} representing the ID of the ledger entry.
   */
  Hash256 index();
}
