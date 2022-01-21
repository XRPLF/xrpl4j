package org.xrpl.xrpl4j.model.client.nft;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.Optional;

/**
 * An Offer returned in an {@link NfTokenOfferObject} list.
 * Note that this object is NOT the same as the Offer ledger object.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableNfTokenOfferObject.class)
@JsonDeserialize(as = ImmutableNfTokenOfferObject.class)
public interface NfTokenOfferObject {

  static ImmutableNfTokenOfferObject.Builder builder() {
    return ImmutableNfTokenOfferObject.builder();
  }

  /**
   * The amount of XRP, in drops, expected or offered for the token.
   *
   * @return the xrp currency amount
   */
  @JsonProperty("Amount")
  XrpCurrencyAmount amount();

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
   * A time after which this offer is considered unfunded, as the number of seconds since
   * the Ripple Epoch.
   *
   * @return The offer's expiration.
   */
  Optional<UnsignedInteger> expiration();

  /**
   * A hint indicating which page of the token buy or sell offer directory links to this object.
   *
   * @return A {@link String} containing the hint.
   */
  @JsonProperty("OfferNode")
  Optional<String> offerNode();
}
