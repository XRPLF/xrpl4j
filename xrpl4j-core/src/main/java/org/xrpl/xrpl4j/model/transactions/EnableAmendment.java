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

import java.util.Optional;

/**
 * An {@link EnableAmendment} pseudo-transaction marks a change in status of an amendment. to the XRP Ledger protocol
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
   * The ledger index where this pseudo-transaction appears. This distinguishes the pseudo-transaction from other
   * occurrences of the same change.
   *
   * @return A {@link LedgerIndex} to indicates where the tx appears.
   */
  @JsonProperty("LedgerSequence")
  Optional<LedgerIndex> ledgerSequence();

  @JsonProperty(value = "TransactionType")
  @Value.Derived
  default TransactionType transactionType() {
    return TransactionType.ENABLE_AMENDMENT;
  }
}
