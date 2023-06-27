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
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.ledger.ImmutableTicketObject;
import org.xrpl.xrpl4j.model.ledger.LedgerObject;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.TicketCreate;

import java.util.Optional;

/**
 * The {@link MetaTicketObject} type represents a Ticket, which tracks an account sequence number that has been set
 * aside for future use. You can create new tickets with a {@link TicketCreate} transaction.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableMetaTicketObject.class)
@JsonDeserialize(as = ImmutableMetaTicketObject.class)
public interface MetaTicketObject extends MetaLedgerObject {

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
  Optional<Address> account();

  /**
   * A hint indicating which page of the owner directory links to this object, in case the directory
   * consists of multiple pages.
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
   * The Sequence Number this Ticket sets aside.
   *
   * @return An {@link UnsignedInteger} denoting the sequence number.
   */
  @JsonProperty("TicketSequence")
  Optional<UnsignedInteger> ticketSequence();
}
