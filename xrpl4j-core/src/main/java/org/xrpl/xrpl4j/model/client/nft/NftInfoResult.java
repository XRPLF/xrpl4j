package org.xrpl.xrpl4j.model.client.nft;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.flags.NfTokenFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.NfTokenId;
import org.xrpl.xrpl4j.model.transactions.NfTokenUri;
import org.xrpl.xrpl4j.model.transactions.TransferFee;

import java.util.Optional;

/**
 * The result of an {@code nft_info} RPC call.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableNftInfoResult.class)
@JsonDeserialize(as = ImmutableNftInfoResult.class)
public interface NftInfoResult extends XrplResult {

  /**
   * Construct a {@code NftInfoResult} builder.
   *
   * @return An {@link ImmutableNftInfoResult.Builder}.
   */
  static ImmutableNftInfoResult.Builder builder() {
    return ImmutableNftInfoResult.builder();
  }

  /**
   * A unique identifier for the non-fungible token (NFT).
   *
   * @return An {@link NfTokenId}.
   */
  @JsonProperty("nft_id")
  NfTokenId nfTokenId();

  /**
   * The ledger index of the most recent ledger version where the state of this NFT was modified, as in the NFT was
   * minted (created), changed ownership (traded), or burned (destroyed). The information returned contains whatever
   * happened most recently compared to the requested ledger.
   *
   * @return A {@link LedgerIndex}.
   */
  @JsonProperty("ledger_index")
  LedgerIndex ledgerIndex();

  /**
   * The account ID of this NFT's owner at this ledger index.
   *
   * @return An {@link Address}.
   */
  Address owner();

  /**
   * Whether the NFT is burned at the request ledger.
   *
   * @return {@code true} if the NFT is burned at this ledger, or {@code false} otherwise.
   */
  @JsonProperty("is_burned")
  boolean burned();

  /**
   * The flag set of this NFT.
   *
   * @return An {@link NfTokenFlags}.
   */
  NfTokenFlags flags();

  /**
   * The transfer fee of this NFT.
   *
   * @return A {@link TransferFee}.
   */
  @JsonProperty("transfer_fee")
  TransferFee transferFee();

  /**
   * The account ID which denotes the issuer of this NFT.
   *
   * @return An {@link Address}.
   */
  Address issuer();

  /**
   * The NFT’s taxon.
   *
   * @return An {@link UnsignedLong} denoting the taxon.
   */
  @JsonProperty("nft_taxon")
  UnsignedLong nftTaxon();

  /**
   * The NFT’s sequence number.
   *
   * @return An {@link UnsignedInteger}.
   */
  @JsonProperty("nft_serial")
  UnsignedInteger nftSerial();

  /**
   * This field is empty if the NFT is not burned at this ledger but does not have a URI. If the NFT is not burned at
   * this ledger, and it does have a URI, this field is a string containing the decoded URI of the NFT.
   *
   * <p>NOTE: If you need to retrieve the URI of a burnt token, re-request nft_info for this token, specifying the
   * ledger_index as the one previous to the index where this token was burned.
   *
   * @return An optionally-present {@link NfTokenUri}.
   */
  Optional<NfTokenUri> uri();

}
