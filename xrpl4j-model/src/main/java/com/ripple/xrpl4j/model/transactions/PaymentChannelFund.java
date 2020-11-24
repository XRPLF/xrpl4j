package com.ripple.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;

import java.util.Optional;

/**
 * Add additional XRP to an open payment channel, update the expiration time of the channel, or both.
 * Only the source address of the channel can use this transaction.
 *
 * @see "https://xrpl.org/paymentchannelfund.html"
 */
@Value.Immutable
@JsonSerialize(as = ImmutablePaymentChannelFund.class)
@JsonDeserialize(as = ImmutablePaymentChannelFund.class)
public interface PaymentChannelFund extends Transaction<Flags.TransactionFlags> {

  static ImmutablePaymentChannelFund.Builder builder() {
    return ImmutablePaymentChannelFund.builder();
  }

  @Override
  @JsonProperty("Flags")
  @Value.Default
  default Flags.TransactionFlags flags() {
    return new Flags.TransactionFlags.Builder().fullyCanonicalSig(true).build();
  }

  /**
   * The unique ID of the channel to fund.
   */
  @JsonProperty("Channel")
  Hash256 channel();

  /**
   * Amount of XRP, in drops to add to the channel. This field is required, therefore it is not possible to
   * set the {@link #expiration()} without adding value to the channel.  However, you can change the expiration
   * and add a negligible amount of XRP (like 1 drop) to the channel.
   */
  @JsonProperty("Amount")
  XrpCurrencyAmount amount();

  /**
   * New Expiration time to set for the channel, in seconds since the Ripple Epoch. This must be later than
   * either the current time plus the SettleDelay of the channel, or the existing Expiration of the channel.
   * After the Expiration time, any transaction that would access the channel closes the channel without
   * taking its normal action. Any unspent XRP is returned to the source address when the channel closes.
   * (Expiration is separate from the channel's immutable CancelAfter time.)
   */
  @JsonProperty("Expiration")
  Optional<UnsignedLong> expiration();
}
