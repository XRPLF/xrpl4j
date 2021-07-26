package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.client.accounts.ImmutableAccountChannelsRequestParams;
import org.xrpl.xrpl4j.model.flags.Flags;

import java.util.Optional;

/**
 * An OfferCreate transaction is effectively a limit order. It defines an intent to exchange currencies, and
 * creates an offer object if not completely fulfilled when placed. Offers can be partially fulfilled.
 *
 * @see "https://xrpl.org/offercreate.html"
 */
@Immutable
@JsonSerialize(as = ImmutableOfferCreate.class)
@JsonDeserialize(as = ImmutableOfferCreate.class)
public interface OfferCreate extends Transaction {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableOfferCreate.Builder}.
   */
  static ImmutableOfferCreate.Builder builder() {
    return ImmutableOfferCreate.builder();
  }

  /**
   * Set of {@link Flags.OfferCreateFlags}s for this {@link OfferCreate}.
   *
   * @return The {@link org.xrpl.xrpl4j.model.flags.Flags.OfferCreateFlags} for this transaction.
   */
  @JsonProperty("Flags")
  @Value.Default
  default Flags.OfferCreateFlags flags() {
    return Flags.OfferCreateFlags.builder().tfFullyCanonicalSig(true).build();
  }

  /**
   * An offer to delete first, specified in the same way as an Offer Cancel.
   *
   * @return An {@link Optional} of type {@link UnsignedInteger} representing the offer sequence.
   */
  @JsonProperty("OfferSequence")
  Optional<UnsignedInteger> offerSequence();

  /**
   * The amount and type of currency being requested by the offer creator.
   *
   * @return A {@link CurrencyAmount} representing the amount being requested.
   */
  @JsonProperty("TakerPays")
  CurrencyAmount takerPays();

  /**
   * The amount and type of currency being provided by the offer creator.
   *
   * @return A {@link CurrencyAmount} representing the amount being offered.
   */
  @JsonProperty("TakerGets")
  CurrencyAmount takerGets();

  /**
   * Time after which the Check is no longer valid, in
   * <a href="https://xrpl.org/basic-data-types.html#specifying-time">seconds since the Ripple Epoch</a>.
   *
   * @return An {@link Optional} of type {@link UnsignedInteger} representing the expiration time.
   */
  @JsonProperty("Expiration")
  Optional<UnsignedInteger> expiration();


}
