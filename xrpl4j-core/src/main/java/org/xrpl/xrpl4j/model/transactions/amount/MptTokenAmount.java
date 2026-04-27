package org.xrpl.xrpl4j.model.transactions.amount;

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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value.Auxiliary;
import org.immutables.value.Value.Derived;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.jackson.modules.MptTokenAmountDeserializer;
import org.xrpl.xrpl4j.model.jackson.modules.MptTokenAmountSerializer;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;
import org.xrpl.xrpl4j.model.transactions.MptCurrencyAmount;

/**
 * A {@link TokenAmount} for Multi-Purpose Tokens (MPTs) on the XRP Ledger.
 *
 * <p>Construct via the builder or the convenience factory:
 * <pre>{@code
 *   MptTokenAmount amount = MptTokenAmount.builder()
 *     .amount(MptAmount.of(UnsignedLong.valueOf(1000L)))
 *     .mptIssuanceId(MpTokenIssuanceId.of("..."))
 *     .build();
 *
 *   // Convenience factory pre-populates amount from an UnsignedLong:
 *   MptTokenAmount amount = MptTokenAmount.builder(UnsignedLong.valueOf(1000L))
 *     .mptIssuanceId(MpTokenIssuanceId.of("..."))
 *     .build();
 * }</pre>
 *
 * <p>MPT amounts are non-negative integers on the ledger (max 2^63 − 1), but the RPC may return a
 * leading {@code -} when reporting changes in transaction metadata.</p>
 *
 * <p>On the wire this serializes as a JSON object with {@code value} and {@code mpt_issuance_id}
 * fields — the same format as {@link MptCurrencyAmount}.</p>
 */
@Immutable
@JsonSerialize(using = MptTokenAmountSerializer.class)
@JsonDeserialize(using = MptTokenAmountDeserializer.class)
public interface MptTokenAmount extends TokenAmount {

  /**
   * Construct a {@code MptTokenAmount} builder.
   *
   * @return An {@link ImmutableMptTokenAmount.Builder}.
   */
  static ImmutableMptTokenAmount.Builder builder() {
    return ImmutableMptTokenAmount.builder();
  }

  /**
   * Construct a {@code MptTokenAmount} builder pre-populated with a non-negative amount.
   *
   * @param value The MPT quantity. Must not be null.
   *
   * @return An {@link ImmutableMptTokenAmount.Builder}.
   */
  static ImmutableMptTokenAmount.Builder builder(final UnsignedLong value) {
    return ImmutableMptTokenAmount.builder()
      .amount(MptAmount.of(value));
  }

  /**
   * The scalar numeric value of this MPT amount.
   *
   * @return An {@link MptAmount} holding the integer string.
   */
  @JsonIgnore
  MptAmount amount();

  /**
   * The MPT issuance ID that identifies which MPT this amount is for.
   *
   * @return A {@link MpTokenIssuanceId}.
   */
  @JsonProperty("mpt_issuance_id")
  MpTokenIssuanceId mptIssuanceId();

  /**
   * The absolute magnitude of this MPT amount as an {@link UnsignedLong} (sign stripped).
   *
   * @return An {@link UnsignedLong}.
   */
  @Auxiliary
  @JsonIgnore
  default UnsignedLong unsignedLongValue() {
    return amount().unsignedLongValue();
  }

  /**
   * Indicates whether this amount is positive or negative.
   *
   * @return {@code true} if this amount is negative; {@code false} otherwise.
   */
  @Derived
  @JsonIgnore
  @Auxiliary
  default boolean isNegative() {
    return amount().isNegative();
  }
}
