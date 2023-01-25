package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Marker;

import java.util.List;
import java.util.Optional;

/**
 * The result of an "account_nfts" rippled API method call.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableAccountNftsResult.class)
@JsonDeserialize(as = ImmutableAccountNftsResult.class)
public interface AccountNftsResult extends XrplResult {
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
   * All the NFTs currently owned by the specified account.
   * @return {@link List} of all {@link NfTokenObject}s owned by an account.
   */
  @JsonProperty("account_nfts")
  List<NfTokenObject> accountNfts();

  /**
   * (Omitted if ledger_current_index is provided instead) The ledger index of the ledger version used when
   * retrieving this information. The information does not contain any changes from ledger versions newer than this one.
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
   * (Omitted if ledger_index is provided instead) The ledger index of the current in-progress ledger,
   * which was used when retrieving this information.
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
   * @throws IllegalStateException If {@link #ledgerCurrentIndex()} is empty.
   */
  @JsonIgnore
  @Value.Auxiliary
  default LedgerIndex ledgerCurrentIndexSafe() {
    return ledgerCurrentIndex()
      .orElseThrow(() -> new IllegalStateException("Result did not contain a ledgerCurrentIndex."));
  }

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
