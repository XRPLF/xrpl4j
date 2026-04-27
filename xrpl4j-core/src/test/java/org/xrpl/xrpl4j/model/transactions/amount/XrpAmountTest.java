package org.xrpl.xrpl4j.model.transactions.amount;

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

import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

/**
 * Unit tests for {@link XrpAmount} — factory methods, arithmetic, and conversion helpers.
 */
class XrpAmountTest {

  // -------------------------------------------------------------------------
  // ofDrops(long)
  // -------------------------------------------------------------------------

  @Test
  void ofDropsLongPositive() {
    XrpAmount amount = XrpAmount.ofDrops(1_000_000L);
    assertThat(amount.value()).isEqualTo("1000000");
    assertThat(amount.isNegative()).isFalse();
    assertThat(amount.unsignedLongValue()).isEqualTo(UnsignedLong.valueOf(1_000_000L));
  }

  @Test
  void ofDropsLongZero() {
    XrpAmount amount = XrpAmount.ofDrops(0L);
    assertThat(amount.value()).isEqualTo("0");
    assertThat(amount.isNegative()).isFalse();
    assertThat(amount.unsignedLongValue()).isEqualTo(UnsignedLong.ZERO);
  }

  @Test
  void ofDropsLongNegative() {
    XrpAmount amount = XrpAmount.ofDrops(-500L);
    assertThat(amount.value()).isEqualTo("-500");
    assertThat(amount.isNegative()).isTrue();
    assertThat(amount.unsignedLongValue()).isEqualTo(UnsignedLong.valueOf(500L));
  }

  // -------------------------------------------------------------------------
  // ofDrops(UnsignedLong)
  // -------------------------------------------------------------------------

  @Test
  void ofDropsUnsignedLong() {
    XrpAmount amount = XrpAmount.ofDrops(UnsignedLong.valueOf(1_000_000L));
    assertThat(amount.value()).isEqualTo("1000000");
    assertThat(amount.isNegative()).isFalse();
    assertThat(amount.unsignedLongValue()).isEqualTo(UnsignedLong.valueOf(1_000_000L));
  }

  @Test
  void ofDropsUnsignedLongNullThrows() {
    assertThatThrownBy(() -> XrpAmount.ofDrops((UnsignedLong) null))
      .isInstanceOf(NullPointerException.class);
  }

  // -------------------------------------------------------------------------
  // ofXrp(BigDecimal)
  // -------------------------------------------------------------------------

  @Test
  void ofXrpOneXrp() {
    XrpAmount amount = XrpAmount.ofXrp(new BigDecimal("1"));
    assertThat(amount.unsignedLongValue()).isEqualTo(UnsignedLong.valueOf(1_000_000L));
    assertThat(amount.isNegative()).isFalse();
  }

  @Test
  void ofXrpFractionalXrp() {
    // 1.5 XRP = 1,500,000 drops
    XrpAmount amount = XrpAmount.ofXrp(new BigDecimal("1.5"));
    assertThat(amount.unsignedLongValue()).isEqualTo(UnsignedLong.valueOf(1_500_000L));
    assertThat(amount.isNegative()).isFalse();
  }

  @Test
  void ofXrpOneDrop() {
    // 0.000001 XRP = 1 drop (minimum positive value)
    XrpAmount amount = XrpAmount.ofXrp(XrpAmount.MIN_XRP_BD);
    assertThat(amount.unsignedLongValue()).isEqualTo(UnsignedLong.ONE);
    assertThat(amount.isNegative()).isFalse();
  }

  @Test
  void ofXrpZero() {
    XrpAmount amount = XrpAmount.ofXrp(BigDecimal.ZERO);
    assertThat(amount.unsignedLongValue()).isEqualTo(UnsignedLong.ZERO);
    assertThat(amount.isNegative()).isFalse();
  }

  @Test
  void ofXrpMaxXrp() {
    XrpAmount amount = XrpAmount.ofXrp(XrpAmount.MAX_XRP_BD);
    assertThat(amount.unsignedLongValue()).isEqualTo(UnsignedLong.valueOf(XrpAmount.MAX_XRP_IN_DROPS));
    assertThat(amount.isNegative()).isFalse();
  }

