package org.xrpl.xrpl4j.model.client.path;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.OfferFlags;
import org.xrpl.xrpl4j.model.ledger.LedgerObject;
import org.xrpl.xrpl4j.model.ledger.OfferObject;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Response object for a "book_offers" rippled API method call.
 *
 * <p>Note that this object duplicates all of the fields of {@link OfferObject} instead of simply containing an
 * {@link OfferObject} field. The offer fields exist at the same JSON level as {@link BookOffersOffer}, but we
 * cannot use {@link JsonUnwrapped} on a field of type {@link OfferObject} because it extends {@link LedgerObject} which
 * has Jackson polymorphic annotations on it and {@link JsonUnwrapped} does not play nicely with polymorphic
 * deserialization.
 *
 * @see "https://xrpl.org/book_offers.html"
 */
@Value.Immutable
@JsonSerialize(as = ImmutableBookOffersOffer.class)
@JsonDeserialize(as = ImmutableBookOffersOffer.class)
public interface BookOffersOffer {

  /**
   * Construct a {@code BookOffersOffer} builder.
   *
   * @return An {@link ImmutableBookOffersOffer.Builder}.
   */
  static ImmutableBookOffersOffer.Builder builder() {
    return ImmutableBookOffersOffer.builder();
  }

  /**
   * The value 0x006F, mapped to the string "Offer", indicates that this object is a {@link OfferObject} object.
   *
   * @return Always {@link org.xrpl.xrpl4j.model.ledger.LedgerObject.LedgerEntryType#OFFER}.
   */
  @JsonProperty("LedgerEntryType")
  @Value.Derived
  default LedgerObject.LedgerEntryType ledgerEntryType() {
    return LedgerObject.LedgerEntryType.OFFER;
  }

  /**
   * The sender of the {@link OfferObject}. Cashing the {@link OfferObject} debits this address's balance.
   *
   * @return The {@link Address} of the offer sender.
   */
  @JsonProperty("Account")
  Address account();

  /**
   * A bit-map of boolean flags.
   *
   * @return A {@link OfferFlags}.
   */
  @JsonProperty("Flags")
  OfferFlags flags();

  /**
   * The sequence number of the {@link org.xrpl.xrpl4j.model.transactions.OfferCreate} transaction that
   * created this offer.
   *
   * @return An {@link UnsignedInteger} representing the sequence number.
   */
  @JsonProperty("Sequence")
  UnsignedInteger sequence();

  /**
   * The remaining amount and type of currency requested by the offer creator.
   *
   * @return A {@link CurrencyAmount}.
   */
  @JsonProperty("TakerPays")
  CurrencyAmount takerPays();


  /**
   * The remaining amount and type of currency being provided by the offer creator.
   *
   * @return A {@link CurrencyAmount}.
   */
  @JsonProperty("TakerGets")
  CurrencyAmount takerGets();


  /**
   * The ID of the Offer Directory that links to this offer.
   *
   * @return A {@link Hash256} containing the ID.
   */
  @JsonProperty("BookDirectory")
  Hash256 bookDirectory();

  /**
   * A hint indicating which page of the offer directory links to this object, in case the directory consists of
   * multiple pages.
   *
   * @return A {@link String} containing the hint.
   */
  @JsonProperty("BookNode")
  String bookNode();

  /**
   * A hint indicating which page of the sender's owner directory links to this object, in case the directory
   * consists of multiple pages.
   * Note: The object does not contain a direct link to the owner directory containing it,
   * since that value can be derived from the Account.
   *
   * @return A {@link String} containing the hint.
   */
  @JsonProperty("OwnerNode")
  String ownerNode();

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
   * Indicates the time after which this offer is considered expired, in
   * <a href="https://xrpl.org/basic-data-types.html#specifying-time">seconds since the Ripple Epoch</a>.
   *
   * @return An {@link Optional} of type {@link UnsignedInteger} representing the expiration of this offer.
   */
  @JsonProperty("Expiration")
  Optional<UnsignedInteger> expiration();

  /**
   * The unique ID of the {@link OfferObject}.
   *
   * @return A {@link Hash256} containing the ID.
   */
  Hash256 index();

  @JsonProperty("owner_funds")
  Optional<String> ownerFunds();

  @Value.Derived
  @JsonIgnore
  default Optional<BigDecimal> ownerFundsBigDecimal() {
    return ownerFunds().map(BigDecimal::new);
  }

  @JsonProperty("taker_gets_funded")
  Optional<CurrencyAmount> takerGetsFunded();

  @JsonProperty("taker_pays_funded")
  Optional<CurrencyAmount> takerPaysFunded();

  String quality();

  @Value.Derived
  @JsonIgnore
  default BigDecimal qualityBigDecimal() {
    return new BigDecimal(quality());
  }
}
