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

import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.transactions.Address;

/**
 * Unit tests for {@link IouTokenAmount} — builder, accessors, and boundary constants.
 */
class IouTokenAmountTest {

  private static final String ISSUER = "rJbVo4xrsGN8o3vLKGXe1s1uW8mAMYHamV";

  // -------------------------------------------------------------------------
  // builder + accessors
  // -------------------------------------------------------------------------

  @Test
  void builderSetsAllFields() {
    IouTokenAmount amount = IouTokenAmount.builder()
      .amount(IouAmount.of("100.50"))
      .currency("USD")
      .issuer(Address.of(ISSUER))
      .build();

    assertThat(amount.amount().value()).isEqualTo("100.50");
    assertThat(amount.currency()).isEqualTo("USD");
    assertThat(amount.issuer()).isEqualTo(Address.of(ISSUER));
  }

  @Test
  void builderWithNegativeAmount() {
    // Negative IOU values appear in transaction metadata
    IouTokenAmount amount = IouTokenAmount.builder()
      .amount(IouAmount.of("-50.5"))
      .currency("EUR")
      .issuer(Address.of(ISSUER))
      .build();

    assertThat(amount.amount().isNegative()).isTrue();
    assertThat(amount.amount().value()).isEqualTo("-50.5");
  }

  @Test
  void builderWithScientificNotation() {
    IouTokenAmount amount = IouTokenAmount.builder()
      .amount(IouAmount.of("1.23e10"))
      .currency("USD")
      .issuer(Address.of(ISSUER))
      .build();

    assertThat(amount.amount().value()).isEqualTo("1.23e10");
  }

  @Test
  void builderWithHexCurrencyCode() {
    // Non-standard 40-character hex currency codes are preserved verbatim
    String hexCurrency = "7872706C346A436F696E00000000000000000000";
    IouTokenAmount amount = IouTokenAmount.builder()
      .amount(IouAmount.of("15"))
      .currency(hexCurrency)
      .issuer(Address.of(ISSUER))
      .build();

    assertThat(amount.currency()).isEqualTo(hexCurrency);
  }

  // -------------------------------------------------------------------------
  // boundary constants
  // -------------------------------------------------------------------------

  @Test
  void maxValueConstantIsParseable() {
    // Verify the constant is a valid numeric string
    IouTokenAmount amount = IouTokenAmount.builder()
      .amount(IouAmount.of(IouTokenAmount.MAX_VALUE))
      .currency("USD")
      .issuer(Address.of(ISSUER))
      .build();
    assertThat(amount.amount().value()).isEqualTo(IouTokenAmount.MAX_VALUE);
    assertThat(amount.amount().isNegative()).isFalse();
  }

  @Test
  void minValueConstantIsParseable() {
    IouTokenAmount amount = IouTokenAmount.builder()
      .amount(IouAmount.of(IouTokenAmount.MIN_VALUE))
      .currency("USD")
      .issuer(Address.of(ISSUER))
      .build();
    assertThat(amount.amount().isNegative()).isTrue();
  }

  @Test
  void minPositiveValueConstantIsParseable() {
    IouTokenAmount amount = IouTokenAmount.builder()
      .amount(IouAmount.of(IouTokenAmount.MIN_POSITIVE_VALUE))
      .currency("USD")
      .issuer(Address.of(ISSUER))
      .build();
    assertThat(amount.amount().isNegative()).isFalse();
  }

  @Test
  void maxNegativeValueConstantIsParseable() {
    IouTokenAmount amount = IouTokenAmount.builder()
      .amount(IouAmount.of(IouTokenAmount.MAX_NEGATIVE_VALUE))
      .currency("USD")
      .issuer(Address.of(ISSUER))
      .build();
    assertThat(amount.amount().isNegative()).isTrue();
  }

  // -------------------------------------------------------------------------
  // equality
  // -------------------------------------------------------------------------

  @Test
  void equalAmountsAreEqual() {
    IouTokenAmount a = IouTokenAmount.builder()
      .amount(IouAmount.of("100.50"))
      .currency("USD")
      .issuer(Address.of(ISSUER))
      .build();
    IouTokenAmount b = IouTokenAmount.builder()
      .amount(IouAmount.of("100.50"))
      .currency("USD")
      .issuer(Address.of(ISSUER))
      .build();
    assertThat(a).isEqualTo(b);
  }

  @Test
  void differentCurrenciesAreNotEqual() {
    IouTokenAmount a = IouTokenAmount.builder()
      .amount(IouAmount.of("100.50"))
      .currency("USD")
      .issuer(Address.of(ISSUER))
      .build();
    IouTokenAmount b = IouTokenAmount.builder()
      .amount(IouAmount.of("100.50"))
      .currency("EUR")
      .issuer(Address.of(ISSUER))
      .build();
    assertThat(a).isNotEqualTo(b);
  }

  // -------------------------------------------------------------------------
  // implements TokenAmount
  // -------------------------------------------------------------------------

  @Test
  void isInstanceOfTokenAmount() {
    IouTokenAmount amount = IouTokenAmount.builder()
      .amount(IouAmount.of("1"))
      .currency("USD")
      .issuer(Address.of(ISSUER))
      .build();
    assertThat(amount).isInstanceOf(TokenAmount.class);
  }
}
