package org.xrpl.xrpl4j.model.ledger;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: model
 * %%
 * Copyright (C) 2020 - 2026 XRPL Foundation and its contributors
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
 * Represents an MPT asset on the ledger, identified by its MPT issuance ID.
 *
 * <p>This class will be marked {@link Beta} until the SingleAssetVault amendment is enabled on mainnet. Its API is
 * subject to change.</p>
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
   * The MPT issuance ID of this asset.
   *
   * @return A {@link MpTokenIssuanceId}.
   */
  @JsonProperty("mpt_issuance_id")
  MpTokenIssuanceId mptIssuanceId();

}
