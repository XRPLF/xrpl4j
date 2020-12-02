package com.ripple.xrpl4j.model.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ripple.xrpl4j.model.transactions.Flags.PaymentChannelClaimFlags;
import org.immutables.value.Value;

import java.util.Optional;

/**
 * Claim XRP from a payment channel, adjust the payment channel's expiration, or both. This transaction can be
 * used differently depending on the transaction sender's role in the specified channel:
 *
 * <p>The source address of a channel can:
 * <ul>
 *  <li>Send XRP from the channel to the destination with or without a signed Claim.</li>
 *  <li>Set the channel to expire as soon as the channel's SettleDelay has passed.</li>
 *  <li>Clear a pending Expiration time.</li>
 *  <li>Close a channel immediately, with or without processing a claim first. The source address cannot
 *   close the channel immediately if the channel has XRP remaining.</li>
 * </ul>
 *
 * <p>The destination address of a channel can:
 * <ul>
 *  <li>Receive XRP from the channel using a signed Claim.</li>
 *  <li>
 *    Close the channel immediately after processing a Claim, refunding any unclaimed XRP to the channel's source.
 *  </li>
 * </ul>
 *
 * <p>Any address sending this transaction can:
 * <ul>
 * <li>Cause a channel to be closed if its Expiration or CancelAfter time is older than the previous ledger's
 *  close time. Any validly-formed PaymentChannelClaim transaction has this effect regardless of the contents
 *  of the transaction.</li>
 * </ul>
 */
@Value.Immutable
@JsonSerialize(as = ImmutablePaymentChannelClaim.class)
@JsonDeserialize(as = ImmutablePaymentChannelClaim.class)
public interface PaymentChannelClaim extends Transaction {

  static ImmutablePaymentChannelClaim.Builder builder() {
    return ImmutablePaymentChannelClaim.builder();
  }

  /**
   * Bit-map of boolean {@link Flags.PaymentChannelClaimFlags} to set for this transaction.
   */
  @JsonProperty("Flags")
  @Value.Default
  default PaymentChannelClaimFlags flags() {
    return PaymentChannelClaimFlags.builder().build();
  }

  /**
   * The unique ID of the channel, as a {@link Hash256}.
   */
  @JsonProperty("Channel")
  Hash256 channel();

  /**
   * Total amount of XRP, in drops, delivered by this channel after processing this claim. Required to deliver XRP.
   * Must be more than the total amount delivered by the channel so far, but not greater than the {@link #amount()}
   * of the signed claim. Must be provided except when closing the channel.
   */
  @JsonProperty("Balance")
  Optional<XrpCurrencyAmount> balance();

  /**
   * The amount of XRP, in drops, authorized by the {@link #signature()}. This must match the amount in
   * the signed message. This is the cumulative amount of XRP that can be dispensed by the channel,
   * including XRP previously redeemed.
   */
  @JsonProperty("Amount")
  Optional<XrpCurrencyAmount> amount();

  /**
   * The signature of this claim, in hexadecimal form. The signed message contains the channel ID and the amount
   * of the claim. Required unless the sender of the transaction is the source address of the channel.
   */
  @JsonProperty("Signature")
  Optional<String> signature();

  /**
   * The public key used for the {@link #signature()}, as hexadecimal. This must match the PublicKey stored
   * in the ledger for the channel. Required unless the sender of the transaction is the source
   * address of the channel and the {@link #signature()} field is omitted.
   * (The transaction includes the public key so that rippled can check the validity of the signature
   * before trying to apply the transaction to the ledger.)
   */
  @JsonProperty("PublicKey")
  Optional<String> publicKey();

}
