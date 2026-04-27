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
 * Unit tests for {@link MptAmount} — factory methods and accessor helpers.
 */
class MptAmountTest {

  // -------------------------------------------------------------------------
  // of(UnsignedLong) — non-negative factory
  // -------------------------------------------------------------------------

  @Test
  void ofPositive() {
    MptAmount amount = MptAmount.of(UnsignedLong.valueOf(1_000L));
    assertThat(amount.value()).isEqualTo("1000");
    assertThat(amount.isNegative()).isFalse();
    assertThat(amount.unsignedLongValue()).isEqualTo(UnsignedLong.valueOf(1_000L));
  }

  @Test
  void ofZero() {
    MptAmount amount = MptAmount.of(UnsignedLong.ZERO);
    assertThat(amount.value()).isEqualTo("0");
    assertThat(amount.isNegative()).isFalse();
    assertThat(amount.unsignedLongValue()).isEqualTo(UnsignedLong.ZERO);
  }

  @Test
  void ofMaxLong() {
    UnsignedLong maxLong = UnsignedLong.valueOf(Long.MAX_VALUE);
    MptAmount amount = MptAmount.of(maxLong);
    assertThat(amount.value()).isEqualTo(maxLong.toString());
    assertThat(amount.isNegative()).isFalse();
    assertThat(amount.unsignedLongValue()).isEqualTo(maxLong);
  }

  @Test
  void ofNullThrows() {
    assertThatThrownBy(() -> MptAmount.of(null))
      .isInstanceOf(NullPointerException.class);
  }

  // -------------------------------------------------------------------------
  // of(UnsignedLong, boolean) — signed factory (e.g. from metadata)
  // -------------------------------------------------------------------------

  @Test
  void ofWithSignFalse() {
    MptAmount amount = MptAmount.of(UnsignedLong.valueOf(500L), false);
    assertThat(amount.value()).isEqualTo("500");
    assertThat(amount.isNegative()).isFalse();
    assertThat(amount.unsignedLongValue()).isEqualTo(UnsignedLong.valueOf(500L));
  }

  @Test
  void ofWithSignTrue() {
    MptAmount amount = MptAmount.of(UnsignedLong.valueOf(500L), true);
    assertThat(amount.value()).isEqualTo("-500");
    assertThat(amount.isNegative()).isTrue();
    assertThat(amount.unsignedLongValue()).isEqualTo(UnsignedLong.valueOf(500L));
  }

  @Test
  void ofNegativeZero() {
    // isNegative=true with magnitude 0 — unusual but must not throw
    MptAmount amount = MptAmount.of(UnsignedLong.ZERO, true);
    assertThat(amount.value()).isEqualTo("-0");
    assertThat(amount.isNegative()).isTrue();
  }

  @Test
  void ofSignedNullThrows() {
    assertThatThrownBy(() -> MptAmount.of(null, true))
      .isInstanceOf(NullPointerException.class);
  }

  // -------------------------------------------------------------------------
  // isNegative()
  // -------------------------------------------------------------------------

  @Test
  void isNegativeFalseForPositive() {
    assertThat(MptAmount.of(UnsignedLong.ONE).isNegative()).isFalse();
  }

  @Test
  void isNegativeTrueForNegative() {
    assertThat(MptAmount.of(UnsignedLong.ONE, true).isNegative()).isTrue();
  }

  // -------------------------------------------------------------------------
  // unsignedLongValue() — magnitude extraction
  // -------------------------------------------------------------------------

  @Test
  void unsignedLongValueStripsSign() {
    MptAmount amount = MptAmount.of(UnsignedLong.valueOf(12345L), true);
    assertThat(amount.unsignedLongValue()).isEqualTo(UnsignedLong.valueOf(12345L));
  }

  @Test
  void unsignedLongValuePositivePassthrough() {
    MptAmount amount = MptAmount.of(UnsignedLong.valueOf(12345L));
    assertThat(amount.unsignedLongValue()).isEqualTo(UnsignedLong.valueOf(12345L));
  }

  // -------------------------------------------------------------------------
  // equality
  // -------------------------------------------------------------------------

  @Test
  void equalAmountsAreEqual() {
    assertThat(MptAmount.of(UnsignedLong.valueOf(1_000L)))
      .isEqualTo(MptAmount.of(UnsignedLong.valueOf(1_000L)));
  }

  @Test
  void differentMagnitudesAreNotEqual() {
    assertThat(MptAmount.of(UnsignedLong.valueOf(1_000L)))
      .isNotEqualTo(MptAmount.of(UnsignedLong.valueOf(2_000L)));
  }

  @Test
  void positiveAndNegativeAreNotEqual() {
    assertThat(MptAmount.of(UnsignedLong.valueOf(1_000L)))
      .isNotEqualTo(MptAmount.of(UnsignedLong.valueOf(1_000L), true));
  }
}