  @Test
  void ofXrpBelowMinThrows() {
    // 0.0000001 XRP = 0.1 drops — sub-drop precision is illegal
    assertThatThrownBy(() -> XrpAmount.ofXrp(new BigDecimal("0.0000001")))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void ofXrpAboveMaxThrows() {
    assertThatThrownBy(() -> XrpAmount.ofXrp(XrpAmount.MAX_XRP_BD.add(BigDecimal.ONE)))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void ofXrpNullThrows() {
    assertThatThrownBy(() -> XrpAmount.ofXrp(null))
      .isInstanceOf(NullPointerException.class);
  }

  // -------------------------------------------------------------------------
  // toXrp()
  // -------------------------------------------------------------------------

  @Test
  void toXrpOneMillion() {
    assertThat(XrpAmount.ofDrops(1_000_000L).toXrp()).isEqualByComparingTo(BigDecimal.ONE);
  }

  @Test
  void toXrpZero() {
    assertThat(XrpAmount.ofDrops(0L).toXrp()).isEqualByComparingTo(BigDecimal.ZERO);
  }

  @Test
  void toXrpNegative() {
    BigDecimal result = XrpAmount.ofDrops(-1_500_000L).toXrp();
    assertThat(result).isEqualByComparingTo(new BigDecimal("-1.5"));
  }

  @Test
  void toXrpRoundTrip() {
    BigDecimal original = new BigDecimal("1.5");
    assertThat(XrpAmount.ofXrp(original).toXrp()).isEqualByComparingTo(original);
  }

  // -------------------------------------------------------------------------
  // plus()
  // -------------------------------------------------------------------------

  @Test
  void plusTwoPositive() {
    XrpAmount sum = XrpAmount.ofDrops(1_000_000L).plus(XrpAmount.ofDrops(500_000L));
    assertThat(sum.unsignedLongValue()).isEqualTo(UnsignedLong.valueOf(1_500_000L));
    assertThat(sum.isNegative()).isFalse();
  }

  @Test
  void plusPositiveAndNegative() {
    // 1,000,000 + (-300,000) = 700,000
    XrpAmount sum = XrpAmount.ofDrops(1_000_000L).plus(XrpAmount.ofDrops(-300_000L));
    assertThat(sum.unsignedLongValue()).isEqualTo(UnsignedLong.valueOf(700_000L));
    assertThat(sum.isNegative()).isFalse();
  }

  @Test
  void plusResultNegative() {
    // 300,000 + (-1,000,000) = -700,000
    XrpAmount sum = XrpAmount.ofDrops(300_000L).plus(XrpAmount.ofDrops(-1_000_000L));
    assertThat(sum.isNegative()).isTrue();
    assertThat(sum.unsignedLongValue()).isEqualTo(UnsignedLong.valueOf(700_000L));
  }

  @Test
  void plusNullThrows() {
    assertThatThrownBy(() -> XrpAmount.ofDrops(1L).plus(null))
      .isInstanceOf(NullPointerException.class);
  }

  // -------------------------------------------------------------------------
  // minus()
  // -------------------------------------------------------------------------

  @Test
  void minusLargerFromSmaller() {
    XrpAmount diff = XrpAmount.ofDrops(1_000_000L).minus(XrpAmount.ofDrops(400_000L));
    assertThat(diff.unsignedLongValue()).isEqualTo(UnsignedLong.valueOf(600_000L));
    assertThat(diff.isNegative()).isFalse();
  }

  @Test
  void minusSmallerFromLarger() {
    // 400,000 - 1,000,000 = -600,000
    XrpAmount diff = XrpAmount.ofDrops(400_000L).minus(XrpAmount.ofDrops(1_000_000L));
    assertThat(diff.isNegative()).isTrue();
    assertThat(diff.unsignedLongValue()).isEqualTo(UnsignedLong.valueOf(600_000L));
  }

  @Test
  void minusEqualAmounts() {
    XrpAmount diff = XrpAmount.ofDrops(1_000_000L).minus(XrpAmount.ofDrops(1_000_000L));
    assertThat(diff.unsignedLongValue()).isEqualTo(UnsignedLong.ZERO);
    assertThat(diff.isNegative()).isFalse();
  }

  @Test
  void minusNullThrows() {
    assertThatThrownBy(() -> XrpAmount.ofDrops(1L).minus(null))
      .isInstanceOf(NullPointerException.class);
  }

  // -------------------------------------------------------------------------
  // times()
  // -------------------------------------------------------------------------

  @Test
  void timesPositiveByPositive() {
    XrpAmount product = XrpAmount.ofDrops(1_000L).times(XrpAmount.ofDrops(3L));
    assertThat(product.unsignedLongValue()).isEqualTo(UnsignedLong.valueOf(3_000L));
    assertThat(product.isNegative()).isFalse();
  }

  @Test
  void timesPositiveByNegative() {
    XrpAmount product = XrpAmount.ofDrops(1_000L).times(XrpAmount.ofDrops(-3L));
    assertThat(product.unsignedLongValue()).isEqualTo(UnsignedLong.valueOf(3_000L));
    assertThat(product.isNegative()).isTrue();
  }

  @Test
  void timesNegativeByNegative() {
    // negative × negative = positive
    XrpAmount product = XrpAmount.ofDrops(-1_000L).times(XrpAmount.ofDrops(-3L));
    assertThat(product.unsignedLongValue()).isEqualTo(UnsignedLong.valueOf(3_000L));
    assertThat(product.isNegative()).isFalse();
  }

  @Test
  void timesByZero() {
    XrpAmount product = XrpAmount.ofDrops(1_000_000L).times(XrpAmount.ofDrops(0L));
    assertThat(product.unsignedLongValue()).isEqualTo(UnsignedLong.ZERO);
  }

  @Test
  void timesNullThrows() {
    assertThatThrownBy(() -> XrpAmount.ofDrops(1L).times(null))
      .isInstanceOf(NullPointerException.class);
  }
}
