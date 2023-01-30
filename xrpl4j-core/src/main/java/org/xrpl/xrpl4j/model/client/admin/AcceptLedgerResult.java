package org.xrpl.xrpl4j.model.client.admin;

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
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;

/**
 * Result from a "advance_ledger" rippled admin API call.
 */
@Immutable
@JsonSerialize(as = ImmutableAcceptLedgerResult.class)
@JsonDeserialize(as = ImmutableAcceptLedgerResult.class)
public interface AcceptLedgerResult extends XrplResult {

  /**
   * Construct a builder for this class.
   *
   * @return An {@link ImmutableAcceptLedgerResult.Builder}.
   */
  static ImmutableAcceptLedgerResult.Builder builder() {
    return ImmutableAcceptLedgerResult.builder();
  }

  /**
   * The Ledger Index of the current open ledger these stats describe.
   *
   * @return A {@link LedgerIndex} denoting the current ledger index.
   */
  @JsonProperty("ledger_current_index")
  LedgerIndex ledgerCurrentIndex();

}
