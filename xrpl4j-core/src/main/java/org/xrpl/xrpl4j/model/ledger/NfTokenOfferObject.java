package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.NfTokenOfferFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.NfTokenId;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.Optional;

/**
 * An Offer returned in an {@link NfTokenOfferObject} list.
 * Note that this object is NOT the same as the Offer ledger object.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableNfTokenOfferObject.class)
@JsonDeserialize(as = ImmutableNfTokenOfferObject.class)
public interface NfTokenOfferObject extends LedgerObject {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableNfTokenOfferObject.Builder}.
   */
  static ImmutableNfTokenOfferObject.Builder builder() {
    return ImmutableNfTokenOfferObject.builder();
  }

  /**
   * The type of ledger object. In this case, this is always "NfTokenOffer".
   *
   * @return Always {@link LedgerObject.LedgerEntryType#NFTOKEN_OFFER}.
   */
  @JsonProperty("LedgerEntryType")
  @Value.Derived
  default LedgerEntryType ledgerEntryType() {
    return LedgerEntryType.NFTOKEN_OFFER;
  }

  /**
   * The amount of XRP, in drops, expected or offered for the token.
   *
   * @return The {@link XrpCurrencyAmount}.
   */
  @JsonProperty("Amount")
  XrpCurrencyAmount amount();

  /**
   * {@link Address} of the source account that created and owns the offer.
   *
   * @return The {@link Address} of the NfTokenOffer owner.
   */
  @JsonProperty("Owner")
  Address owner();

  /**
   * The identifying hash of the transaction that most recently modified this object.
   *
   * @return A {@link Hash256} containing the previous transaction hash.
   */
  @JsonProperty("PreviousTxnID")
  Hash256 previousTransactionId();

  /**
   * The index of the ledger that contains the transaction that most recently modified this object.
   *
   * @return An {@link UnsignedInteger} representing the previous transaction ledger sequence.
   */
  @JsonProperty("PreviousTxnLgrSeq")
  UnsignedInteger previousTransactionLedgerSequence();

  /**
   * The TokenId of the NfToken for which the offer has been created.
   *
   * @return {@link org.xrpl.xrpl4j.model.transactions.NfTokenId} of the NfToken.
   */
  @JsonProperty("NFTokenID")
  NfTokenId nfTokenId();

  /**
   * A time after which this offer is considered unfunded, as the number of seconds since
   * the Ripple Epoch.
   *
   * @return The offer's expiration.
   */
  Optional<UnsignedInteger> expiration();

  /**
   * The intended recipient of the {@link org.xrpl.xrpl4j.model.client.accounts.NfTokenObject}.
   * This address will receive the NFToken when the offer is accepted.
   *
   * @return The {@link Address} of the destination.
   */
  @JsonProperty("Destination")
  Optional<Address> destination();

  /**
   * A hint indicating which page of the sender's owner directory links to this object, in case the directory
   * consists of multiple pages.
   * Note: The object does not contain a direct link to the owner directory containing it,
   * since that value can be derived from the Account.
   *
   * @return A {@link String} containing the hint.
   */
  @JsonProperty("OwnerNode")
  Optional<String> ownerNode();

  /**
   * A hint indicating which page of the token buy or sell offer directory links to this object.
   *
   * @return A {@link String} containing the hint.
   */
  @JsonProperty("NFTokenOfferNode")
  Optional<String> offerNode();

  /**
   * A set of boolean {@link NfTokenOfferFlags} containing options
   * enabled for this object.
   *
   * @return The {@link NfTokenOfferFlags} for this object.
   */
  @JsonProperty("Flags")
  NfTokenOfferFlags flags();
}
