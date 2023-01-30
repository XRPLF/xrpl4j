package org.xrpl.xrpl4j.model.client.fees;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
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

import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.math.BigDecimal;

/**
 * Unit tests for {@link ComputedNetworkFees}.
 */
class NetworkFeeResultTests {

  @Test
  void feeOptionsLow() {
    ComputedNetworkFees feeOptions = ComputedNetworkFees.builder()
      .feeLow(XrpCurrencyAmount.ofDrops(1L))
      .feeMedium(XrpCurrencyAmount.ofDrops(2L))
      .feeHigh(XrpCurrencyAmount.ofDrops(100L))
      .queuePercentage(BigDecimal.valueOf(-1))
      .build();

    assertThat(feeOptions.recommendedFee()).isEqualTo(XrpCurrencyAmount.ofDrops(1L));
    assertThat(feeOptions.isTranactionQueueFull()).isFalse();
    assertThat(feeOptions.isTransactionQueueEmpty()).isTrue();

    feeOptions = ComputedNetworkFees.builder()
      .feeLow(XrpCurrencyAmount.ofDrops(1L))
      .feeMedium(XrpCurrencyAmount.ofDrops(2L))
      .feeHigh(XrpCurrencyAmount.ofDrops(100L))
      .queuePercentage(BigDecimal.ZERO)
      .build();

    assertThat(feeOptions.recommendedFee()).isEqualTo(XrpCurrencyAmount.ofDrops(1L));
    assertThat(feeOptions.isTranactionQueueFull()).isFalse();
    assertThat(feeOptions.isTransactionQueueEmpty()).isTrue();
  }

  @Test
  void feeOptionsMedium() {
    ComputedNetworkFees feeOptions = ComputedNetworkFees.builder()
      .feeLow(XrpCurrencyAmount.ofDrops(1L))
      .feeMedium(XrpCurrencyAmount.ofDrops(2L))
      .feeHigh(XrpCurrencyAmount.ofDrops(100L))
      .queuePercentage(BigDecimal.valueOf(0.5))
      .build();

    assertThat(feeOptions.recommendedFee()).isEqualTo(XrpCurrencyAmount.ofDrops(2L));
    assertThat(feeOptions.isTranactionQueueFull()).isFalse();
    assertThat(feeOptions.isTransactionQueueEmpty()).isFalse();
  }

  @Test
  void isTranactionQueueHigh() {
    ComputedNetworkFees feeOptions = ComputedNetworkFees.builder()
      .feeLow(XrpCurrencyAmount.ofDrops(1L))
      .feeMedium(XrpCurrencyAmount.ofDrops(2L))
      .feeHigh(XrpCurrencyAmount.ofDrops(100L))
      .queuePercentage(BigDecimal.ONE)
      .build();

    assertThat(feeOptions.recommendedFee()).isEqualTo(XrpCurrencyAmount.ofDrops(100L));
    assertThat(feeOptions.isTranactionQueueFull()).isTrue();
    assertThat(feeOptions.isTransactionQueueEmpty()).isFalse();

    feeOptions = ComputedNetworkFees.builder()
      .feeLow(XrpCurrencyAmount.ofDrops(1L))
      .feeMedium(XrpCurrencyAmount.ofDrops(2L))
      .feeHigh(XrpCurrencyAmount.ofDrops(100L))
      .queuePercentage(BigDecimal.valueOf(1.1))
      .build();

    assertThat(feeOptions.recommendedFee()).isEqualTo(XrpCurrencyAmount.ofDrops(100L));
    assertThat(feeOptions.isTranactionQueueFull()).isTrue();
    assertThat(feeOptions.isTransactionQueueEmpty()).isFalse();
  }
}
