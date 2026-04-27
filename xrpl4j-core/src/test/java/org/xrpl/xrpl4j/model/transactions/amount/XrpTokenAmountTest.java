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

/**
 * Unit tests for {@link XrpTokenAmount} — factory methods and the {@link XrpTokenAmount#amount()} accessor.
 */
class XrpTokenAmountTest {

  // -------------------------------------------------------------------------
  // ofDrops(long)
  // -------------------------------------------------------------------------

  @Test
  void ofDropsLongPositive() {
    XrpTokenAmount amount = XrpTokenAmount.ofDrops(1_000_000L);
    assertThat(amount.amount().value()).isEqualTo("1000000");
    assertThat(amount.amount().isNegative()).isFalse();
    assertThat(amount.amount().unsignedLongValue()).isEqualTo(UnsignedLong.valueOf(1_000_000L));
  }

  @Test
  void ofDropsLongZero() {
    XrpTokenAmount amount = XrpTokenAmount.ofDrops(0L);
    assertThat(amount.amount().value()).isEqualTo("0");
    assertThat(amount.amount().isNegative()).isFalse();
  }

  @Test
  void ofDropsLongNegative() {
    // Negative values appear in transaction metadata
    XrpTokenAmount amount = XrpTokenAmount.ofDrops(-500L);
    assertThat(amount.amount().value()).isEqualTo("-500");
    assertThat(amount.amount().isNegative()).isTrue();
    assertThat(amount.amount().unsignedLongValue()).isEqualTo(UnsignedLong.valueOf(500L));
  }

  // -------------------------------------------------------------------------
  // ofDrops(UnsignedLong)
  // -------------------------------------------------------------------------

  @Test
  void ofDropsUnsignedLong() {
    XrpTokenAmount amount = XrpTokenAmount.ofDrops(UnsignedLong.valueOf(1_000_000L));
    assertThat(amount.amount().value()).isEqualTo("1000000");
    assertThat(amount.amount().isNegative()).isFalse();
  }

  @Test
  void ofDropsUnsignedLongNullThrows() {
    assertThatThrownBy(() -> XrpTokenAmount.ofDrops((UnsignedLong) null))
      .isInstanceOf(NullPointerException.class);
  }

  // -------------------------------------------------------------------------
  // of(XrpAmount) — wrap factory
  // -------------------------------------------------------------------------

  @Test
  void ofXrpAmountWrapsCorrectly() {
    XrpAmount xrpAmount = XrpAmount.ofDrops(42_000L);
    XrpTokenAmount wrapped = XrpTokenAmount.of(xrpAmount);
    assertThat(wrapped.amount()).isEqualTo(xrpAmount);
    assertThat(wrapped.amount().value()).isEqualTo("42000");
  }

  @Test
  void ofXrpAmountWrapsNegative() {
    XrpAmount xrpAmount = XrpAmount.ofDrops(-300L);
    XrpTokenAmount wrapped = XrpTokenAmount.of(xrpAmount);
    assertThat(wrapped.amount()).isEqualTo(xrpAmount);
    assertThat(wrapped.amount().isNegative()).isTrue();
  }

  @Test
  void ofXrpAmountNullThrows() {
    assertThatThrownBy(() -> XrpTokenAmount.of((XrpAmount) null))
      .isInstanceOf(NullPointerException.class);
  }

  // -------------------------------------------------------------------------
  // equality
  // -------------------------------------------------------------------------

  @Test
  void equalAmountsAreEqual() {
    assertThat(XrpTokenAmount.ofDrops(1_000_000L))
      .isEqualTo(XrpTokenAmount.ofDrops(1_000_000L));
  }

  @Test
  void differentDropCountsAreNotEqual() {
    assertThat(XrpTokenAmount.ofDrops(1_000_000L))
      .isNotEqualTo(XrpTokenAmount.ofDrops(2_000_000L));
  }

  // -------------------------------------------------------------------------
  // implements TokenAmount
  // -------------------------------------------------------------------------

  @Test
  void isInstanceOfTokenAmount() {
    assertThat(XrpTokenAmount.ofDrops(1L)).isInstanceOf(TokenAmount.class);
  }
}
