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
import com.ripple.cryptoconditions.Condition;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.EscrowCancel;
import org.xrpl.xrpl4j.model.transactions.EscrowCreate;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.Optional;

/**
 * Represents a held payment of XRP waiting to be executed or canceled. An {@link EscrowCreate} transaction creates an
 * {@link MetaEscrowObject} in the ledger. A successful {@link org.xrpl.xrpl4j.model.transactions.EscrowFinish} or
 * {@link EscrowCancel} transaction deletes the object. If the {@link MetaEscrowObject} has a crypto-condition, the
 * payment can only succeed if an {@link org.xrpl.xrpl4j.model.transactions.EscrowFinish} transaction provides the
 * corresponding fulfillment that satisfies the condition (the only supported crypto-condition type is
 * PREIMAGE-SHA-256). If the {@link MetaEscrowObject} has a {@link MetaEscrowObject#finishAfter()} time, the held
 * payment can only execute after that time.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableMetaEscrowObject.class)
@JsonDeserialize(as = ImmutableMetaEscrowObject.class)
public interface MetaEscrowObject extends MetaLedgerObject {

  /**
   * The {@link Address} of the owner (sender) of this held payment. This is the account that provided the XRP, and gets
   * it back if the held payment is canceled.
   *
   * @return The {@link Address} of the owner of this escrow.
   */
  @JsonProperty("Account")
  Optional<Address> account();

  /**
   * The destination {@link Address} where the XRP is paid if the held payment is successful.
   *
   * @return The {@link Address} of the destination of this escrow.
   */
  @JsonProperty("Destination")
  Optional<Address> destination();

  /**
   * The amount of XRP, in drops, to be delivered by the held payment.
   *
   * @return A {@link XrpCurrencyAmount} denoting the amount.
   */
  @JsonProperty("Amount")
  Optional<XrpCurrencyAmount> amount();

  /**
   * A PREIMAGE-SHA-256 crypto-condition in DER hexadecimal encoding. If present, the
   * {@link org.xrpl.xrpl4j.model.transactions.EscrowFinish} transaction
   * must contain a fulfillment that satisfies this condition.
   *
   * @return An {@link Optional} of type {@link Condition} containing the escrow condition.
   * @see "https://tools.ietf.org/html/draft-thomas-crypto-conditions-04#section-8.1"
   */
  @JsonProperty("Condition")
  Optional<Condition> condition();

  /**
   * The held payment can be canceled if and only if this field is present and the time it specifies has passed.
   * Specifically, this is specified as
   * <a href="https://xrpl.org/basic-data-types.html#specifying-time">seconds since the Ripple Epoch</a> and
   * it "has passed" if it's earlier than the close time of the previous validated ledger.
   *
   * @return An {@link Optional} of type {@link UnsignedLong} representing the cancel after time.
   */
  @JsonProperty("CancelAfter")
  Optional<UnsignedLong> cancelAfter();

  /**
   * The time, in <a href="https://xrpl.org/basic-data-types.html#specifying-time">seconds since the Ripple Epoch</a>,
   * after which this held payment can be finished. Any {@link org.xrpl.xrpl4j.model.transactions.EscrowFinish}
   * transaction before this time fails.
   * (Specifically, this is compared with the close time of the previous validated ledger.)
   *
   * @return An {@link Optional} of type {@link UnsignedLong} representing the finish after time.
   */
  @JsonProperty("FinishAfter")
  Optional<UnsignedLong> finishAfter();

  /**
   * A bit-map of boolean flags. No flags are defined for the {@link MetaEscrowObject} type, so this value is always 0.
   *
   * @return Always {@link Flags#UNSET}.
   */
  @JsonProperty("Flags")
  @Value.Derived
  default Flags flags() {
    return Flags.UNSET;
  }

  /**
   * An arbitrary tag to further specify the source for this held payment, such as a hosted recipient at the owner's
   * address.
   *
   * @return An {@link Optional} of type {@link UnsignedInteger} representing the tag of the owner account.
   */
  @JsonProperty("SourceTag")
  Optional<UnsignedInteger> sourceTag();

  /**
   * An arbitrary tag to further specify the destination for this held payment, such as a hosted recipient at the
   * destination address.
   *
   * @return An {@link Optional} of type {@link UnsignedInteger} representing the tag of the destination account.
   */
  @JsonProperty("DestinationTag")
  Optional<UnsignedInteger> destinationTag();

  /**
   * A hint indicating which page of the owner directory links to this object, in case the directory consists of
   * multiple pages.
   *
   * <p>Note: The object does not contain a direct link to the owner directory containing it, since that value can be
   * derived from the Account.</p>
   *
   * @return An {@link Optional} of type {@link String} containing the owner node hint.
   */
  @JsonProperty("OwnerNode")
  Optional<String> ownerNode();

  /**
   * A hint indicating which page of the destination's owner directory links to this object, in case the directory
   * consists of multiple pages. Omitted on escrows created before enabling the
   * <a href="https://xrpl.org/known-amendments.html#fix1523">fix1523 amendment</a>.
   *
   * @return An {@link Optional} of type {@link String} containing the destination node hint.
   */
  @JsonProperty("DestinationNode")
  Optional<String> destinationNode();

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
   * @return An {@link Optional} of type {@link LedgerIndex} representing the previous transaction ledger sequence.
   */
  @JsonProperty("PreviousTxnLgrSeq")
  Optional<LedgerIndex> previousTransactionLedgerSequence();

}
