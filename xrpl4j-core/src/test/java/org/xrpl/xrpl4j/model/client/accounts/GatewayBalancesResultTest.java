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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.transactions.Address;

public class GatewayBalancesResultTest {

  @Test
  public void testDefaults() {
    GatewayBalancesResult result = GatewayBalancesResult
      .builder()
      .account(Address.of("rMwjYedjc7qqtKYVLiAccJSmCwih4LnE2q"))
      .assets(GatewayBalancesAssets.builder().build())
      .balances(GatewayBalancesHotWallets.builder().build())
      .obligations(GatewayBalancesObligations.builder().build())
      .build();

    assertThat(result.validated()).isFalse();
    assertThat(result.ledgerHash()).isEmpty();
    assertThat(result.ledgerIndex()).isEmpty();
    assertThat(result.ledgerCurrentIndex()).isEmpty();
    assertThatThrownBy(result::ledgerCurrentIndexSafe).isInstanceOf(IllegalStateException.class);
    assertThatThrownBy(result::ledgerHashSafe).isInstanceOf(IllegalStateException.class);
    assertThatThrownBy(result::ledgerIndexSafe).isInstanceOf(IllegalStateException.class);
  }
}
