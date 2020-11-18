package com.ripple.xrpl4j.client.model.ledger.objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.model.transactions.Address;
import com.ripple.xrpl4j.model.transactions.Hash256;
import com.ripple.xrpl4j.model.transactions.PaymentChannelCreate;
import com.ripple.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutablePayChannelObject.class)
@JsonDeserialize(as = ImmutablePayChannelObject.class)
public interface PayChannelObject extends LedgerObject {

  static ImmutablePayChannelObject.Builder builder() {
    return ImmutablePayChannelObject.builder();
  }

  /**
   * The type of ledger object, which is always "PayChannel".
   */
  @JsonProperty("LedgerEntryType")
  @Value.Derived
  default LedgerEntryType ledgerEntryType() {
    return LedgerEntryType.PAY_CHANNEL;
  }

  /**
   * The source {@link Address} that owns this payment channel. This comes from the sending {@link Address} of
   * the {@link PaymentChannelCreate} transaction that created the channel.
   */
  @JsonProperty("Account")
  Address account();

  /**
   * The destination {@link Address} for this payment channel. While the payment channel is open, this {@link Address}
   * is the only one that can receive XRP from the channel. This comes from the
   * {@link PaymentChannelCreate#destination()} field of the transaction that created the channel.
   */
  @JsonProperty("Destination")
  Address destination();

  /**
   * Total XRP, in drops, that has been allocated to this channel. This includes XRP that has been paid to the
   * {@link #destination()} address. This is initially set by the {@link PaymentChannelCreate} transaction that
   * created the channel and can be increased if the source address sends a {@link PaymentChannelFund} transaction.
   */
  @JsonProperty("Amount")
  XrpCurrencyAmount amount();

  /**
   * Total XRP, in drops, already paid out by the channel. The difference between this value and the
   * {@link #amount()} field is how much XRP can still be paid to the {@link #destination()} address with
   * {@link PaymentChannelClaim} transactions. If the channel closes, the remaining difference is returned to
   * the {@link #account()} address.
   */
  @JsonProperty("Balance")
  XrpCurrencyAmount balance();

  /**
   * Public key, in hexadecimal, of the key pair that can be used to sign claims against this channel.
   * This can be any valid secp256k1 or Ed25519 public key. This is set by the {@link PaymentChannelCreate}
   * transaction that created the channel and must match the public key used in claims against the channel.
   * The channel {@link #account()} can also send XRP from this channel to the {@link #destination()} without
   * signed claims.
   */
  @JsonProperty("PublicKey")
  String publicKey();

  /**
   * Number of seconds the source address must wait to close the channel if it still has any XRP in it.
   * Smaller values mean that the {@link #destination()} has less time to redeem any outstanding claims after the
   * source address requests to close the channel.
   */
  @JsonProperty("SettleDelay")
  UnsignedInteger settleDelay();

  /**
   * A hint indicating which page of the source address's owner directory links to this object, in case
   * the directory consists of multiple pages.
   */
  @JsonProperty("OwnerNode")
  String ownerNode();

  /**
   * The identifying hash of the transaction that most recently modified this object.
   */
  @JsonProperty("PreviousTxnID")
  Hash256 previousTransactionId();

  /**
   * The index of the ledger that contains the transaction that most recently modified this object.
   */
  @JsonProperty("PreviousTxnLgrSeq")
  UnsignedInteger previousTransactionLedgerSequence();

  /**
   * The mutable expiration time for this payment channel, in
   * <a href="https://xrpl.org/basic-data-types.html#specifying-time">seconds since the Ripple Epoch</a>.
   * The channel is expired if this value is present and smaller than the previous
   * {@link LedgerHeader#closeTime()} field.
   */
  @JsonProperty("Expiration")
  Optional<UnsignedInteger> expiration();

  /**
   * The immutable expiration time for this payment channel, in
   * <a href="https://xrpl.org/basic-data-types.html#specifying-time">seconds since the Ripple Epoch</a>.
   * This channel is expired if this value is present and smaller than the previous {@link LedgerHeader#closeTime()}
   * field.
   */
  @JsonProperty("CancelAfter")
  Optional<UnsignedInteger> cancelAfter();

  /**
   * An arbitrary tag to further specify the {@link #account()} for this payment channel, such as a hosted recipient
   * at the owner's address.
   */
  @JsonProperty("SourceTag")
  Optional<UnsignedInteger> sourceTag();

  /**
   * An arbitrary tag to further specify the {@link #destination()} for this payment channel, such as a hosted
   * recipient at the destination address.
   */
  @JsonProperty("DestinationTag")
  Optional<UnsignedInteger> destinationTag();

  /**
   * Unique ID for this channel.
   */
  Hash256 index();
}
