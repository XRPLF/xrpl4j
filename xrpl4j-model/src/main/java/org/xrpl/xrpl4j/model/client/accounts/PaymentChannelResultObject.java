package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.admin.ImmutableAcceptLedgerResult;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.Optional;

/**
 * A Payment Channel returned in an {@link AccountChannelsResult#channels()} list.
 * Note that this object is NOT the same as the PayChannel ledger object.
 */
@Value.Immutable
@JsonSerialize(as = ImmutablePaymentChannelResultObject.class)
@JsonDeserialize(as = ImmutablePaymentChannelResultObject.class)
public interface PaymentChannelResultObject {


  static ImmutablePaymentChannelResultObject.Builder builder() {
    return ImmutablePaymentChannelResultObject.builder();
  }

  /**
   * The {@link Address} of the owner of the channel.
   *
   * @return The {@link Address} of the owner of the {@link PaymentChannelResultObject}.
   */
  Address account();

  /**
   * The total amount of XRP, in drops, allocated to this channel.
   *
   * @return An {@link XrpCurrencyAmount} denoting the amount allocated to this channel.
   */
  XrpCurrencyAmount amount();

  /**
   * The total amount of XRP, in drops, paid out from this channel, as of the ledger version used.
   * (You can calculate the amount of XRP left in the channel by subtracting this from {@link #amount()}.)
   *
   * @return An {@link XrpCurrencyAmount} denoting the balance of this channel.
   */
  XrpCurrencyAmount balance();

  /**
   * A unique ID for this channel, as a {@link Hash256}. This is also the ID of the channel object in the
   * ledger's state data.
   *
   * @return A {@link Hash256} containing the channel ID.
   */
  @JsonProperty("channel_id")
  Hash256 channelId();

  /**
   * The destination {@link Address} for this channel.  Only this account can receiver the XRP in the
   * channel while it is open.
   *
   * @return The {@link Address} of the destination account.
   */
  @JsonProperty("destination_account")
  Address destinationAccount();

  /**
   * The number of seconds the payment channel must stay open after the owner of the channel requests to close it.
   *
   * @return An {@link UnsignedInteger} representing the settle delay in seconds.
   */
  @JsonProperty("settle_delay")
  UnsignedInteger settleDelay();

  /**
   * The public key for the payment channel in the XRP Ledger's base58 format. Signed claims against this channel
   * must be redeemed with the matching key pair.
   *
   * @return An optionally-present {@link String} containing the public key.
   */
  @JsonProperty("public_key")
  Optional<String> publicKey();

  /**
   * The public key for the payment channel in hexadecimal format, if one was specified at channel creation.
   * Signed claims against this channel must be redeemed with the matching key pair.
   *
   * @return An optionally-present {@link String} containing the public key as hex.
   */
  @JsonProperty("public_key_hex")
  Optional<String> publicKeyHex();

  /**
   * Time, in <a href="https://xrpl.org/basic-data-types.html#specifying-time">seconds since the Ripple Epoch</a>,
   * when this channel is set to expire. This expiration date is mutable.
   * If this is before the close time of the most recent validated ledger, the channel is expired.
   *
   * @return An optionally-present {@link UnsignedLong} representing the expiration in seconds.
   */
  Optional<UnsignedLong> expiration();

  /**
   * Time, in <a href="https://xrpl.org/basic-data-types.html#specifying-time">seconds since the Ripple Epoch</a>,
   * of this channel's immutable expiration, if one was specified at channel creation.
   * If this is before the close time of the most recent validated ledger, the channel is expired.
   *
   * @return An optionally-present {@link UnsignedLong} representing the cancel after time.
   */
  @JsonProperty("cancel_after")
  Optional<UnsignedLong> cancelAfter();

  /**
   * An {@link UnsignedInteger} to use as a source tag for payments through this payment channel, if one was
   * specified at channel creation. This indicates the payment channel's originator or other purpose at
   * the source account. Conventionally, if you bounce payments from this channel, you should specify this
   * value in the DestinationTag of the return payment.
   *
   * @return An optionally-present {@link UnsignedInteger}.
   */
  @JsonProperty("source_tag")
  Optional<UnsignedInteger> sourceTag();

  /**
   * An {@link UnsignedInteger} to use as a destination tag for payments through this channel, if one was
   * specified at channel creation. This indicates the payment channel's beneficiary or other
   * purpose at the destination account.
   *
   * @return An optionally-present {@link UnsignedInteger}.
   */
  @JsonProperty("destination_tag")
  Optional<UnsignedInteger> destinationTag();

}
