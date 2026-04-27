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

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

/**
 * Unit tests for {@link IouAmount} — factory methods and accessor helpers.
 */
class IouAmountTest {

  // -------------------------------------------------------------------------
  // of(String)
  // -------------------------------------------------------------------------

  @Test
  void ofStringDecimal() {
    IouAmount amount = IouAmount.of("100.50");
    assertThat(amount.value()).isEqualTo("100.50");
    assertThat(amount.isNegative()).isFalse();
  }

  @Test
  void ofStringNegative() {
    IouAmount amount = IouAmount.of("-50.5");
    assertThat(amount.value()).isEqualTo("-50.5");
    assertThat(amount.isNegative()).isTrue();
  }

  @Test
  void ofStringScientificNotation() {
    // Scientific notation is preserved verbatim
    IouAmount amount = IouAmount.of("1.23e10");
    assertThat(amount.value()).isEqualTo("1.23e10");
  }

  @Test
  void ofStringZero() {
    IouAmount amount = IouAmount.of("0");
    assertThat(amount.value()).isEqualTo("0");
    assertThat(amount.isNegative()).isFalse();
  }

  @Test
  void ofStringInvalidThrows() {
    assertThatThrownBy(() -> IouAmount.of("not-a-number"))
      .isInstanceOf(NumberFormatException.class);
  }

  @Test
  void ofStringNullThrows() {
    assertThatThrownBy(() -> IouAmount.of((String) null))
      .isInstanceOf(NullPointerException.class);
  }

  // -------------------------------------------------------------------------
  // of(BigDecimal)
  // -------------------------------------------------------------------------

  @Test
  void ofBigDecimalPositive() {
    IouAmount amount = IouAmount.of(new BigDecimal("100.50"));
    // BigDecimal.toPlainString() does not use scientific notation
    assertThat(amount.value()).isEqualTo("100.50");
    assertThat(amount.isNegative()).isFalse();
  }

  @Test
  void ofBigDecimalNegative() {
    IouAmount amount = IouAmount.of(new BigDecimal("-50.5"));
    assertThat(amount.value()).isEqualTo("-50.5");
    assertThat(amount.isNegative()).isTrue();
  }

  @Test
  void ofBigDecimalAvoidsSciNotation() {
    // 1.23e10 as BigDecimal → toPlainString() = "12300000000"
    IouAmount amount = IouAmount.of(new BigDecimal("1.23e10"));
    assertThat(amount.value()).isEqualTo("12300000000");
  }

  @Test
  void ofBigDecimalNullThrows() {
    assertThatThrownBy(() -> IouAmount.of((BigDecimal) null))
      .isInstanceOf(NullPointerException.class);
  }

  // -------------------------------------------------------------------------
  // bigDecimalValue()
  // -------------------------------------------------------------------------

  @Test
  void bigDecimalValueFromPlainString() {
    IouAmount amount = IouAmount.of("100.50");
    assertThat(amount.bigDecimalValue()).isEqualByComparingTo(new BigDecimal("100.50"));
  }

  @Test
  void bigDecimalValueFromScientificNotation() {
    IouAmount amount = IouAmount.of("1.23e10");
    assertThat(amount.bigDecimalValue()).isEqualByComparingTo(new BigDecimal("12300000000"));
  }

  @Test
  void bigDecimalValueNegative() {
    IouAmount amount = IouAmount.of("-50.5");
    assertThat(amount.bigDecimalValue()).isEqualByComparingTo(new BigDecimal("-50.5"));
  }
}
