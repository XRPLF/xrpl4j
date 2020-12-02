package com.ripple.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

import java.util.Optional;

/**
 * An OfferCreate transaction is effectively a limit order . It defines an intent to exchange currencies, and
 * creates an offer object if not completely fulfilled when placed. Offers can be partially fulfilled.
 *
 * @see "https://xrpl.org/offercreate.html"
 */
@Immutable
@JsonSerialize(as = ImmutableOfferCreate.class)
@JsonDeserialize(as = ImmutableOfferCreate.class)
public interface OfferCreate extends Transaction {

  static ImmutableOfferCreate.Builder builder() {
    return ImmutableOfferCreate.builder();
  }

  @JsonProperty("Flags")
  @Value.Default
  default Flags.OfferFlags flags() {
    return Flags.OfferFlags.builder().fullyCanonicalSig(true).build();
  }

  /**
   * An offer to delete first, specified in the same way as an Offer Cancel.
   */
  @JsonProperty("OfferSequence")
  Optional<UnsignedInteger> offerSequence();

  /**
   * The amount and type of currency being requested by the offer creator.
   *
   * @return amount
   */
  @JsonProperty("TakerPays")
  CurrencyAmount takerPays();

  /**
   * The amount and type of currency being provided by the offer creator.
   *
   * @return amount
   */
  @JsonProperty("TakerGets")
  CurrencyAmount takerGets();

  /**
   * Time after which the Check is no longer valid, in
   * <a href="https://xrpl.org/basic-data-types.html#specifying-time">seconds since the Ripple Epoch</a>.
   */
  @JsonProperty("Expiration")
  Optional<UnsignedInteger> expiration();


}
