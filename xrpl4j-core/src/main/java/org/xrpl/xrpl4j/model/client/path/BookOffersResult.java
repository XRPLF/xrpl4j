package org.xrpl.xrpl4j.model.client.path;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.util.List;
import java.util.Optional;

/**
 * Response object for a "book_offers" rippled API method call.
 *
 * @see "https://xrpl.org/book_offers.html"
 */
@Value.Immutable
@JsonSerialize(as = ImmutableBookOffersResult.class)
@JsonDeserialize(as = ImmutableBookOffersResult.class)
public interface BookOffersResult extends XrplResult {

  /**
   * Construct a {@code BookOffersResult} builder.
   *
   * @return An {@link ImmutableBookOffersResult.Builder}.
   */
  static ImmutableBookOffersResult.Builder builder() {
    return ImmutableBookOffersResult.builder();
  }

  /**
   * A {@link List} of {@link BookOffersOffer}s containing information about the offers in the requested order book.
   *
   * @return A {@link List} of {@link BookOffersOffer}s.
   */
  List<BookOffersOffer> offers();

  /**
   * A 20-byte hex string for the ledger version to use.
   *
   * @return An optionally-present {@link Hash256} containing the ledger hash.
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
   * (Omitted if ledger_current_index is provided instead) The ledger index of the ledger version used when retrieving
   * this information. The information does not contain any changes from ledger versions newer than this one.
   *
   * @return An optionally-present {@link LedgerIndex}.
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
   * (Omitted if ledger_index is provided instead) The ledger index of the current in-progress ledger, which was used
   * when retrieving this information.
   *
   * @return An optionally-present {@link LedgerIndex}.
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

}
