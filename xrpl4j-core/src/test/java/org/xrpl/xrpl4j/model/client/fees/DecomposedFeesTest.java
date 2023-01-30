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
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.xrpl.xrpl4j.model.client.fees.FeeUtils.DecomposedFees.MAX_XRP_IN_DROPS_BIG_INT;
import static org.xrpl.xrpl4j.model.transactions.CurrencyAmount.MAX_XRP_IN_DROPS;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

/**
 * Unit tests for {@link FeeUtils.DecomposedFees}.
 */
public class DecomposedFeesTest {

  @Mock
  FeeDrops feeDropsMock;

  @Mock
  FeeResult feeResultMock;

  @BeforeEach
  void setup() {
    openMocks(this);

    when(feeResultMock.drops()).thenReturn(feeDropsMock);
    when(feeResultMock.currentQueueSize()).thenReturn(UnsignedInteger.ZERO);
    when(feeResultMock.maxQueueSize()).thenReturn(Optional.of(UnsignedInteger.ONE));

    when(feeDropsMock.minimumFee()).thenReturn(XrpCurrencyAmount.builder().value(UnsignedLong.ZERO).build());
    when(feeDropsMock.medianFee()).thenReturn(XrpCurrencyAmount.builder().value(UnsignedLong.ZERO).build());
    when(feeDropsMock.openLedgerFee()).thenReturn(XrpCurrencyAmount.builder().value(UnsignedLong.ZERO).build());
  }

  @Test
  void buildWithZeroFees() {
    when(feeDropsMock.minimumFee()).thenReturn(XrpCurrencyAmount.builder().value(UnsignedLong.ZERO).build());
    when(feeDropsMock.medianFee()).thenReturn(XrpCurrencyAmount.builder().value(UnsignedLong.ZERO).build());
    when(feeDropsMock.openLedgerFee()).thenReturn(XrpCurrencyAmount.builder().value(UnsignedLong.ZERO).build());

    FeeUtils.DecomposedFees decomposedFees = FeeUtils.DecomposedFees.builder(feeResultMock);

    assertThat(decomposedFees.adjustedMinimumFeeDrops()).isEqualTo(BigInteger.ZERO);
    assertThat(decomposedFees.adjustedMinimumFeeDropsAsBigDecimal()).isEqualTo(BigDecimal.ZERO);
    assertThat(decomposedFees.medianFeeDrops()).isEqualTo(BigInteger.ZERO);
    assertThat(decomposedFees.medianFeeDropsAsBigDecimal()).isEqualTo(BigDecimal.ZERO);
    assertThat(decomposedFees.openLedgerFeeDrops()).isEqualTo(BigInteger.ZERO);
    assertThat(decomposedFees.openLedgerFeeDropsAsBigDecimal()).isEqualTo(BigDecimal.ZERO);
  }

  @Test
  void buildWithOneFees() {
    when(feeDropsMock.minimumFee()).thenReturn(XrpCurrencyAmount.builder().value(UnsignedLong.ONE).build());
    when(feeDropsMock.medianFee()).thenReturn(XrpCurrencyAmount.builder().value(UnsignedLong.ONE).build());
    when(feeDropsMock.openLedgerFee()).thenReturn(XrpCurrencyAmount.builder().value(UnsignedLong.ONE).build());

    FeeUtils.DecomposedFees decomposedFees = FeeUtils.DecomposedFees.builder(feeResultMock);

    assertThat(decomposedFees.adjustedMinimumFeeDrops()).isEqualTo(BigInteger.ONE);
    assertThat(decomposedFees.adjustedMinimumFeeDropsAsBigDecimal()).isEqualTo(BigDecimal.ONE);
    assertThat(decomposedFees.medianFeeDrops()).isEqualTo(BigInteger.ONE);
    assertThat(decomposedFees.medianFeeDropsAsBigDecimal()).isEqualTo(BigDecimal.ONE);
    assertThat(decomposedFees.openLedgerFeeDrops()).isEqualTo(BigInteger.ONE);
    assertThat(decomposedFees.openLedgerFeeDropsAsBigDecimal()).isEqualTo(BigDecimal.ONE);
  }

