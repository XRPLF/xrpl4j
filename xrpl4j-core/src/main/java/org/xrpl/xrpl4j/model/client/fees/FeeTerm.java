package org.xrpl.xrpl4j.model.client.fees;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
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

import org.immutables.value.Value;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.Optional;

/**
 * One additive term of a {@link FeeBreakdown}: a single reason a transaction owes part of its fee, priced either as a
 * number of base fees ({@link #feeUnits()}) or as a flat amount ({@link #flatAmount()}, used for owner reserve
 * charges, which do not scale with the base fee).
 *
 * <p>Every term carries a {@link Provenance} saying where its value came from, so that a developer inspecting a
 * breakdown can see at a glance which parts of the fee were {@link Provenance#ASSUMED} — and therefore which
 * {@link FeeParams} inputs they may still need to supply before the computed fee matches what the signed transaction
 * will actually owe.
 */
@Value.Immutable
public interface FeeTerm {

  /**
   * Construct a {@link FeeTerm} priced as a number of base fees.
   *
   * @param description A human-readable {@link String} explaining what this term charges for.
   * @param feeUnits    The number of base fees this term contributes.
   * @param provenance  Where the term's value came from.
   *
   * @return A {@link FeeTerm}.
   */
  static FeeTerm of(final String description, final long feeUnits, final Provenance provenance) {
    return ImmutableFeeTerm.builder()
      .description(description)
      .feeUnits(feeUnits)
      .provenance(provenance)
      .build();
  }

  /**
   * Construct a {@link FeeTerm} priced as a flat amount rather than a multiple of the base fee, as an owner reserve
   * charge is.
   *
   * @param description A human-readable {@link String} explaining what this term charges for.
   * @param flatAmount  The flat {@link XrpCurrencyAmount} this term contributes.
   * @param provenance  Where the term's value came from.
   *
   * @return A {@link FeeTerm}.
   */
  static FeeTerm flat(final String description, final XrpCurrencyAmount flatAmount, final Provenance provenance) {
    return ImmutableFeeTerm.builder()
      .description(description)
      .feeUnits(0L)
      .flatAmount(flatAmount)
      .provenance(provenance)
      .build();
  }

  /**
   * A human-readable explanation of what this term charges for. When the term is {@link Provenance#ASSUMED}, the
   * description also names the {@link FeeParams} input that would override the assumption.
   *
   * @return A {@link String}.
   */
  String description();

  /**
   * The number of base fees this term contributes. Zero when the term is flat-priced ({@link #flatAmount()} present),
   * and also for an "assumed" term that exists purely to surface an overridable assumption (e.g., that a transaction
   * is single-signed, which costs nothing extra).
   *
   * @return A number of base fees.
   */
  long feeUnits();

  /**
   * The flat amount this term contributes, present only for a charge that does not scale with the base fee — an owner
   * reserve increment for an {@code AccountDelete} or {@code AMMCreate}, standalone or as a Batch inner transaction.
   *
   * @return An optionally-present {@link XrpCurrencyAmount}.
   */
  Optional<XrpCurrencyAmount> flatAmount();

  /**
   * Where this term's value came from.
   *
   * @return A {@link Provenance}.
   */
  Provenance provenance();

  /**
   * Where a {@link FeeTerm}'s value came from, distinguishing the parts of a fee that are facts from the parts that
   * are defaults a caller can override.
   */
  enum Provenance {
    /**
     * The value was supplied explicitly on {@link FeeParams} by the caller.
     */
    SPECIFIED,
    /**
     * The value is a default that the caller did not override. An assumed term's {@link FeeTerm#description()} names
     * the {@link FeeParams} input to supply if the assumption is wrong — these are the terms to review before
     * trusting a computed fee.
     */
    ASSUMED,
    /**
     * The value was read or derived from the transaction itself (or from supplied ledger data) and is exact — a
     * per-type surcharge, a collected signature count, or a fulfillment's size.
     */
    DERIVED
  }
}
