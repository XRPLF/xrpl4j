package org.xrpl.xrpl4j.model.transactions.metadata;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: model
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.Optional;

/**
 * The PayChannel object type represents a payment channel. Payment channels enable small, rapid off-ledger payments of
 * XRP that can be later reconciled with the consensus ledger. A payment channel holds a balance of XRP that can only
 * be paid out to a specific destination address until the channel is closed. Any unspent XRP is returned to the
 * channel's owner (the source address that created and funded it) when the channel closes.
 *
 * <p>The {@link org.xrpl.xrpl4j.model.transactions.PaymentChannelCreate} transaction type creates a PayChannel object.
 * The {@link org.xrpl.xrpl4j.model.transactions.PaymentChannelFund} and
 * {@link org.xrpl.xrpl4j.model.transactions.PaymentChannelClaim} transaction types modify existing PayChannel objects.
 *
 * <p>When a payment channel expires, at first it remains on the ledger, because only new transactions can modify
 * ledger contents. Transaction processing automatically closes a payment channel when any transaction accesses it
 * after the expiration. To close an expired channel and return the unspent XRP to the owner, some address must send
 * a new {@link org.xrpl.xrpl4j.model.transactions.PaymentChannelClaim} or
 * {@link org.xrpl.xrpl4j.model.transactions.PaymentChannelFund} transaction accessing the channel.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableMetaPayChannelObject.class)
@JsonDeserialize(as = ImmutableMetaPayChannelObject.class)
public interface MetaPayChannelObject extends MetaLedgerObject {

  /**
   * A bit-map of boolean flags enabled for this payment channel. Currently, the protocol defines no flags for
   * PayChannel objects.
   *
   * @return Always {@link Flags#UNSET}.
   */
  @JsonProperty("Flags")
  @Value.Derived
  default Flags flags() {
    return Flags.UNSET;
  }

  /**
   * The source {@link Address} that owns this payment channel. This comes from the sending {@link Address} of
   * the {@link org.xrpl.xrpl4j.model.transactions.PaymentChannelCreate} transaction that created the channel.
   *
   * @return The {@link Address} of the source account.
   */
  @JsonProperty("Account")
  Optional<Address> account();

  /**
   * The destination {@link Address} for this payment channel. While the payment channel is open, this {@link Address}
   * is the only one that can receive XRP from the channel. This comes from the
   * {@link org.xrpl.xrpl4j.model.transactions.PaymentChannelCreate#destination()} field of the transaction
   * that created the channel.
   *
   * @return The {@link Address} of the destination account.
   */
  @JsonProperty("Destination")
  Optional<Address> destination();

  /**
   * Total XRP, in drops, that has been allocated to this channel. This includes XRP that has been paid to the
   * {@link #destination()} address. This is initially set by the
   * {@link org.xrpl.xrpl4j.model.transactions.PaymentChannelCreate} transaction that created the channel and can
   * be increased if the source address sends a {@link org.xrpl.xrpl4j.model.transactions.PaymentChannelFund}
   * transaction.
   *
   * @return An {@link XrpCurrencyAmount} representing the total amount allocated to this channel.
   */
  @JsonProperty("Amount")
  Optional<XrpCurrencyAmount> amount();

  /**
   * Total XRP, in drops, already paid out by the channel. The difference between this value and the
   * {@link #amount()} field is how much XRP can still be paid to the {@link #destination()} address with
   * {@link org.xrpl.xrpl4j.model.transactions.PaymentChannelClaim} transactions. If the channel closes,
   * the remaining difference is returned to the {@link #account()} address.
   *
   * @return An {@link XrpCurrencyAmount} representing the total amount paid out by this channel.
   */
  @JsonProperty("Balance")
  Optional<XrpCurrencyAmount> balance();

  /**
   * Public key, in hexadecimal, of the key pair that can be used to sign claims against this channel.
   * This can be any valid secp256k1 or Ed25519 public key. This is set by the
   * {@link org.xrpl.xrpl4j.model.transactions.PaymentChannelCreate} transaction that created the channel and
   * must match the public key used in claims against the channel. The channel {@link #account()} can also send
   * XRP from this channel to the {@link #destination()} without signed claims.
   *
   * @return A {@link String} containing the payment channel's public key.
   */
  @JsonProperty("PublicKey")
  Optional<String> publicKey();

  /**
   * Number of seconds the source address must wait to close the channel if it still has any XRP in it.
   * Smaller values mean that the {@link #destination()} has less time to redeem any outstanding claims after the
   * source address requests to close the channel.
   *
   * @return An {@link UnsignedLong} representing the settlement delay.
   */
  @JsonProperty("SettleDelay")
  Optional<UnsignedLong> settleDelay();

  /**
   * A hint indicating which page of the source address's owner directory links to this object, in case
   * the directory consists of multiple pages.
   *
   * @return A {@link String} containing the hint.
   */
  @JsonProperty("OwnerNode")
  Optional<String> ownerNode();

  /**
   * The identifying hash of the transaction that most recently modified this object.
   *
   * @return A {@link Hash256} containing the previous transaction hash.
   */
  @JsonProperty("PreviousTxnID")
  Optional<Hash256> previousTransactionId();

  /**
   * The index of the ledger that contains the transaction that most recently modified this object.
   *
   * @return An {@link LedgerIndex} representing the previous transaction ledger sequence.
   */
  @JsonProperty("PreviousTxnLgrSeq")
  Optional<LedgerIndex> previousTransactionLedgerSequence();

  /**
   * The mutable expiration time for this payment channel, in
   * <a href="https://xrpl.org/basic-data-types.html#specifying-time">seconds since the Ripple Epoch</a>.
   * The channel is expired if this value is present and smaller than the previous
   * {@link org.xrpl.xrpl4j.model.ledger.LedgerHeader#closeTime()} field.
   *
   * @return An {@link Optional} of type {@link UnsignedLong} representing the expiration time.
   */
  @JsonProperty("Expiration")
  Optional<UnsignedLong> expiration();

  /**
   * The immutable expiration time for this payment channel, in
   * <a href="https://xrpl.org/basic-data-types.html#specifying-time">seconds since the Ripple Epoch</a>.
   * This channel is expired if this value is present and smaller than the previous
   * {@link org.xrpl.xrpl4j.model.ledger.LedgerHeader#closeTime()} field.
   *
   * @return An {@link Optional} of type {@link UnsignedLong} representing the cancel after time.
   */
  @JsonProperty("CancelAfter")
  Optional<UnsignedLong> cancelAfter();

  /**
   * An arbitrary tag to further specify the {@link #account()} for this payment channel, such as a hosted recipient
   * at the owner's address.
   *
   * @return An {@link Optional} of type {@link UnsignedInteger} representing the tag of the source account.
   */
  @JsonProperty("SourceTag")
  Optional<UnsignedInteger> sourceTag();

  /**
   * An arbitrary tag to further specify the {@link #destination()} for this payment channel, such as a hosted
   * recipient at the destination address.
   *
   * @return An {@link Optional} of type {@link UnsignedInteger} representing the tag of the destination account.
   */
  @JsonProperty("DestinationTag")
  Optional<UnsignedInteger> destinationTag();
}
