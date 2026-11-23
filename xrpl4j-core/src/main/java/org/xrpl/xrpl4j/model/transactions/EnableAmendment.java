package org.xrpl.xrpl4j.model.transactions;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
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
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.flags.EnableAmendmentFlags;

import java.util.Optional;

/**
 * An {@link EnableAmendment} pseudo-transaction marks a change in status of an amendment.
 * to the XRP Ledger protocol
 *
 * @see "https://xrpl.org/enableamendment.html"
 */
@Value.Immutable
@JsonSerialize(as = ImmutableEnableAmendment.class)
@JsonDeserialize(as = ImmutableEnableAmendment.class)
public interface EnableAmendment extends Transaction {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableEnableAmendment.Builder}.
   */
  static ImmutableEnableAmendment.Builder builder() {
    return ImmutableEnableAmendment.builder();
  }

  /**
   * A unique identifier for the amendment. This is not intended to be a human-readable name.
   *
   * @return A {@link Hash256} value indentifying an amendment.
   */
  @JsonProperty("Amendment")
  Hash256 amendment();

  /**
   * The ledger index where this pseudo-transaction appears. This distinguishes the
   * pseudo-transaction from other occurrences of the same change.
   *
   * @return A {@link LedgerIndex} to indicates where the tx appears.
   */
  @JsonProperty("LedgerSequence")
  Optional<LedgerIndex> ledgerSequence();

  /**
   * The {@link EnableAmendmentFlags} for this {@link EnableAmendment}, which identify <em>which</em> status transition
   * this pseudo-transaction records. xrpld emits an {@link EnableAmendment} at each of three transitions and
   * distinguishes them solely by {@code Flags}:
   * <ul>
   *   <li>{@link EnableAmendmentFlags#tfGotMajority()}: the amendment gained support from more than 80% of trusted
   *       validators, starting the two-week activation countdown;</li>
   *   <li>{@link EnableAmendmentFlags#tfLostMajority()}: support fell below 80%, cancelling the countdown;</li>
   *   <li>neither flag set (i.e. {@code Flags} is {@code 0} or absent): the countdown completed and the amendment is
   *       now enabled on the ledger. Use {@link EnableAmendmentFlags#isEnabled()} for this case rather than testing
   *       for empty flags.</li>
   * </ul>
   * Defaults to {@link EnableAmendmentFlags#empty()}; a value present in a deserialized transaction is preserved.
   *
   * <p>Every {@link Transaction} subtype declares {@code flags()} so that {@link Transaction#transactionFlags()} can
   * resolve it reflectively.</p>
   *
   * @return The {@link EnableAmendmentFlags} for this transaction.
   *
   * @see "https://xrpl.org/enableamendment.html#enableamendment-flags"
   */
  @JsonProperty("Flags")
  @Value.Default
  default EnableAmendmentFlags flags() {
    return EnableAmendmentFlags.empty();
  }
}