  @Test
  void buildWithTenFees() {
    when(feeDropsMock.minimumFee()).thenReturn(XrpCurrencyAmount.builder().value(UnsignedLong.valueOf(10L)).build());
    when(feeDropsMock.medianFee()).thenReturn(XrpCurrencyAmount.builder().value(UnsignedLong.valueOf(10L)).build());
    when(feeDropsMock.openLedgerFee()).thenReturn(XrpCurrencyAmount.builder().value(UnsignedLong.valueOf(10L)).build());

    FeeUtils.DecomposedFees decomposedFees = FeeUtils.DecomposedFees.builder(feeResultMock);

    assertThat(decomposedFees.adjustedMinimumFeeDrops()).isEqualTo(BigInteger.valueOf(15L));
    assertThat(decomposedFees.adjustedMinimumFeeDropsAsBigDecimal()).isEqualTo(BigDecimal.valueOf(15L));
    assertThat(decomposedFees.medianFeeDrops()).isEqualTo(BigInteger.valueOf(10L));
    assertThat(decomposedFees.medianFeeDropsAsBigDecimal()).isEqualTo(BigDecimal.valueOf(10L));
    assertThat(decomposedFees.openLedgerFeeDrops()).isEqualTo(BigInteger.valueOf(10L));
    assertThat(decomposedFees.openLedgerFeeDropsAsBigDecimal()).isEqualTo(BigDecimal.valueOf(10L));
  }

  @Test
  void buildWithMaxFees() {
    when(feeResultMock.currentQueueSize()).thenReturn(UnsignedInteger.MAX_VALUE);
    when(feeResultMock.maxQueueSize()).thenReturn(Optional.of(UnsignedInteger.MAX_VALUE));

    final UnsignedLong maxFees = UnsignedLong.valueOf(MAX_XRP_IN_DROPS);
    when(feeDropsMock.minimumFee()).thenReturn(
      XrpCurrencyAmount.builder().value(maxFees).build());
    when(feeDropsMock.medianFee()).thenReturn(
      XrpCurrencyAmount.builder().value(maxFees).build());
    when(feeDropsMock.openLedgerFee()).thenReturn(
      XrpCurrencyAmount.builder().value(maxFees).build());

    FeeUtils.DecomposedFees decomposedFees = FeeUtils.DecomposedFees.builder(feeResultMock);

    assertThat(decomposedFees.adjustedMinimumFeeDrops()).isEqualTo(MAX_XRP_IN_DROPS_BIG_INT);
    assertThat(decomposedFees.adjustedMinimumFeeDropsAsBigDecimal())
      .isEqualTo(new BigDecimal(maxFees.bigIntegerValue()));
    assertThat(decomposedFees.medianFeeDrops()).isEqualTo(MAX_XRP_IN_DROPS_BIG_INT);
    assertThat(decomposedFees.medianFeeDropsAsBigDecimal())
      .isEqualTo(new BigDecimal(maxFees.bigIntegerValue()));
    assertThat(decomposedFees.openLedgerFeeDrops()).isEqualTo(MAX_XRP_IN_DROPS_BIG_INT);
    assertThat(decomposedFees.openLedgerFeeDropsAsBigDecimal())
      .isEqualTo(new BigDecimal(maxFees.bigIntegerValue()));
  }

  @Test
  void buildWithEmptyMaxQueueSize() {
    when(feeResultMock.currentQueueSize()).thenReturn(UnsignedInteger.ONE);
    when(feeResultMock.maxQueueSize()).thenReturn(Optional.empty());

    when(feeDropsMock.minimumFee()).thenReturn(XrpCurrencyAmount.builder().value(UnsignedLong.ONE).build());
    when(feeDropsMock.medianFee()).thenReturn(XrpCurrencyAmount.builder().value(UnsignedLong.ONE).build());
    when(feeDropsMock.openLedgerFee()).thenReturn(XrpCurrencyAmount.builder().value(UnsignedLong.ONE).build());

    FeeUtils.DecomposedFees decomposedFees = FeeUtils.DecomposedFees.builder(feeResultMock);

    assertThat(decomposedFees.adjustedMinimumFeeDrops()).isEqualTo(BigInteger.ONE);
    assertThat(decomposedFees.adjustedMinimumFeeDropsAsBigDecimal()).isEqualTo(BigDecimal.ONE);
    assertThat(decomposedFees.medianFeeDrops()).isEqualTo(BigInteger.ONE);
    assertThat(decomposedFees.medianFeeDropsAsBigDecimal()).isEqualTo(BigDecimal.ONE);
    assertThat(decomposedFees.openLedgerFeeDrops()).isEqualTo(BigInteger.ONE);
    assertThat(decomposedFees.openLedgerFeeDropsAsBigDecimal()).isEqualTo(BigDecimal.ONE);
  }

  @Test
  void buildWithNegativeQueuePercentage() {
    when(feeResultMock.currentQueueSize()).thenReturn(UnsignedInteger.ONE);
    when(feeResultMock.maxQueueSize()).thenReturn(Optional.empty());

    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      FeeUtils.DecomposedFees.builder(feeDropsMock, BigDecimal.valueOf(-1));
    });
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      FeeUtils.DecomposedFees.builder(feeDropsMock, BigDecimal.valueOf(1.1));
    });
  }

}
