package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;

import java.util.List;
import java.util.Optional;

/**
 * The result of an "account_nfts" rippled API method call.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAccountNftsResult.class)
@JsonDeserialize(as = ImmutableAccountNftsResult.class)
public interface AccountNftsResult  extends XrplResult {
  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableAccountNftsResult.Builder}.
   */
  static ImmutableAccountNftsResult.Builder builder() {
    return ImmutableAccountNftsResult.builder();
  }

  /**
   * The unique {@link Address} for the account that made the request.
   *
   * @return The unique {@link Address} for the account that made the request.
   */
  Address account();

  /**
   * All of the NFTs currently owned by the specified account.
   * @return {@link List} of all {@link NfTokenObject}s owned by an account.
   */
  @JsonProperty("account_nfts")
  List<NfTokenObject> accountNfts();

  /**
   * The ledger index of the current open ledger, which was used when retrieving this information. Only present
   * in responses to requests with ledger_index = "current".
   *
   * @return An optionally-present {@link LedgerIndex} representing the current ledger index.
   */
  @JsonProperty("ledger_current_index")
  Optional<LedgerIndex> ledgerCurrentIndex();

  /**
   * If true, the information in this response comes from a validated ledger version.
   * Otherwise, the information is subject to change.
   *
   * @return {@code true} if the information in this response comes from a validated ledger version, {@code false}
   *   if not.
   */
  @Value.Default
  default boolean validated() {
    return false;
  }

  /**
   * The limit to how many {@link #accountNfts()} were actually returned by this request.
   *
   * @return An optionally-present {@link UnsignedInteger} containing the request limit.
   */
  Optional<UnsignedInteger> limit();

  /**
   * Server-defined value for pagination. Pass this to the next call to resume getting results where this
   * call left off. Omitted when there are no additional pages after this one.
   *
   * @return An optionally-present {@link String} containing the response marker.
   */
  Optional<Marker> marker();


}


@Value.Immutable
@JsonSerialize(as = ImmutableAccountLinesResult.class)
@JsonDeserialize(as = ImmutableAccountLinesResult.class)
public interface AccountLinesResult extends XrplResult {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableAccountLinesResult.Builder}.
   */
  static ImmutableAccountLinesResult.Builder builder() {
    return ImmutableAccountLinesResult.builder();
  }

  /**
   * The unique {@link Address} for the account that made the request.
   *
   * @return The unique {@link Address} for the account that made the request.
   */
  Address account();

  /**
   * A {@link List} of {@link TrustLine}s.  If the number of {@link TrustLine}s for this account is large,
   * this will contain up to {@link AccountLinesRequestParams#limit()} entries.
   *
   * @return A {@link List} of {@link TrustLine}s for the requested account.
   */
  List<TrustLine> lines();

  /**
   * The identifying hash the ledger version that was used when retrieving this data.
   *
   * @return An optionally-present {@link org.xrpl.xrpl4j.model.transactions.Hash256}.
   */
  @JsonProperty("ledger_hash")
  Optional<Hash256> ledgerHash();

  /**
   * Get {@link #ledgerHash()}, or throw an {@link IllegalStateException} if {@link #ledgerHash()} is empty.
   *
   * @return The value of {@link #ledgerHash()}.
   * @throws IllegalStateException If {@link #ledgerHash()} is empty.
   */
  @JsonIgnore
  @Value.Auxiliary
  default Hash256 ledgerHashSafe() {
    return ledgerHash()
        .orElseThrow(() -> new IllegalStateException("Result did not contain a ledgerHash."));
  }

  /**
   * The ledger index of the ledger version that was used when retrieving this data.
   *
   * @return An optionally-present {@link LedgerIndex} representing the ledger index of the response.
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
   * The ledger index of the current open ledger, which was used when retrieving this information.
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
   * @throws IllegalStateException If {@link #ledgerCurrentIndex()} is empty.
   */
  @JsonIgnore
  @Value.Auxiliary
  default LedgerIndex ledgerCurrentIndexSafe() {
    return ledgerCurrentIndex()
        .orElseThrow(() -> new IllegalStateException("Result did not contain a ledgerCurrentIndex."));
  }

  /**
   * Server-defined value indicating the response is paginated. Pass this to the next call to resume where this
   * call left off. Omitted when there are no additional pages after this one.
   *
   * @return An optionally-present {@link String} containing a marker.
   */
  Optional<Marker> marker();

}
