package org.xrpl.xrpl4j.model.ledger;

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
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.TicketCreate;

/**
 * The {@link TicketObject} type represents a Ticket, which tracks an account sequence number that has been set
 * aside for future use. You can create new tickets with a {@link TicketCreate} transaction.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableTicketObject.class)
@JsonDeserialize(as = ImmutableTicketObject.class)
public interface TicketObject extends LedgerObject {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableTicketObject.Builder}.
   */
  static ImmutableTicketObject.Builder builder() {
    return ImmutableTicketObject.builder();
  }

  /**
   * The type of ledger object. In this case, always "SignerList".
   *
   * @return Always {@link LedgerEntryType#SIGNER_LIST}.
   */
  @JsonProperty("LedgerEntryType")
  @Value.Derived
  default LedgerEntryType ledgerEntryType() {
    return LedgerEntryType.TICKET;
  }

  /**
   * A bit-map of Boolean flags enabled for this Ticket. Currently, there are no flags defined for Tickets.
   *
   * @return Always returns {@link Flags#UNSET}.
   */
  @JsonProperty("Flags")
  @Value.Derived
  default Flags flags() {
    return Flags.UNSET;
  }

  /**
   * The account that owns this Ticket.
   *
   * @return The account that owns this Ticket, as an {@link Address}.
   */
  @JsonProperty("Account")
  Address account();

  /**
   * A hint indicating which page of the owner directory links to this object, in case the directory
   * consists of multiple pages.
   *
   * @return A {@link String} containing the hint.
   */
  @JsonProperty("OwnerNode")
  String ownerNode();

  /**
   * The identifying hash of the transaction that most recently modified this object.
   *
   * @return A {@link Hash256} containing the previous transaction hash.
   */
  @JsonProperty("PreviousTxnID")
  Hash256 previousTransactionId();

  /**
   * The index of the ledger that contains the transaction that most recently modified this object.
   *
   * @return An {@link UnsignedInteger} representing the previous transaction ledger sequence.
   */
  @JsonProperty("PreviousTxnLgrSeq")
  UnsignedInteger previousTransactionLedgerSequence();

  /**
   * The Sequence Number this Ticket sets aside.
   *
   * @return An {@link UnsignedInteger} denoting the sequence number.
   */
  @JsonProperty("TicketSequence")
  UnsignedInteger ticketSequence();
}
