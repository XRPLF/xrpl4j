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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value.Immutable;
import org.xrpl.xrpl4j.model.jackson.modules.XrpTokenAmountDeserializer;
import org.xrpl.xrpl4j.model.jackson.modules.XrpTokenAmountSerializer;

import java.util.Objects;

/**
 * A {@link TokenAmount} representing XRP, denominated in drops.
 *
 * <p>Construct via the typed static factories rather than the raw builder:
 * <pre>{@code
 *   XrpTokenAmount amount = XrpTokenAmount.ofDrops(1_000_000L);   // 1 XRP
 *   XrpTokenAmount amount = XrpTokenAmount.ofDrops(UnsignedLong.valueOf(1_000_000L));
 * }</pre>
 *
 * <p>The underlying {@link XrpAmount} stores the drop count as a decimal-integer string, optionally
 * prefixed with {@code -} if the amount is negative (which can occur in metadata returned by the RPC).
 *
 * <p>On the wire, XRP amounts serialize as a bare JSON string containing the drop count (e.g.
 * {@code "1000000"}), not as a JSON object.
 */
@Immutable
@JsonSerialize(using = XrpTokenAmountSerializer.class)
@JsonDeserialize(using = XrpTokenAmountDeserializer.class)
public interface XrpTokenAmount extends TokenAmount {

  /**
   * Construct an {@link XrpTokenAmount} from a (possibly negative) number of drops.
   *
   * @param drops The number of drops. May be negative (e.g. when sourced from transaction metadata).
   *
   * @return An {@link XrpTokenAmount}.
   */
  static XrpTokenAmount ofDrops(final long drops) {
    return ImmutableXrpTokenAmount.builder()
      .amount(XrpAmount.ofDrops(drops))
      .build();
  }

  /**
   * Construct a non-negative {@link XrpTokenAmount} from an {@link UnsignedLong} drop count.
   *
   * @param drops The number of drops. Must not be null.
   *
   * @return An {@link XrpTokenAmount}.
   */
  static XrpTokenAmount ofDrops(final UnsignedLong drops) {
    Objects.requireNonNull(drops, "drops must not be null");
    return ImmutableXrpTokenAmount.builder()
      .amount(XrpAmount.ofDrops(drops))
      .build();
  }

  /**
   * Construct an {@link XrpTokenAmount} that wraps an existing {@link XrpAmount}.
   *
   * <p>This is the preferred factory when converting from a deserialized {@link XrpAmount}.
   *
   * @param amount An {@link XrpAmount} to wrap. Must not be null.
   *
   * @return An {@link XrpTokenAmount}.
   */
  static XrpTokenAmount of(final XrpAmount amount) {
    Objects.requireNonNull(amount, "amount must not be null");
    return ImmutableXrpTokenAmount.builder()
      .amount(amount)
      .build();
  }

  /**
   * The scalar numeric value of this XRP amount.
   *
   * @return An {@link XrpAmount} holding the drop count.
   */
  @JsonIgnore
  XrpAmount amount();
}
