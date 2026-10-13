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

/**
 * Unit tests for {@link FeeTerm}.
 */
public class FeeTermTest {

  @Test
  void ofConstructsABaseFeeTermWithNoFlatAmount() {
    FeeTerm term = FeeTerm.of("a base-fee term", 3L, FeeTerm.Provenance.SPECIFIED);

    assertThat(term.description()).isEqualTo("a base-fee term");
    assertThat(term.feeUnits()).isEqualTo(3L);
    assertThat(term.provenance()).isEqualTo(FeeTerm.Provenance.SPECIFIED);
    assertThat(term.flatAmount()).isEmpty();
  }

  @Test
  void flatConstructsAZeroFeeUnitTermWithTheGivenFlatAmount() {
    XrpCurrencyAmount flatAmount = XrpCurrencyAmount.ofDrops(200000);
    FeeTerm term = FeeTerm.flat("an owner reserve term", flatAmount, FeeTerm.Provenance.DERIVED);

    assertThat(term.description()).isEqualTo("an owner reserve term");
    assertThat(term.feeUnits()).isEqualTo(0L);
    assertThat(term.provenance()).isEqualTo(FeeTerm.Provenance.DERIVED);
    assertThat(term.flatAmount()).contains(flatAmount);
  }
}
