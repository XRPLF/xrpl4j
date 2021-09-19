package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Marker;

import java.util.List;
import java.util.Optional;

/**
 * The result of an account_offers rippled call.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAccountOffersResult.class)
@JsonDeserialize(as = ImmutableAccountOffersResult.class)
public interface AccountOffersResult extends XrplResult {

  static ImmutableAccountOffersResult.Builder builder() {
    return ImmutableAccountOffersResult.builder();
  }

  /**
   * Unique {@link Address} identifying the account that made the offers.
   *
   * @return The {@link Address} of the account.
   */
  Address account();

  /**
   * Array of {@link OfferResultObject}, which each represent an offer made by this
   * account that is outstanding as of the requested ledger version. If
   * the number of offers is large, only returns up to limit at a time.
   *
   * @return A {@link List} of {@link OfferResultObject}
   */
  List<OfferResultObject> offers();

  /**
   * Omitted if ledger_hash or ledger_index provided. The ledger index
   * of the current in-progress ledger version, which was used when retrieving this data.
   *
   * @return A {@link LedgerIndex}.
   */
  @JsonProperty("ledger_current_index")
  Optional<LedgerIndex> ledgerCurrentIndex();

  /**
   * The Ledger Index of the ledger version used to generate this response.
   *
   * @return A {@link LedgerIndex}.
   */
  @JsonProperty("ledger_index")
  Optional<LedgerIndex> ledgerIndex();

  /**
   * The ledger index that was used when retrieving this result, regardless of whether the ledger has been validated,
   * closed, or is still open.
   *
   * @return The {@link LedgerIndex} found in {@link #ledgerIndex()} or {@link #ledgerCurrentIndex()}, depending
   *   on which one is present.
   */
  @JsonIgnore
  @Value.Derived
  default LedgerIndex ledgerIndexSafe() {
    return ledgerIndex()
      .orElseGet(() ->
        ledgerCurrentIndex()
          .orElseThrow(() -> new IllegalStateException("Result did not contain ledger_index or ledger_current_index."))
      );
  }

  /**
   * The identifying Hash of the ledger version used to generate this response.
   *
   * @return A {@link Hash256} containing the ledger hash.
   */
  @JsonProperty("ledger_hash")
  Optional<Hash256> ledgerHash();

  /**
   * Server-defined value for pagination. Pass this to the next call to resume getting results where this
   * call left off. Omitted when there are no additional pages after this one.
   *
   * @return An optionally-present {@link String} containing the response marker.
   */
  Optional<Marker> marker();
}
