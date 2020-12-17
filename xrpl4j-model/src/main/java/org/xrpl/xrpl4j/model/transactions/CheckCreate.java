package org.xrpl.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.flags.Flags.TransactionFlags;

import java.util.Optional;

/**
 * Create a Check object in the ledger, which is a deferred payment that can be cashed by its intended destination.
 * The sender of this transaction is the sender of the Check.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableCheckCreate.class)
@JsonDeserialize(as = ImmutableCheckCreate.class)
public interface CheckCreate extends Transaction {

  static ImmutableCheckCreate.Builder builder() {
    return ImmutableCheckCreate.builder();
  }

  /**
   * Set of {@link Flags.TransactionFlags}s for this {@link AccountDelete}, which only allows tfFullyCanonicalSig flag.
   *
   * <p>The value of the flags cannot be set manually, but exists for JSON serialization/deserialization only and for
   * proper signature computation in rippled.
   */
  @JsonProperty("Flags")
  @Derived
  default TransactionFlags flags() {
    return new TransactionFlags.Builder().tfFullyCanonicalSig(true).build();
  }

  /**
   * The unique {@link Address} of the account that can cash the Check.
   */
  @JsonProperty("Destination")
  Address destination();

  /**
   * Arbitrary tag that identifies the reason for the Check, or a hosted recipient to pay.
   */
  @JsonProperty("DestinationTag")
  Optional<UnsignedInteger> destinationTag();

  /**
   * Maximum amount of source currency the Check is allowed to debit the sender, including transfer fees on
   * non-XRP currencies. The Check can only credit the destination with the same currency
   * (from the same issuer, for non-XRP currencies). For non-XRP amounts, the nested field names MUST be lower-case.
   */
  @JsonProperty("SendMax")
  CurrencyAmount sendMax();

  /**
   * Time after which the Check is no longer valid, in
   * <a href="https://xrpl.org/basic-data-types.html#specifying-time">seconds since the Ripple Epoch</a>.
   */
  @JsonProperty("Expiration")
  Optional<UnsignedInteger> expiration();

  /**
   * Arbitrary 256-bit hash representing a specific reason or identifier for this Check.
   */
  @JsonProperty("InvoiceID")
  Optional<Hash256> invoiceId();

}
