package org.xrpl.xrpl4j.model.transactions;

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
import org.xrpl.xrpl4j.model.flags.TransactionFlags;

/**
 * A TicketCreate transaction sets aside one or more sequence numbers as Tickets.
 *
 * @see "https://xrpl.org/ticketcreate.html"
 */
@Value.Immutable
@JsonSerialize(as = ImmutableTicketCreate.class)
@JsonDeserialize(as = ImmutableTicketCreate.class)
public interface TicketCreate extends Transaction {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableTicketCreate.Builder}.
   */
  static ImmutableTicketCreate.Builder builder() {
    return ImmutableTicketCreate.builder();
  }

  /**
   * How many Tickets to create. This number of tickets cannot cause the account to own more than 250 Tickets after
   * executing this transaction.
   *
   * @return An {@link UnsignedInteger} denoting the number of Tickets to create.
   */
  @JsonProperty("TicketCount")
  UnsignedInteger ticketCount();

  /**
   * Set of {@link TransactionFlags}s for this {@link PaymentChannelFund}, which only allows
   * {@code tfFullyCanonicalSig} flag.
   *
   * <p>The value of the flags cannot be set manually, but exists for JSON serialization/deserialization only and for
   * proper signature computation in rippled.
   *
   * @return Always {@link TransactionFlags} with {@code tfFullyCanonicalSig} set.
   */
  @JsonProperty("Flags")
  @Value.Derived
  default TransactionFlags flags() {
    return new TransactionFlags.Builder().build();
  }
}
