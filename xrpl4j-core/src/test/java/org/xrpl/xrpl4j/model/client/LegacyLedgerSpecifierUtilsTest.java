package org.xrpl.xrpl4j.model.client;

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

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Strings;
import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.util.Optional;

@Deprecated // Remove once LegacyLedgerSpecifierUtils is removed
class LegacyLedgerSpecifierUtilsTest {

  static final Hash256 LEDGER_HASH = Hash256.of(Strings.repeat("0", 64));

  @Test
  void computeLedgerSpecifierWithHash() {
    LedgerSpecifier ledgerSpecifier = LegacyLedgerSpecifierUtils.computeLedgerSpecifier(
      Optional.of(LEDGER_HASH),
      LedgerIndex.VALIDATED
    );

    assertThat(ledgerSpecifier.ledgerHash()).isNotEmpty().get().isEqualTo(LEDGER_HASH);
  }

  @Test
  void computeLedgerSpecifierWithNumericalIndex() {
    LedgerSpecifier ledgerSpecifier = LegacyLedgerSpecifierUtils.computeLedgerSpecifier(
      Optional.empty(),
      LedgerIndex.of(UnsignedInteger.ONE)
    );

    assertThat(ledgerSpecifier.ledgerIndex()).isNotEmpty().get().isEqualTo(LedgerIndex.of(UnsignedInteger.ONE));
  }

  @Test
  void computeLedgerSpecifierWithShortcut() {
    LedgerSpecifier ledgerSpecifier = LegacyLedgerSpecifierUtils.computeLedgerSpecifier(
      Optional.empty(),
      LedgerIndex.VALIDATED
    );

    assertThat(ledgerSpecifier.ledgerIndexShortcut()).isNotEmpty();
    assertThat(ledgerSpecifier).isEqualTo(LedgerSpecifier.VALIDATED);

    ledgerSpecifier = LegacyLedgerSpecifierUtils.computeLedgerSpecifier(
      Optional.empty(),
      LedgerIndex.CURRENT
    );

    assertThat(ledgerSpecifier.ledgerIndexShortcut()).isNotEmpty();
    assertThat(ledgerSpecifier).isEqualTo(LedgerSpecifier.CURRENT);

    ledgerSpecifier = LegacyLedgerSpecifierUtils.computeLedgerSpecifier(
      Optional.empty(),
      LedgerIndex.CLOSED
    );

    assertThat(ledgerSpecifier.ledgerIndexShortcut()).isNotEmpty();
    assertThat(ledgerSpecifier).isEqualTo(LedgerSpecifier.CLOSED);
  }

  @Test
  void computeDefaultLedgerSpecifier() {
    LedgerSpecifier ledgerSpecifier = LegacyLedgerSpecifierUtils.computeLedgerSpecifier(
      Optional.empty(),
      null
    );

    assertThat(ledgerSpecifier.ledgerIndexShortcut()).isNotEmpty();
    assertThat(ledgerSpecifier).isEqualTo(LedgerSpecifier.CURRENT);
  }
}
