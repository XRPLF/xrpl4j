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
import com.google.common.annotations.Beta;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

/**
 * Represents an MPToken asset on the ledger without an amount.
 *
 * <p>This is one of the two implementations of {@link Issue}. An {@link MptIssue} identifies
 * an MPToken by its issuance ID, as opposed to {@link CurrencyIssue} which identifies assets
 * by currency code and issuer.</p>
 *
 * @see Issue
 * @see CurrencyIssue
 */
@Value.Immutable
@JsonSerialize(as = ImmutableMptIssue.class)
@JsonDeserialize(as = ImmutableMptIssue.class)
@Beta
public interface MptIssue extends Issue {

  /**
   * Construct a {@code MptIssue} builder.
   *
   * @return An {@link ImmutableMptIssue.Builder}.
   */
  static ImmutableMptIssue.Builder builder() {
    return ImmutableMptIssue.builder();
  }

  /**
   * Construct a {@code MptIssue} from an {@link MpTokenIssuanceId}.
   *
   * @param mptIssuanceId The MPToken issuance ID.
   * @return An {@link MptIssue}.
   */
  static MptIssue of(MpTokenIssuanceId mptIssuanceId) {
    return builder().mptIssuanceId(mptIssuanceId).build();
  }

  /**
   * The MPToken issuance ID that uniquely identifies this MPToken.
   *
   * <p>The issuance ID is a 192-bit (48 character hex) identifier that combines
   * the issuer's account ID and the sequence number of the MPTokenIssuanceCreate transaction.</p>
   *
   * @return An {@link MpTokenIssuanceId}.
   */
  @JsonProperty("mpt_issuance_id")
  MpTokenIssuanceId mptIssuanceId();
}

