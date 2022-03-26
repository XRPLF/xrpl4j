package org.xrpl.xrpl4j.model.client.accounts;

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
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;

class AccountCurrenciesResultTest {

  @Test
  void testWithHash() {
    AccountCurrenciesResult result = AccountCurrenciesResult.builder()
      .ledgerHash(Hash256.of("B9D3D80EDF4083A06B2D51202E0BFB63C46FC0985E015D06767C21A62853BF6D"))
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(37230600)))
      .status("success")
      .validated(true)
      .addSendCurrencies("USD")
      .addReceiveCurrencies("EUR")
      .addReceiveCurrencies("USD")
      .build();

    assertThat(result.ledgerHash()).isNotEmpty().get().isEqualTo(result.ledgerHashSafe());
  }

  @Test
  void testWithoutHash() {
    AccountCurrenciesResult result = AccountCurrenciesResult.builder()
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(37230600)))
      .status("success")
      .validated(true)
      .addSendCurrencies("USD")
      .addReceiveCurrencies("EUR")
      .addReceiveCurrencies("USD")
      .build();

    assertThat(result.ledgerHash()).isEmpty();
    assertThrows(
      IllegalStateException.class,
      result::ledgerHashSafe
    );
  }

  @Test
  void testWithLedgerIndex() {
    AccountCurrenciesResult result = AccountCurrenciesResult.builder()
      .ledgerHash(Hash256.of("B9D3D80EDF4083A06B2D51202E0BFB63C46FC0985E015D06767C21A62853BF6D"))
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(37230600)))
      .status("success")
      .validated(true)
      .addSendCurrencies("USD")
      .addReceiveCurrencies("EUR")
      .addReceiveCurrencies("USD")
      .build();

    assertThat(result.ledgerIndex()).isNotNull();
    assertThat(result.ledgerIndexSafe()).isEqualTo(result.ledgerIndex());
    assertThat(result.ledgerCurrentIndex()).isEmpty();
    assertThrows(
      IllegalStateException.class,
      result::ledgerCurrentIndexSafe
    );
  }

  @Test
  void testWithLedgerCurrentIndex() {
    AccountCurrenciesResult result = AccountCurrenciesResult.builder()
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.valueOf(66467750)))
      .status("success")
      .validated(false)
      .receiveCurrencies(Lists.newArrayList("BTC", "CNY", "015841551A748AD2C1F76FF6ECB0CCCD00000000"))
      .sendCurrencies(Lists.newArrayList("ASP", "BTC", "USD"))
      .build();

    assertThat(result.ledgerIndex()).isNull();
    assertThat(result.ledgerCurrentIndex()).isNotEmpty().get().isEqualTo(result.ledgerCurrentIndexSafe());
    assertThrows(
      IllegalStateException.class,
      result::ledgerIndexSafe
    );
  }

}
