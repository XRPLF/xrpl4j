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

import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

/**
 * Unit tests for {@link MptTokenAmount} — builders, accessors, and derived helpers.
 */
class MptTokenAmountTest {

  private static final String ISSUANCE_ID = "00000143A58DCB491FD36A15A7D3172E6A9F088A5478BA41";

  // -------------------------------------------------------------------------
  // builder() — raw builder with explicit MptAmount
  // -------------------------------------------------------------------------

  @Test
  void builderWithExplicitAmount() {
    MptTokenAmount amount = MptTokenAmount.builder()
      .amount(MptAmount.of(UnsignedLong.valueOf(1_000L)))
      .mptIssuanceId(MpTokenIssuanceId.of(ISSUANCE_ID))
      .build();

    assertThat(amount.amount().value()).isEqualTo("1000");
    assertThat(amount.mptIssuanceId()).isEqualTo(MpTokenIssuanceId.of(ISSUANCE_ID));
    assertThat(amount.isNegative()).isFalse();
    assertThat(amount.unsignedLongValue()).isEqualTo(UnsignedLong.valueOf(1_000L));
  }

  @Test
  void builderWithZeroAmount() {
    MptTokenAmount amount = MptTokenAmount.builder()
      .amount(MptAmount.of(UnsignedLong.ZERO))
      .mptIssuanceId(MpTokenIssuanceId.of(ISSUANCE_ID))
      .build();

    assertThat(amount.amount().value()).isEqualTo("0");
    assertThat(amount.isNegative()).isFalse();
    assertThat(amount.unsignedLongValue()).isEqualTo(UnsignedLong.ZERO);
  }

  @Test
  void builderWithNegativeAmount() {
    // Negative MPT values appear in transaction metadata
    MptTokenAmount amount = MptTokenAmount.builder()
      .amount(MptAmount.of(UnsignedLong.valueOf(500L), true))
      .mptIssuanceId(MpTokenIssuanceId.of(ISSUANCE_ID))
      .build();

    assertThat(amount.amount().value()).isEqualTo("-500");
    assertThat(amount.isNegative()).isTrue();
    assertThat(amount.unsignedLongValue()).isEqualTo(UnsignedLong.valueOf(500L));
  }

  // -------------------------------------------------------------------------
  // builder(UnsignedLong) — convenience factory
  // -------------------------------------------------------------------------

  @Test
  void convenienceBuilderSetsAmount() {
    MptTokenAmount amount = MptTokenAmount.builder(UnsignedLong.valueOf(1_000L))
      .mptIssuanceId(MpTokenIssuanceId.of(ISSUANCE_ID))
      .build();

    assertThat(amount.amount().value()).isEqualTo("1000");
    assertThat(amount.isNegative()).isFalse();
    assertThat(amount.unsignedLongValue()).isEqualTo(UnsignedLong.valueOf(1_000L));
  }

  @Test
  void convenienceBuilderWithZero() {
    MptTokenAmount amount = MptTokenAmount.builder(UnsignedLong.ZERO)
      .mptIssuanceId(MpTokenIssuanceId.of(ISSUANCE_ID))
      .build();

    assertThat(amount.amount().value()).isEqualTo("0");
    assertThat(amount.isNegative()).isFalse();
  }

  @Test
  void convenienceBuilderProducesSameResultAsExplicitBuilder() {
    MptTokenAmount fromConvenience = MptTokenAmount.builder(UnsignedLong.valueOf(1_000L))
      .mptIssuanceId(MpTokenIssuanceId.of(ISSUANCE_ID))
      .build();
    MptTokenAmount fromExplicit = MptTokenAmount.builder()
      .amount(MptAmount.of(UnsignedLong.valueOf(1_000L)))
      .mptIssuanceId(MpTokenIssuanceId.of(ISSUANCE_ID))
      .build();

    assertThat(fromConvenience).isEqualTo(fromExplicit);
  }

  // -------------------------------------------------------------------------
  // isNegative() — delegates to MptAmount
  // -------------------------------------------------------------------------

  @Test
  void isNegativeFalseForPositiveAmount() {
    MptTokenAmount amount = MptTokenAmount.builder(UnsignedLong.valueOf(1_000L))
      .mptIssuanceId(MpTokenIssuanceId.of(ISSUANCE_ID))
      .build();
    assertThat(amount.isNegative()).isFalse();
  }

