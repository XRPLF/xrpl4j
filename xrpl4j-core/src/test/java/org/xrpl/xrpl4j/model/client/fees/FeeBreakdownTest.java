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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.Collections;

/**
 * Unit tests for {@link FeeBreakdown}.
 */
public class FeeBreakdownTest {

  @Test
  void ofListAndOfVarargsProduceTheSameTerms() {
    FeeTerm term1 = FeeTerm.of("term one", 1L, FeeTerm.Provenance.SPECIFIED);
    FeeTerm term2 = FeeTerm.of("term two", 2L, FeeTerm.Provenance.DERIVED);

    FeeBreakdown fromList = FeeBreakdown.of(Collections.singletonList(term1));
    FeeBreakdown fromVarargs = FeeBreakdown.of(term1, term2);

    assertThat(fromList.terms()).containsExactly(term1);
    assertThat(fromVarargs.terms()).containsExactly(term1, term2);
  }

  @Test
  void totalFeeUnitsSumsEveryTermsFeeUnits() {
    FeeBreakdown breakdown = FeeBreakdown.of(
      FeeTerm.of("a", 3L, FeeTerm.Provenance.SPECIFIED),
      FeeTerm.of("b", 4L, FeeTerm.Provenance.DERIVED)
    );

    assertThat(breakdown.totalFeeUnits()).isEqualTo(7L);
  }

  @Test
  void totalFeeUnitsIsZeroWhenEveryTermIsFlat() {
    FeeBreakdown breakdown = FeeBreakdown.of(
      FeeTerm.flat("flat term", XrpCurrencyAmount.ofDrops(1000), FeeTerm.Provenance.DERIVED)
    );

    assertThat(breakdown.totalFeeUnits()).isEqualTo(0L);
  }

  @Test
  void totalFlatAmountIsEmptyWhenNoTermIsFlat() {
    FeeBreakdown breakdown = FeeBreakdown.of(FeeTerm.of("a", 1L, FeeTerm.Provenance.SPECIFIED));

    assertThat(breakdown.totalFlatAmount()).isEmpty();
  }

  @Test
  void totalFlatAmountSumsAcrossMultipleFlatTerms() {
    FeeBreakdown breakdown = FeeBreakdown.of(
      FeeTerm.of("base", 1L, FeeTerm.Provenance.DERIVED),
      FeeTerm.flat("first owner reserve", XrpCurrencyAmount.ofDrops(200000), FeeTerm.Provenance.DERIVED),
      FeeTerm.flat("second owner reserve", XrpCurrencyAmount.ofDrops(300000), FeeTerm.Provenance.DERIVED)
    );

    assertThat(breakdown.totalFlatAmount()).contains(XrpCurrencyAmount.ofDrops(500000));
  }

  @Test
  void summaryRendersAFlatTermAndTheFlatTotal() {
    FeeBreakdown breakdown = FeeBreakdown.of(
      FeeTerm.of("base fee", 1L, FeeTerm.Provenance.DERIVED),
      FeeTerm.flat("owner reserve", XrpCurrencyAmount.ofDrops(200000), FeeTerm.Provenance.DERIVED)
    );

    String summary = breakdown.summary();

    assertThat(summary).contains("flat " + XrpCurrencyAmount.ofDrops(200000));
    assertThat(summary).contains("total: 1 x base fee + " + XrpCurrencyAmount.ofDrops(200000) + " flat");
  }
}
