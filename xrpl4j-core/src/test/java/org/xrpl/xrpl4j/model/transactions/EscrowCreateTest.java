package org.xrpl.xrpl4j.model.transactions;

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
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import com.ripple.cryptoconditions.Condition;
import com.ripple.cryptoconditions.CryptoConditionReader;
import com.ripple.cryptoconditions.der.DerEncodingException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link EscrowCreate}.
 */
public class EscrowCreateTest {

  public static final String GOOD_CONDITION_STR =
    "A0258020E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855810100";

  private static Condition condition;

  @BeforeAll
  static void beforeAll() {
    try {
      condition = CryptoConditionReader.readCondition(BaseEncoding.base16().decode(GOOD_CONDITION_STR));
    } catch (DerEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testWithNeitherCancelNorFinish() {
    EscrowCreate.builder()
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .amount(XrpCurrencyAmount.ofDrops(1))
      .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .build();
  }

  @Test
  public void testCancelBeforeFinish() {
    assertThrows(
      IllegalStateException.class,
      () -> EscrowCreate.builder()
        .sequence(UnsignedInteger.ONE)
        .fee(XrpCurrencyAmount.ofDrops(1))
        .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
        .amount(XrpCurrencyAmount.ofDrops(1))
        .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .cancelAfter(UnsignedLong.ONE)
        .finishAfter(UnsignedLong.valueOf(2L))
        .build(),
      "If both CancelAfter and FinishAfter are specified, the FinishAfter time must be before the CancelAfter time."
    );
  }

  @Test
  public void testCancelAfterFinish() {
    EscrowCreate.builder()
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .amount(XrpCurrencyAmount.ofDrops(1))
      .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .cancelAfter(UnsignedLong.valueOf(2L))
      .finishAfter(UnsignedLong.ONE)
      .build();
  }

  @Test
  public void testCancelEqualsFinish() {
    assertThrows(
      IllegalStateException.class,
      () -> EscrowCreate.builder()
        .sequence(UnsignedInteger.ONE)
        .fee(XrpCurrencyAmount.ofDrops(1))
        .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
        .amount(XrpCurrencyAmount.ofDrops(1))
        .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .cancelAfter(UnsignedLong.ONE)
        .finishAfter(UnsignedLong.ONE)
        .build(),
      "The DepositPreAuth transaction must include either Authorize or Unauthorize, but not both."
    );
  }

  ////////////////////////////////
  // normalizeCondition tests
  ////////////////////////////////

  @Test
  void normalizeWithNoConditionNoRawValue() {
    EscrowCreate create = EscrowCreate.builder()
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .amount(XrpCurrencyAmount.ofDrops(1))
      .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .build();

    assertThat(create.condition()).isEmpty();
    assertThat(create.conditionRawValue()).isEmpty();
  }

  @Test
  void normalizeWithConditionAndRawValueMatching() {
    EscrowCreate create = EscrowCreate.builder()
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .amount(XrpCurrencyAmount.ofDrops(1))
      .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .condition(condition)
      .conditionRawValue(GOOD_CONDITION_STR)
      .build();

    assertThat(create.condition()).isNotEmpty().get().isEqualTo(condition);
    assertThat(create.conditionRawValue()).isNotEmpty().get().isEqualTo(GOOD_CONDITION_STR);
  }

  @Test
  void normalizeWithConditionAndRawValueNonMatching() {
    assertThatThrownBy(() -> EscrowCreate.builder()
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .amount(XrpCurrencyAmount.ofDrops(1))
      .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .condition(condition)
      // This is slightly different than GOOD_CONDITION_STR
      .conditionRawValue("A0258020E3B0C44298FC1C149ABCD4C8996FB92427AE41E4649B934CA495991B7852B855810100")
      .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessage("condition and conditionRawValue should be equivalent if both are present.");
  }

  @Test
  void normalizeWithConditionPresentAndNoRawValue() {
    EscrowCreate create = EscrowCreate.builder()
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .amount(XrpCurrencyAmount.ofDrops(1))
      .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .condition(condition)
      .build();

    assertThat(create.conditionRawValue()).isNotEmpty().get().isEqualTo(GOOD_CONDITION_STR);
  }

  @Test
  void normalizeWithNoConditionAndRawValueForValidCondition() {
    EscrowCreate create = EscrowCreate.builder()
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .amount(XrpCurrencyAmount.ofDrops(1))
      .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .conditionRawValue(GOOD_CONDITION_STR)
      .build();

    assertThat(create.condition()).isNotEmpty().get().isEqualTo(condition);
  }

  @Test
  void normalizeWithNoConditionAndRawValueForMalformedCondition() {
    EscrowCreate create = EscrowCreate.builder()
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .amount(XrpCurrencyAmount.ofDrops(1))
      .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .conditionRawValue("1234")
      .build();

    assertThat(create.condition()).isEmpty();
    assertThat(create.conditionRawValue()).isNotEmpty().get().isEqualTo("1234");
  }

  @Test
  void normalizeWithNoConditionAndRawValueForBadHexLengthCondition() {
    EscrowCreate create = EscrowCreate.builder()
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .amount(XrpCurrencyAmount.ofDrops(1))
      .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .conditionRawValue("123")
      .build();

    assertThat(create.condition()).isEmpty();
    assertThat(create.conditionRawValue()).isNotEmpty().get().isEqualTo("123");
  }

}
