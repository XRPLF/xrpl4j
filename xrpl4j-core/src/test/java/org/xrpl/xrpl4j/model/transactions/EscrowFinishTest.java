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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.cryptoconditions.Condition;
import com.ripple.cryptoconditions.CryptoConditionReader;
import com.ripple.cryptoconditions.CryptoConditionWriter;
import com.ripple.cryptoconditions.Fulfillment;
import com.ripple.cryptoconditions.PrefixSha256Fulfillment;
import com.ripple.cryptoconditions.PreimageSha256Condition;
import com.ripple.cryptoconditions.PreimageSha256Fulfillment;
import com.ripple.cryptoconditions.der.DerEncodingException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import org.xrpl.xrpl4j.model.transactions.ImmutableEscrowFinish.Builder;

/**
 * Unit tests for {@link EscrowFinish}.
 */
public class EscrowFinishTest {

  public static final String GOOD_CONDITION_STR =
    "A0258020E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855810100";

  public static final String GOOD_FULFILLMENT_STR =
    "A0028000";

  private static Condition condition;
  private static Fulfillment<?> fulfillment;

  @BeforeAll
  static void beforeAll() {
    try {
      condition = CryptoConditionReader.readCondition(BaseEncoding.base16().decode(GOOD_CONDITION_STR));
      fulfillment = CryptoConditionReader.readFulfillment(BaseEncoding.base16().decode(GOOD_FULFILLMENT_STR));
    } catch (DerEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void constructWithNoFulfillmentNoCondition() {
    EscrowFinish actual = EscrowFinish.builder()
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .sequence(UnsignedInteger.ONE)
      .owner(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .offerSequence(UnsignedInteger.ZERO)
      .build();

    assertThat(actual.condition()).isNotPresent();
    assertThat(actual.conditionRawValue()).isNotPresent();
    assertThat(actual.fulfillment()).isNotPresent();
    assertThat(actual.fulfillmentRawValue()).isNotPresent();
    assertThat(actual.fee()).isEqualTo(XrpCurrencyAmount.ofDrops(1));
    assertThat(actual.account()).isEqualTo(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"));
    assertThat(actual.sequence()).isEqualTo(UnsignedInteger.ONE);
    assertThat(actual.owner()).isEqualTo(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"));
    assertThat(actual.offerSequence()).isEqualTo(UnsignedInteger.ZERO);
  }

  ////////////////////////////////
  // normalizeCondition tests

  /// /////////////////////////////

  @Test
  void normalizeWithNoConditionNoRawValue() {
    EscrowFinish actual = EscrowFinish.builder()
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .sequence(UnsignedInteger.ONE)
      .owner(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .offerSequence(UnsignedInteger.ZERO)
      .build();

    assertThat(actual.condition()).isEmpty();
    assertThat(actual.conditionRawValue()).isEmpty();
  }

  @Test
  void normalizeWithConditionAndRawValueMatching() {
    EscrowFinish actual = EscrowFinish.builder()
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .sequence(UnsignedInteger.ONE)
      .owner(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .offerSequence(UnsignedInteger.ZERO)
      .condition(condition)
      .conditionRawValue(GOOD_CONDITION_STR)
      .build();

    assertThat(actual.condition()).isNotEmpty().get().isEqualTo(condition);
    assertThat(actual.conditionRawValue()).isNotEmpty().get().isEqualTo(GOOD_CONDITION_STR);
  }

  @Test
  void normalizeWithConditionAndRawValueNonMatching() {
    assertThatThrownBy(() -> EscrowFinish.builder()
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .sequence(UnsignedInteger.ONE)
      .owner(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .offerSequence(UnsignedInteger.ZERO)
      .condition(condition)
      // This is slightly different than GOOD_CONDITION_STR
      .conditionRawValue("A0258020E3B0C44298FC1C149ABCD4C8996FB92427AE41E4649B934CA495991B7852B855810100")
      .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessage("condition and conditionRawValue should be equivalent if both are present.");
  }

  @Test
  void normalizeWithConditionPresentAndNoRawValue() {
    EscrowFinish actual = EscrowFinish.builder()
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .sequence(UnsignedInteger.ONE)
      .owner(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .offerSequence(UnsignedInteger.ZERO)
      .condition(condition)
      .build();

    assertThat(actual.conditionRawValue()).isNotEmpty().get().isEqualTo(GOOD_CONDITION_STR);
  }

  @Test
  void normalizeWithNoConditionAndRawValueForValidCondition() {
    EscrowFinish actual = EscrowFinish.builder()
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .sequence(UnsignedInteger.ONE)
      .owner(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .offerSequence(UnsignedInteger.ZERO)
      .conditionRawValue(GOOD_CONDITION_STR)
      .build();

    assertThat(actual.condition()).isNotEmpty().get().isEqualTo(condition);
  }

  @Test
  void normalizeWithNoConditionAndRawValueForMalformedCondition() {
    EscrowFinish actual = EscrowFinish.builder()
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .sequence(UnsignedInteger.ONE)
      .owner(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .offerSequence(UnsignedInteger.ZERO)
      .conditionRawValue("1234")
      .build();

    assertThat(actual.condition()).isEmpty();
    assertThat(actual.conditionRawValue()).isNotEmpty().get().isEqualTo("1234");
  }

  /**
   * This tests the case where conditionRawValue is present and is parseable to a Condition but when the parsed
   * Condition is written to a byte array, the value differs from the conditionRawValue bytes. This can occur if the
   * condition raw value contains a valid condition in the first 32 bytes, but also includes extra bytes afterward.
   */
  @Test
  void normalizeConditionWithRawValueThatIsParseableButNotValid() {
    EscrowFinish actual = EscrowFinish.builder()
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .sequence(UnsignedInteger.ONE)
      .owner(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .offerSequence(UnsignedInteger.ZERO)
      .conditionRawValue(GOOD_CONDITION_STR + GOOD_CONDITION_STR)
      .build();

    assertThat(actual.condition()).isEmpty();
    assertThat(actual.conditionRawValue()).isNotEmpty().get().isEqualTo(
      GOOD_CONDITION_STR + GOOD_CONDITION_STR
    );
  }

  @Test
  void normalizeWithNoConditionAndRawValueForBadHexLengthCondition() {
    EscrowFinish actual = EscrowFinish.builder()
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .sequence(UnsignedInteger.ONE)
      .owner(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .offerSequence(UnsignedInteger.ZERO)
      .conditionRawValue("123")
      .build();

    assertThat(actual.condition()).isEmpty();
    assertThat(actual.conditionRawValue()).isNotEmpty().get().isEqualTo("123");
  }

  ////////////////////////////////
  // normalizeFulfillment tests

  /// /////////////////////////////

  @Test
  void normalizeWithNoFulfillmentNoRawValue() {
    EscrowFinish actual = EscrowFinish.builder()
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .sequence(UnsignedInteger.ONE)
      .owner(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .offerSequence(UnsignedInteger.ZERO)
      .build();

    assertThat(actual.fulfillment()).isEmpty();
    assertThat(actual.fulfillmentRawValue()).isEmpty();
  }

  @Test
  void normalizeWithFulfillmentAndRawValueMatching() {
    EscrowFinish actual = EscrowFinish.builder()
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .sequence(UnsignedInteger.ONE)
      .owner(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .offerSequence(UnsignedInteger.ZERO)
      .fulfillment(fulfillment)
      .fulfillmentRawValue(GOOD_FULFILLMENT_STR)
      .build();

    assertThat(actual.fulfillment()).isNotEmpty().get().isEqualTo(fulfillment);
    assertThat(actual.fulfillmentRawValue()).isNotEmpty().get().isEqualTo(GOOD_FULFILLMENT_STR);
  }

  @Test
  void normalizeWithFulfillmentAndRawValueNonMatching() {
    assertThatThrownBy(() -> EscrowFinish.builder()
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .sequence(UnsignedInteger.ONE)
      .owner(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .offerSequence(UnsignedInteger.ZERO)
      .fulfillment(fulfillment)
      // This is slightly different than GOOD_FULFILLMENT_STR
      .fulfillmentRawValue("A0011000")
      .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessage("fulfillment and fulfillmentRawValue should be equivalent if both are present.");
  }

  @Test
  void normalizeWithFulfillmentPresentAndNoRawValue() {
    EscrowFinish actual = EscrowFinish.builder()
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .sequence(UnsignedInteger.ONE)
      .owner(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .offerSequence(UnsignedInteger.ZERO)
      .fulfillment(fulfillment)
      .build();

    assertThat(actual.fulfillmentRawValue()).isNotEmpty().get().isEqualTo(GOOD_FULFILLMENT_STR);
  }

  @Test
  void normalizeWithNoFulfillmentAndRawValueForValidFulfillment() {
    EscrowFinish actual = EscrowFinish.builder()
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .sequence(UnsignedInteger.ONE)
      .owner(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .offerSequence(UnsignedInteger.ZERO)
      .fulfillmentRawValue(GOOD_FULFILLMENT_STR)
      .build();

    assertThat(actual.fulfillment()).isNotEmpty().get().isEqualTo(fulfillment);
  }

  @Test
  void normalizeWithNoFulfillmentAndRawValueForMalformedFulfillment() {
    EscrowFinish actual = EscrowFinish.builder()
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .sequence(UnsignedInteger.ONE)
      .owner(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .offerSequence(UnsignedInteger.ZERO)
      .fulfillmentRawValue("1234")
      .build();

    assertThat(actual.fulfillment()).isEmpty();
    assertThat(actual.fulfillmentRawValue()).isNotEmpty().get().isEqualTo("1234");
  }

  /**
   * This tests the case where fulfillmentRawValue is present and is parseable to a Fulfillment<?> but when the parsed
   * Fulfillment is written to a byte array, the value differs from the fulfillmentRawValue bytes. This can occur if the
   * fulfillment raw value contains a valid fulfillment in the first 32 bytes, but also includes extra bytes afterward,
   * such as in transaction 138543329687544CDAFCD3AB0DCBFE9C4F8E710397747BA7155F19426F493C8D.
   */
  @Test
  void normalizeFulfillmentWithRawValueThatIsParseableButNotValid() {
    EscrowFinish actual = EscrowFinish.builder()
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .sequence(UnsignedInteger.ONE)
      .owner(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .offerSequence(UnsignedInteger.ZERO)
      .fulfillmentRawValue(GOOD_FULFILLMENT_STR + GOOD_FULFILLMENT_STR)
      .build();

    assertThat(actual.fulfillment()).isEmpty();
    assertThat(actual.fulfillmentRawValue()).isNotEmpty().get().isEqualTo(
      GOOD_FULFILLMENT_STR + GOOD_FULFILLMENT_STR
    );
  }

  @Test
  void normalizeWithNoFulfillmentAndRawValueForBadHexLengthFulfillment() {
    EscrowFinish actual = EscrowFinish.builder()
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .sequence(UnsignedInteger.ONE)
      .owner(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .offerSequence(UnsignedInteger.ZERO)
      .fulfillmentRawValue("123")
      .build();

    assertThat(actual.fulfillment()).isEmpty();
    assertThat(actual.fulfillmentRawValue()).isNotEmpty().get().isEqualTo("123");
  }

  @ParameterizedTest
  @CsvSource( {
    // Simulate 10 drop base fee
    "0,10,330",  // Standard 10 drop fee, plus 320 drops
    "1,10,330",  // Standard 10 drop fee, plus 320 drops
    "2,10,330",  // Standard 10 drop fee, plus 320 drops
    "15,10,330", // Standard 10 drop fee, plus 320 drops
    "16,10,340", // Standard 10 drop fee, plus 320 drops + 10 drops for 1 chunk of 16 bytes
    "17,10,340", // Standard 10 drop fee, plus 320 drops + 10 drops for 1 chunk of 16 bytes
    "31,10,340", // Standard 10 drop fee, plus 320 drops + 10 drops for 1 chunk of 16 bytes
    "32,10,350", // Standard 10 drop fee, plus 320 drops + 20 drops for 2 chunks of 16 bytes
    "33,10,350", // Standard 10 drop fee, plus 320 drops + 20 drops for 2 chunks of 16 bytes
    // Simulate 100 drop base fee
    "0,100,3300",  // Standard 100 drop fee, plus 3200 drops
    "1,100,3300",  // Standard 100 drop fee, plus 3200 drops
    "2,100,3300",  // Standard 100 drop fee, plus 3200 drops
    "15,100,3300", // Standard 100 drop fee, plus 3200 drops
    "16,100,3400", // Standard 100 drop fee, plus 3200 drops + 100 drops for 1 chunk of 16 bytes
    "17,100,3400", // Standard 100 drop fee, plus 3200 drops + 100 drops for 1 chunk of 16 bytes
    "31,100,3400", // Standard 100 drop fee, plus 3200 drops + 100 drops for 1 chunk of 16 bytes
    "32,100,3500", // Standard 100 drop fee, plus 3200 drops + 200 drops for 2 chunks of 16 bytes
    "33,100,3500", // Standard 100 drop fee, plus 3200 drops + 200 drops for 2 chunks of 16 bytes
    // Simulate 200 drop base fee
    "0,200,6600", // Standard 200 drop fee, plus 6400 drops
    "1,200,6600", // Standard 200 drop fee, plus 6400 drops
    "2,200,6600", // Standard 200 drop fee, plus 6400 drops
    "15,200,6600",// Standard 200 drop fee, plus 6400 drops
    "16,200,6800",// Standard 200 drop fee, plus 6400 drops + 200 drops for 1 chunk of 16 bytes
    "17,200,6800",// Standard 200 drop fee, plus 6400 drops + 200 drops for 1 chunk of 16 bytes
    "31,200,6800",// Standard 200 drop fee, plus 6400 drops + 200 drops for 1 chunk of 16 bytes
    "32,200,7000",// Standard 200 drop fee, plus 6400 drops + 400 drops for 2 chunks of 16 bytes
    "33,200,7000",// Standard 200 drop fee, plus 6400 drops + 400 drops for 2 chunks of 16 bytes
    // Simulate 5000 drop base fee
    "0,5000,165000", // Standard 5000 drop fee, plus 160000 drops
    "1,5000,165000", // Standard 5000 drop fee, plus 160000 drops
    "2,5000,165000", // Standard 5000 drop fee, plus 160000 drops
    "15,5000,165000",// Standard 5000 drop fee, plus 160000 drops
    "16,5000,170000",// Standard 5000 drop fee, plus 160000 drops + 5000 drops for 1 chunk of 16 bytes
    "17,5000,170000",// Standard 5000 drop fee, plus 160000 drops + 5000 drops for 1 chunk of 16 bytes
    "31,5000,170000",// Standard 5000 drop fee, plus 160000 drops + 5000 drops for 1 chunk of 16 bytes
    "32,5000,175000",// Standard 5000 drop fee, plus 160000 drops + 10000 drops for 2 chunks of 16 bytes
    "33,5000,175000",// Standard 5000 drop fee, plus 160000 drops + 10000 drops for 2 chunks of 16 bytes
  })
  public void testNormalizeWithVariousFulfillmentSizes(int numBytes, int baseFee, int expectedDrops) {
    /////////////////
    // Test Normalize
    /////////////////

    Builder builder = EscrowFinish.builder()
      .fee(XrpCurrencyAmount.ofDrops(1))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .sequence(UnsignedInteger.ONE)
      .owner(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .offerSequence(UnsignedInteger.ZERO);

    Fulfillment<?> fulfillment = PreimageSha256Fulfillment.from(new byte[numBytes]);
    builder.fulfillment(fulfillment);
    builder.fee(EscrowFinish.computeFee(XrpCurrencyAmount.ofDrops(baseFee), fulfillment));
    builder.condition(fulfillment.getDerivedCondition());
    // Standard fee, plus (32 * base-fee) drops, plus one base-fee for every 16 fulfillment bytes.
    // (see https://xrpl.org/transaction-cost.html#fee-levels)
    Assertions.assertThat(builder.build().fee()).isEqualTo(XrpCurrencyAmount.ofDrops(expectedDrops));

    ///////////////////
    // Test Compute Fee
    ///////////////////
    final XrpCurrencyAmount computedFee = EscrowFinish.computeFee(
      XrpCurrencyAmount.ofDrops(baseFee), PreimageSha256Fulfillment.from(new byte[numBytes])
    );
    assertThat(computedFee).isEqualTo(XrpCurrencyAmount.ofDrops(expectedDrops));
  }

  @Test
  public void testComputeFeeWithWrongFulfillment() {
    //////////////////////
    // Invalid Fulfillment
    //////////////////////

    PreimageSha256Fulfillment preimageSha256Fulfillment = PreimageSha256Fulfillment.from(new byte[10]);
    PrefixSha256Fulfillment prefixFulfillment = PrefixSha256Fulfillment.from(
      new byte[1], 50, preimageSha256Fulfillment
    );
    Exception thrownException = assertThrows(
      Exception.class, () -> EscrowFinish.computeFee(XrpCurrencyAmount.ofDrops(10), prefixFulfillment)
    );
    assertThat(thrownException instanceof RuntimeException).isTrue();
    assertThat(thrownException.getMessage()).isEqualTo("Only PreimageSha256Fulfillment is supported.");
  }

  @Test
  public void testComputeFeeWithNullParams() {
    PreimageSha256Fulfillment preimageSha256Fulfillment = PreimageSha256Fulfillment.from(new byte[10]);
    PrefixSha256Fulfillment prefixFulfillment = PrefixSha256Fulfillment.from(
      new byte[1], 50, preimageSha256Fulfillment
    );

    //////////////////////
    // Null Base Fee
    //////////////////////
    Exception thrownException = assertThrows(
      Exception.class, () -> EscrowFinish.computeFee(null, prefixFulfillment)
    );
    assertThat(thrownException instanceof NullPointerException).isTrue();
    assertThat(thrownException.getMessage()).isNull();

    //////////////////////
    // Null Fulfillment
    //////////////////////
    thrownException = assertThrows(
      Exception.class, () -> EscrowFinish.computeFee(XrpCurrencyAmount.ofDrops(10), null)
    );
    assertThat(thrownException instanceof NullPointerException).isTrue();
    assertThat(thrownException.getMessage()).isNull();
  }
}
