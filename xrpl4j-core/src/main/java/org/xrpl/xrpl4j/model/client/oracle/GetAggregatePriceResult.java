package org.xrpl.xrpl4j.model.client.oracle;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.annotations.Beta;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Lazy;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Result object for {@code get_aggregate_price} RPC calls.
 */
@Beta
@Immutable
@JsonSerialize(as = ImmutableGetAggregatePriceResult.class)
@JsonDeserialize(as = ImmutableGetAggregatePriceResult.class)
public interface GetAggregatePriceResult extends XrplResult {

  /**
   * Construct a {@code GetAggregatePriceResult} builder.
   *
   * @return An {@link ImmutableGetAggregatePriceResult.Builder}.
   */
  static ImmutableGetAggregatePriceResult.Builder builder() {
    return ImmutableGetAggregatePriceResult.builder();
  }

  /**
   * The statistics from the collected oracle prices.
   *
   * @return An {@link AggregatePriceSet}.
   */
  @JsonProperty("entire_set")
  AggregatePriceSet entireSet();

  /**
   * The trimmed statistics from the collected oracle prices. Only appears if the trim field was specified in the
   * request.
   *
   * @return An {@link Optional} {@link AggregatePriceSet}.
   */
  @JsonProperty("trimmed_set")
  Optional<AggregatePriceSet> trimmedSet();

  /**
   * The median price, as a {@link String}.
   *
   * @return A {@link String}.
   */
  @JsonProperty("median")
  String medianString();

  /**
   * Get the median price as a {@link BigDecimal}.
   *
   * @return A {@link BigDecimal}.
   */
  @Lazy
  @JsonIgnore
  default BigDecimal median() {
    return new BigDecimal(medianString());
  }

  /**
   * The most recent timestamp out of all LastUpdateTime values.
   *
   * @return An {@link UnsignedInteger}.
   */
  UnsignedInteger time();

  /**
   * The identifying Hash of the ledger version used to generate this response.
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
   * The Ledger Index of the ledger version used to generate this response. Only present in responses to requests with
   * ledger_index = "validated" or "closed".
   *
   * @return A {@link LedgerIndex}.
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
   * The ledger index of the current open ledger, which was used when retrieving this information. Only present in
   * responses to requests with ledger_index = "current".
   *
   * @return An optionally-present {@link LedgerIndex} representing the current ledger index.
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
   * If true, the information in this response comes from a validated ledger version. Otherwise, the information is
   * subject to change.
   *
   * @return {@code true} if the information in this response comes from a validated ledger version, {@code false} if
   *   not.
   */
  @Value.Default
  default boolean validated() {
    return false;
  }
}