  @Test
  void isNegativeTrueForNegativeAmount() {
    MptTokenAmount amount = MptTokenAmount.builder()
      .amount(MptAmount.of(UnsignedLong.valueOf(500L), true))
      .mptIssuanceId(MpTokenIssuanceId.of(ISSUANCE_ID))
      .build();
    assertThat(amount.isNegative()).isTrue();
  }

  // -------------------------------------------------------------------------
  // unsignedLongValue() — delegates to MptAmount, strips sign
  // -------------------------------------------------------------------------

  @Test
  void unsignedLongValueForPositive() {
    MptTokenAmount amount = MptTokenAmount.builder(UnsignedLong.valueOf(99_999L))
      .mptIssuanceId(MpTokenIssuanceId.of(ISSUANCE_ID))
      .build();
    assertThat(amount.unsignedLongValue()).isEqualTo(UnsignedLong.valueOf(99_999L));
  }

  @Test
  void unsignedLongValueStripsSignForNegative() {
    MptTokenAmount amount = MptTokenAmount.builder()
      .amount(MptAmount.of(UnsignedLong.valueOf(500L), true))
      .mptIssuanceId(MpTokenIssuanceId.of(ISSUANCE_ID))
      .build();
    assertThat(amount.unsignedLongValue()).isEqualTo(UnsignedLong.valueOf(500L));
  }

  // -------------------------------------------------------------------------
  // mptIssuanceId() accessor
  // -------------------------------------------------------------------------

  @Test
  void mptIssuanceIdAccessor() {
    MpTokenIssuanceId id = MpTokenIssuanceId.of(ISSUANCE_ID);
    MptTokenAmount amount = MptTokenAmount.builder(UnsignedLong.ONE)
      .mptIssuanceId(id)
      .build();
    assertThat(amount.mptIssuanceId()).isEqualTo(id);
  }

  // -------------------------------------------------------------------------
  // equality
  // -------------------------------------------------------------------------

  @Test
  void equalAmountsAreEqual() {
    MptTokenAmount a = MptTokenAmount.builder(UnsignedLong.valueOf(1_000L))
      .mptIssuanceId(MpTokenIssuanceId.of(ISSUANCE_ID))
      .build();
    MptTokenAmount b = MptTokenAmount.builder(UnsignedLong.valueOf(1_000L))
      .mptIssuanceId(MpTokenIssuanceId.of(ISSUANCE_ID))
      .build();
    assertThat(a).isEqualTo(b);
  }

  @Test
  void differentAmountsAreNotEqual() {
    MptTokenAmount a = MptTokenAmount.builder(UnsignedLong.valueOf(1_000L))
      .mptIssuanceId(MpTokenIssuanceId.of(ISSUANCE_ID))
      .build();
    MptTokenAmount b = MptTokenAmount.builder(UnsignedLong.valueOf(2_000L))
      .mptIssuanceId(MpTokenIssuanceId.of(ISSUANCE_ID))
      .build();
    assertThat(a).isNotEqualTo(b);
  }

  @Test
  void positiveAndNegativeAreNotEqual() {
    MptTokenAmount positive = MptTokenAmount.builder(UnsignedLong.valueOf(500L))
      .mptIssuanceId(MpTokenIssuanceId.of(ISSUANCE_ID))
      .build();
    MptTokenAmount negative = MptTokenAmount.builder()
      .amount(MptAmount.of(UnsignedLong.valueOf(500L), true))
      .mptIssuanceId(MpTokenIssuanceId.of(ISSUANCE_ID))
      .build();
    assertThat(positive).isNotEqualTo(negative);
  }

  // -------------------------------------------------------------------------
  // implements TokenAmount
  // -------------------------------------------------------------------------

  @Test
  void isInstanceOfTokenAmount() {
    MptTokenAmount amount = MptTokenAmount.builder(UnsignedLong.ONE)
      .mptIssuanceId(MpTokenIssuanceId.of(ISSUANCE_ID))
      .build();
    assertThat(amount).isInstanceOf(TokenAmount.class);
  }
}
