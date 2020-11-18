package com.ripple.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

import java.util.Optional;

/**
 * An OfferCancel transaction removes an Offer object from the XRP Ledger.
 *
 * @see "https://xrpl.org/offercancel.html"
 */
@Immutable
@JsonSerialize(as = ImmutableOfferCancel.class)
@JsonDeserialize(as = ImmutableOfferCancel.class)
public interface OfferCancel extends Transaction<Flags.TransactionFlags> {

  static ImmutableOfferCancel.Builder builder() {
    return ImmutableOfferCancel.builder();
  }

  @JsonProperty("Flags")
  @Value.Default
  default Flags.TransactionFlags flags() {
    return new Flags.TransactionFlags.Builder().fullyCanonicalSig(true).build();
  }

  /**
   * The sequence number of a previous {@link OfferCreate} transaction.
   */
  @JsonProperty("OfferSequence")
  Optional<UnsignedInteger> offerSequence();

}
