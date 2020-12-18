package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.flags.Flags;

import java.util.Optional;

/**
 * An OfferCancel transaction removes an Offer object from the XRP Ledger.
 *
 * @see "https://xrpl.org/offercancel.html"
 */
@Immutable
@JsonSerialize(as = ImmutableOfferCancel.class)
@JsonDeserialize(as = ImmutableOfferCancel.class)
public interface OfferCancel extends Transaction {

  static ImmutableOfferCancel.Builder builder() {
    return ImmutableOfferCancel.builder();
  }

  /**
   * Set of {@link Flags.TransactionFlags}s for this {@link OfferCancel}, which only allows {@code tfFullyCanonicalSig}
   * flag.
   *
   * <p>The value of the flags cannot be set manually, but exists for JSON serialization/deserialization only and for
   * proper signature computation in rippled.
   *
   * @return Always {@link Flags.TransactionFlags} with {@code tfFullyCanonicalSig} set.
   */
  @JsonProperty("Flags")
  @Value.Default
  default Flags.TransactionFlags flags() {
    return new Flags.TransactionFlags.Builder().tfFullyCanonicalSig(true).build();
  }

  /**
   * The sequence number of a previous {@link OfferCreate} transaction.
   *
   * @return An {@link Optional} of type {@link UnsignedInteger} representing the offer sequence.
   */
  @JsonProperty("OfferSequence")
  Optional<UnsignedInteger> offerSequence();

}
