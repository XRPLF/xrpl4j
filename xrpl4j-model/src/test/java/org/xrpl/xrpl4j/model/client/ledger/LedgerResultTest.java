package org.xrpl.xrpl4j.model.client.ledger;

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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.client.accounts.AccountLinesResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.ledger.LedgerHeader;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;

class LedgerResultTest {

  @Test
  void testWithHash() {
    LedgerResult result = LedgerResult.builder()
      .status("success")
      .ledgerHash(Hash256.of("3652D7FD0576BC452C0D2E9B747BDD733075971D1A9A1D98125055DEF428721A"))
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(54300940)))
      .validated(true)
      .ledger(mock(LedgerHeader.class))
      .build();

    assertThat(result.ledgerHash()).isNotEmpty().get().isEqualTo(result.ledgerHashSafe());
  }

  @Test
  void testWithoutHash() {
    LedgerResult result = LedgerResult.builder()
      .status("success")
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(54300940)))
      .validated(true)
      .ledger(mock(LedgerHeader.class))
      .build();

    assertThat(result.ledgerHash()).isEmpty();
    assertThrows(
      IllegalStateException.class,
      result::ledgerHashSafe
    );
  }

  @Test
  void testWithLedgerIndex() {
    LedgerResult result = LedgerResult.builder()
      .status("success")
      .ledgerHash(Hash256.of("3652D7FD0576BC452C0D2E9B747BDD733075971D1A9A1D98125055DEF428721A"))
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(54300940)))
      .validated(true)
      .ledger(mock(LedgerHeader.class))
      .build();

    assertThat(result.ledgerIndex()).isNotEmpty().get().isEqualTo(result.ledgerIndexSafe());
    assertThat(result.ledgerCurrentIndex()).isEmpty();
    assertThrows(
      IllegalStateException.class,
      result::ledgerCurrentIndexSafe
    );
  }

  @Test
  void testWithLedgerCurrentIndex() {
    LedgerResult result = LedgerResult.builder()
      .status("success")
      .ledgerHash(Hash256.of("3652D7FD0576BC452C0D2E9B747BDD733075971D1A9A1D98125055DEF428721A"))
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.valueOf(54300940)))
      .validated(true)
      .ledger(mock(LedgerHeader.class))
      .build();

    assertThat(result.ledgerIndex()).isEmpty();
    assertThat(result.ledgerCurrentIndex()).isNotEmpty().get().isEqualTo(result.ledgerCurrentIndexSafe());
    assertThrows(
      IllegalStateException.class,
      result::ledgerIndexSafe
    );
  }

}
