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

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * An itemized account of how a transaction's fee was computed by {@link FeeUtils#computeFee(FeeParams)}: one
 * {@link FeeTerm} per reason the transaction owes something, each tagged with where its value came from.
 *
 * <p>The intended use is iterative: compute a fee, look at (or log) {@link #summary()}, and check whether any
 * {@link FeeTerm.Provenance#ASSUMED} line describes an assumption that is wrong for the transaction at hand — e.g., a
 * Batch participant assumed to sign with a single key when it will actually multi-sign. Each assumed line names the
 * {@link FeeParams} input that overrides it. When every line is {@code specified} or {@code derived}, the fee is
 * exact.
 *
 * <p>The breakdown is the source of truth for the computation itself: the fee levels on the returned
 * {@link ComputedNetworkFees} are {@link #totalFeeUnits()} base fees plus {@link #totalFlatAmount()}.
 */
@Value.Immutable
public interface FeeBreakdown {

  /**
   * Construct a {@link FeeBreakdown} from a list of terms.
   *
   * @param terms The {@link FeeTerm}s of the breakdown, in display order.
   *
   * @return A {@link FeeBreakdown}.
   */
  static FeeBreakdown of(final List<FeeTerm> terms) {
    return ImmutableFeeBreakdown.builder().terms(terms).build();
  }

  /**
   * Construct a {@link FeeBreakdown} from one or more terms.
   *
   * @param terms The {@link FeeTerm}s of the breakdown, in display order.
   *
   * @return A {@link FeeBreakdown}.
   */
  static FeeBreakdown of(final FeeTerm... terms) {
    return of(Arrays.asList(terms));
  }

  /**
   * The additive terms of the fee, in a stable, human-meaningful order: the transaction's own terms first, then
   * per-participant and per-inner-transaction terms.
   *
   * @return A {@link List} of {@link FeeTerm}.
   */
  List<FeeTerm> terms();

  /**
   * The total number of base fees the transaction owes: the sum of every term's {@link FeeTerm#feeUnits()}. Zero for
   * a transaction priced entirely flat (an {@code AccountDelete} or {@code AMMCreate}).
   *
   * @return A number of base fees.
   */
  @Value.Derived
  default long totalFeeUnits() {
    return terms().stream().mapToLong(FeeTerm::feeUnits).sum();
  }

  /**
   * The total flat amount the transaction owes on top of (or, for an owner-reserve type, instead of) its base-fee
   * multiple: the sum of every term's {@link FeeTerm#flatAmount()}.
   *
   * @return An optionally-present {@link XrpCurrencyAmount}, empty when no term is flat-priced.
   */
  @Value.Derived
  default Optional<XrpCurrencyAmount> totalFlatAmount() {
    return terms().stream()
      .map(FeeTerm::flatAmount)
      .filter(Optional::isPresent)
      .map(Optional::get)
      .reduce(XrpCurrencyAmount::plus);
  }

  /**
   * Renders the breakdown as a multi-line, human-readable table, suitable for logging while developing or for
   * explaining a fee to a user. Review the {@code [assumed]} lines: each one names the {@link FeeParams} input to
   * supply if its assumption is wrong.
   *
   * @return A multi-line {@link String}.
   */
  default String summary() {
    final StringBuilder stringBuilder = new StringBuilder();
    for (final FeeTerm term : terms()) {
      final String amount = term.flatAmount()
        .map(flat -> "flat " + flat)
        .orElseGet(() -> term.feeUnits() + " x base");
      stringBuilder
        .append(String.format("%-16s %-11s %s%n",
          amount,
          "[" + term.provenance().name().toLowerCase(Locale.ENGLISH) + "]",
          term.description()
        ));
    }
    stringBuilder.append("total: ").append(totalFeeUnits()).append(" x base fee");
    totalFlatAmount().ifPresent(flat -> stringBuilder.append(" + ").append(flat).append(" flat"));
    return stringBuilder.toString();
  }
}
