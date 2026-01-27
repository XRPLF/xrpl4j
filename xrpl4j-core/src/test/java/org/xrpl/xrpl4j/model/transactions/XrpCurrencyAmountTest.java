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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.xrpl.xrpl4j.model.transactions.Wrappers._XrpCurrencyAmount.MAX_XRP;
import static org.xrpl.xrpl4j.model.transactions.Wrappers._XrpCurrencyAmount.MAX_XRP_IN_DROPS;
import static org.xrpl.xrpl4j.model.transactions.Wrappers._XrpCurrencyAmount.ONE_XRP_IN_DROPS;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedLong;
import org.immutables.value.Value;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

import java.math.BigDecimal;

/**
 * Unit tests for {@link XrpCurrencyAmount}.
 */
public class XrpCurrencyAmountTest {

  private final ObjectMapper objectMapper = ObjectMapperFactory.create();

  private static final long NEGATIVE_HALF_XRP_IN_DROPS = -500_000L;
  private static final long HALF_XRP_IN_DROPS = 500_000L;
  private static final long TWO_XRP_IN_DROPS = 2_000_000L;

  @Test
  void testNull() {
    assertThrows(NullPointerException.class, () -> XrpCurrencyAmount.ofDrops(null));
    assertThrows(NullPointerException.class, () -> XrpCurrencyAmount.ofDrops(null, false));
    assertThrows(NullPointerException.class, () -> XrpCurrencyAmount.ofXrp(null));

    XrpCurrencyAmount xrpCurrencyAmount = XrpCurrencyAmount.ofDrops(1L);
    assertThrows(NullPointerException.class, () -> xrpCurrencyAmount.plus(null));
    assertThrows(NullPointerException.class, () -> xrpCurrencyAmount.minus(null));
    assertThrows(NullPointerException.class, () -> xrpCurrencyAmount.times(null));
    assertThat(xrpCurrencyAmount.equals(null)).isFalse();
  }

  @Test
  public void ofDropsLong() {
    XrpCurrencyAmount xrpCurrencyAmount = XrpCurrencyAmount.ofDrops(0L);
    assertThat(xrpCurrencyAmount.value()).isEqualTo(UnsignedLong.ZERO);
    assertThat(xrpCurrencyAmount.isNegative()).isFalse();

    xrpCurrencyAmount = XrpCurrencyAmount.ofDrops(1L);
    assertThat(xrpCurrencyAmount.value()).isEqualTo(UnsignedLong.ONE);
    assertThat(xrpCurrencyAmount.isNegative()).isFalse();

    xrpCurrencyAmount = XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS);
    assertThat(xrpCurrencyAmount.value()).isEqualTo(UnsignedLong.valueOf(ONE_XRP_IN_DROPS));
    assertThat(xrpCurrencyAmount.isNegative()).isFalse();

    xrpCurrencyAmount = XrpCurrencyAmount.ofDrops(MAX_XRP_IN_DROPS);
    assertThat(xrpCurrencyAmount.value()).isEqualTo(UnsignedLong.valueOf(MAX_XRP_IN_DROPS));
    assertThat(xrpCurrencyAmount.isNegative()).isFalse();

    assertThrows(IllegalStateException.class, () -> XrpCurrencyAmount.ofDrops(MAX_XRP_IN_DROPS + 1));
  }

  @Test
  public void ofDropsLongNegative() {
    XrpCurrencyAmount xrpCurrencyAmount = XrpCurrencyAmount.ofDrops(-1L);
    assertThat(xrpCurrencyAmount.value()).isEqualTo(UnsignedLong.ONE);
    assertThat(xrpCurrencyAmount.isNegative()).isTrue();

    xrpCurrencyAmount = XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS * -1);
    assertThat(xrpCurrencyAmount.value()).isEqualTo(UnsignedLong.valueOf(ONE_XRP_IN_DROPS));
    assertThat(xrpCurrencyAmount.isNegative()).isTrue();

    xrpCurrencyAmount = XrpCurrencyAmount.ofDrops(MAX_XRP_IN_DROPS * -1);
    assertThat(xrpCurrencyAmount.value()).isEqualTo(UnsignedLong.valueOf(MAX_XRP_IN_DROPS));
    assertThat(xrpCurrencyAmount.isNegative()).isTrue();

    assertThrows(IllegalStateException.class, () -> XrpCurrencyAmount.ofDrops((MAX_XRP_IN_DROPS * -1) - 1));
  }

  @Test
  public void ofDropsUnsignedLong() {
    XrpCurrencyAmount xrpCurrencyAmount = XrpCurrencyAmount.ofDrops(UnsignedLong.ZERO);
    assertThat(xrpCurrencyAmount.value()).isEqualTo(UnsignedLong.ZERO);
    assertThat(xrpCurrencyAmount.isNegative()).isFalse();

    xrpCurrencyAmount = XrpCurrencyAmount.ofDrops(UnsignedLong.ONE);
    assertThat(xrpCurrencyAmount.value()).isEqualTo(UnsignedLong.ONE);
    assertThat(xrpCurrencyAmount.isNegative()).isFalse();

    xrpCurrencyAmount = XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS);
    assertThat(xrpCurrencyAmount.value()).isEqualTo(UnsignedLong.valueOf(ONE_XRP_IN_DROPS));
    assertThat(xrpCurrencyAmount.isNegative()).isFalse();

    xrpCurrencyAmount = XrpCurrencyAmount.ofDrops(MAX_XRP_IN_DROPS);
    assertThat(xrpCurrencyAmount.value()).isEqualTo(UnsignedLong.valueOf(MAX_XRP_IN_DROPS));
    assertThat(xrpCurrencyAmount.isNegative()).isFalse();

    // Too big
    assertThrows(IllegalStateException.class,
      () -> XrpCurrencyAmount.ofDrops(UnsignedLong.valueOf(MAX_XRP_IN_DROPS + 1)));
  }

  @Test
  public void ofDropsUnsignedLongNegative() {
    XrpCurrencyAmount xrpCurrencyAmount = XrpCurrencyAmount.ofDrops(UnsignedLong.ZERO, true);
    assertThat(xrpCurrencyAmount.value()).isEqualTo(UnsignedLong.ZERO);
    assertThat(xrpCurrencyAmount.isNegative()).isTrue();

    xrpCurrencyAmount = XrpCurrencyAmount.ofDrops(UnsignedLong.ONE, true);
    assertThat(xrpCurrencyAmount.value()).isEqualTo(UnsignedLong.ONE);
    assertThat(xrpCurrencyAmount.isNegative()).isTrue();

    xrpCurrencyAmount = XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS * -1);
    assertThat(xrpCurrencyAmount.value()).isEqualTo(UnsignedLong.valueOf(ONE_XRP_IN_DROPS));
    assertThat(xrpCurrencyAmount.isNegative()).isTrue();

    xrpCurrencyAmount = XrpCurrencyAmount.ofDrops(MAX_XRP_IN_DROPS * -1);
    assertThat(xrpCurrencyAmount.value()).isEqualTo(UnsignedLong.valueOf(MAX_XRP_IN_DROPS));
    assertThat(xrpCurrencyAmount.isNegative()).isTrue();

    // Too big
    assertThrows(IllegalStateException.class,
      () -> XrpCurrencyAmount.ofDrops(UnsignedLong.valueOf(MAX_XRP_IN_DROPS + 1), true));
  }

  @Test
  public void ofXrpBigDecimal() {
    XrpCurrencyAmount xrpCurrencyAmount = XrpCurrencyAmount.ofXrp(BigDecimal.ZERO);
    assertThat(xrpCurrencyAmount.value()).isEqualTo(UnsignedLong.ZERO);
    assertThat(xrpCurrencyAmount.isNegative()).isFalse();

    xrpCurrencyAmount = XrpCurrencyAmount.ofXrp(new BigDecimal("0.000001"));
    assertThat(xrpCurrencyAmount.value()).isEqualTo(UnsignedLong.ONE);
    assertThat(xrpCurrencyAmount.isNegative()).isFalse();

    xrpCurrencyAmount = XrpCurrencyAmount.ofXrp(new BigDecimal("0.1"));
    assertThat(xrpCurrencyAmount.value()).isEqualTo(UnsignedLong.valueOf(100000));
    assertThat(xrpCurrencyAmount.isNegative()).isFalse();

    xrpCurrencyAmount = XrpCurrencyAmount.ofXrp(BigDecimal.ONE);
    assertThat(xrpCurrencyAmount.value()).isEqualTo(UnsignedLong.valueOf(ONE_XRP_IN_DROPS));
    assertThat(xrpCurrencyAmount.isNegative()).isFalse();

    xrpCurrencyAmount = XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(MAX_XRP));
    assertThat(xrpCurrencyAmount.value()).isEqualTo(UnsignedLong.valueOf(MAX_XRP_IN_DROPS));
    assertThat(xrpCurrencyAmount.isNegative()).isFalse();

    // Too big (positive)
    assertThrows(IllegalArgumentException.class, () -> XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(MAX_XRP + 1)));

    // Too small (positive)
    assertThrows(IllegalArgumentException.class, () -> XrpCurrencyAmount.ofXrp(new BigDecimal("0.0000001")));
  }

  @Test
  public void ofXrpBigDecimalNegative() {
    XrpCurrencyAmount xrpCurrencyAmount = XrpCurrencyAmount.ofXrp(new BigDecimal("-0.000001"));
    assertThat(xrpCurrencyAmount.value()).isEqualTo(UnsignedLong.ONE);
    assertThat(xrpCurrencyAmount.isNegative()).isTrue();

    xrpCurrencyAmount = XrpCurrencyAmount.ofXrp(new BigDecimal("-0.1"));
    assertThat(xrpCurrencyAmount.value()).isEqualTo(UnsignedLong.valueOf(100000));
    assertThat(xrpCurrencyAmount.isNegative()).isTrue();

    xrpCurrencyAmount = XrpCurrencyAmount.ofXrp(BigDecimal.ONE.negate());
    assertThat(xrpCurrencyAmount.value()).isEqualTo(UnsignedLong.valueOf(ONE_XRP_IN_DROPS));
    assertThat(xrpCurrencyAmount.isNegative()).isTrue();

    xrpCurrencyAmount = XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(MAX_XRP).negate());
    assertThat(xrpCurrencyAmount.value()).isEqualTo(UnsignedLong.valueOf(MAX_XRP_IN_DROPS));
    assertThat(xrpCurrencyAmount.isNegative()).isTrue();

    // Too small (negative)
    assertThrows(IllegalArgumentException.class, () -> XrpCurrencyAmount.ofXrp(BigDecimal.valueOf((MAX_XRP * -1) - 1)));
    // Too big (positive)
    assertThrows(IllegalArgumentException.class, () -> XrpCurrencyAmount.ofXrp(new BigDecimal("-0.0000001")));
  }

  @Test
  public void toXrp() {

    // ////////////////
    // Zero Value
    // ////////////////

    XrpCurrencyAmount xrpCurrencyAmount = XrpCurrencyAmount.ofDrops(0L);
    assertThat(xrpCurrencyAmount.toXrp()).isEqualTo(BigDecimal.ZERO);
    assertThat(xrpCurrencyAmount.isNegative()).isFalse();

    // ////////////////
    // Positive Values
    // ////////////////

    xrpCurrencyAmount = XrpCurrencyAmount.ofDrops(1L);
    assertThat(xrpCurrencyAmount.toXrp()).isEqualTo(new BigDecimal("0.000001"));
    assertThat(xrpCurrencyAmount.isNegative()).isFalse();

    xrpCurrencyAmount = XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS);
    assertThat(xrpCurrencyAmount.toXrp()).isEqualTo(BigDecimal.ONE);
    assertThat(xrpCurrencyAmount.isNegative()).isFalse();

    xrpCurrencyAmount = XrpCurrencyAmount.ofDrops(33);
    assertThat(xrpCurrencyAmount.toXrp()).isEqualTo(new BigDecimal("0.000033"));
    assertThat(xrpCurrencyAmount.isNegative()).isFalse();

    xrpCurrencyAmount = XrpCurrencyAmount.ofDrops(MAX_XRP_IN_DROPS);
    assertThat(xrpCurrencyAmount.toXrp()).isEqualTo(new BigDecimal(MAX_XRP));
    assertThat(xrpCurrencyAmount.isNegative()).isFalse();

    // Too small (the largest number with the largest decimal component (99B plus nearly 1 drop)).
    assertThat(XrpCurrencyAmount.ofDrops(MAX_XRP_IN_DROPS - 1).toXrp()).isEqualTo(new BigDecimal("99999999999.999999"));

    // ////////////////
    // Negative Values
    // ////////////////

    xrpCurrencyAmount = XrpCurrencyAmount.ofDrops(-1L);
    assertThat(xrpCurrencyAmount.toXrp()).isEqualTo(new BigDecimal("-0.000001"));
    assertThat(xrpCurrencyAmount.isNegative()).isTrue();

    xrpCurrencyAmount = XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS * -1);
    assertThat(xrpCurrencyAmount.toXrp()).isEqualTo(BigDecimal.ONE.negate());
    assertThat(xrpCurrencyAmount.isNegative()).isTrue();

    xrpCurrencyAmount = XrpCurrencyAmount.ofDrops(-33);
    assertThat(xrpCurrencyAmount.toXrp()).isEqualTo(new BigDecimal("-0.000033"));
    assertThat(xrpCurrencyAmount.isNegative()).isTrue();

    xrpCurrencyAmount = XrpCurrencyAmount.ofDrops(MAX_XRP_IN_DROPS * -1);
    assertThat(xrpCurrencyAmount.toXrp()).isEqualTo(new BigDecimal(MAX_XRP * -1));
    assertThat(xrpCurrencyAmount.isNegative()).isTrue();

    // Too small (the largest number with the largest decimal component (99B plus nearly 1 drop)).
    xrpCurrencyAmount = XrpCurrencyAmount.ofDrops((MAX_XRP_IN_DROPS * -1) + 1);
    assertThat(xrpCurrencyAmount.toXrp()).isEqualTo(new BigDecimal("-99999999999.999999"));
    assertThat(xrpCurrencyAmount.isNegative()).isTrue();
  }

  @Test
  public void plusXrp() {
    // First Positive, Second Positive
    {
      XrpCurrencyAmount result = XrpCurrencyAmount.ofDrops(HALF_XRP_IN_DROPS)
        .plus(XrpCurrencyAmount.ofDrops(HALF_XRP_IN_DROPS));
      assertThat(result).isEqualTo(XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS));
      assertThat(result.value()).isEqualTo(UnsignedLong.valueOf(ONE_XRP_IN_DROPS));
      assertThat(result.isNegative()).isFalse();
    }

    // First Positive, Second Negative
    {
      XrpCurrencyAmount result = XrpCurrencyAmount.ofDrops(NEGATIVE_HALF_XRP_IN_DROPS)
        .plus(XrpCurrencyAmount.ofDrops(HALF_XRP_IN_DROPS));
      assertThat(result).isEqualTo(XrpCurrencyAmount.ofDrops(0L));
      assertThat(result.value()).isEqualTo(UnsignedLong.ZERO);
      assertThat(result.isNegative()).isFalse();
    }

    // First Negative, Second Positive
    {
      XrpCurrencyAmount result = XrpCurrencyAmount.ofDrops(HALF_XRP_IN_DROPS)
        .plus(XrpCurrencyAmount.ofDrops(NEGATIVE_HALF_XRP_IN_DROPS));
      assertThat(result).isEqualTo(XrpCurrencyAmount.ofDrops(0L));
      assertThat(result.value()).isEqualTo(UnsignedLong.ZERO);
      assertThat(result.isNegative()).isFalse();
    }

    // First Negative, Second Negative
    {
      XrpCurrencyAmount result = XrpCurrencyAmount.ofDrops(NEGATIVE_HALF_XRP_IN_DROPS)
        .plus(XrpCurrencyAmount.ofDrops(NEGATIVE_HALF_XRP_IN_DROPS));
      assertThat(result).isEqualTo(XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS * -1));
      assertThat(result.value()).isEqualTo(UnsignedLong.valueOf(ONE_XRP_IN_DROPS));
      assertThat(result.isNegative()).isTrue();
    }

    // Decimals (first positive, second positive)
    assertThat(XrpCurrencyAmount.ofXrp(new BigDecimal("0.000001"))
      .plus(XrpCurrencyAmount.ofXrp(new BigDecimal("0.000001")))).isEqualTo(XrpCurrencyAmount.ofDrops(2L));
    // Decimals (first positive, second negative)
    assertThat(XrpCurrencyAmount.ofXrp(new BigDecimal("0.000001"))
      .plus(XrpCurrencyAmount.ofXrp(new BigDecimal("-0.000001")))).isEqualTo(XrpCurrencyAmount.ofDrops(0L));
    // Decimals (first negative, second positive)
    assertThat(XrpCurrencyAmount.ofXrp(new BigDecimal("-0.000001"))
      .plus(XrpCurrencyAmount.ofXrp(new BigDecimal("+0.000001")))).isEqualTo(XrpCurrencyAmount.ofDrops(0L));
    // Decimals (first negative, second negative)
    assertThat(XrpCurrencyAmount.ofXrp(new BigDecimal("-0.000001"))
      .plus(XrpCurrencyAmount.ofXrp(new BigDecimal("-0.000001")))).isEqualTo(XrpCurrencyAmount.ofDrops(2L));

    // Overflow
    assertThrows(IllegalStateException.class, () -> {
      XrpCurrencyAmount.ofDrops(MAX_XRP_IN_DROPS) // <-- Max drops
        .plus(XrpCurrencyAmount.ofDrops(1L)); // <-- plus just one more
    });
  }

  @Test
  public void minusXrp() {
    // First Positive, Second Positive
    {
      XrpCurrencyAmount result = XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS)
        .minus(XrpCurrencyAmount.ofDrops(HALF_XRP_IN_DROPS));
      assertThat(result).isEqualTo(XrpCurrencyAmount.ofDrops(HALF_XRP_IN_DROPS));
      assertThat(result.value()).isEqualTo(UnsignedLong.valueOf(500_000));
      assertThat(result.isNegative()).isFalse();
    }

    // First Positive, Second Negative
    {
      XrpCurrencyAmount result = XrpCurrencyAmount.ofDrops(NEGATIVE_HALF_XRP_IN_DROPS)
        .minus(XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS * -1));
      assertThat(result).isEqualTo(XrpCurrencyAmount.ofDrops(HALF_XRP_IN_DROPS));
      assertThat(result.value()).isEqualTo(UnsignedLong.valueOf(500_000L));
      assertThat(result.isNegative()).isFalse();
    }

    // First Negative, Second Positive
    {
      XrpCurrencyAmount result = XrpCurrencyAmount.ofDrops(HALF_XRP_IN_DROPS)
        .minus(XrpCurrencyAmount.ofDrops(NEGATIVE_HALF_XRP_IN_DROPS));
      assertThat(result).isEqualTo(XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS));
      assertThat(result.value()).isEqualTo(UnsignedLong.valueOf(ONE_XRP_IN_DROPS));
      assertThat(result.isNegative()).isFalse();
    }

    // First Negative, Second Negative
    {
      XrpCurrencyAmount result = XrpCurrencyAmount.ofDrops(NEGATIVE_HALF_XRP_IN_DROPS)
        .minus(XrpCurrencyAmount.ofDrops(NEGATIVE_HALF_XRP_IN_DROPS));
      assertThat(result).isEqualTo(XrpCurrencyAmount.ofDrops(0));
      assertThat(result.value()).isEqualTo(UnsignedLong.ZERO);
      assertThat(result.isNegative()).isFalse();
    }

    // Decimals (first positive, second positive)
    assertThat(XrpCurrencyAmount.ofXrp(new BigDecimal("0.000001"))
      .minus(XrpCurrencyAmount.ofXrp(new BigDecimal("0.000001")))).isEqualTo(XrpCurrencyAmount.ofDrops(0L));
    // Decimals (first positive, second negative)
    assertThat(XrpCurrencyAmount.ofXrp(new BigDecimal("0.000001"))
      .minus(XrpCurrencyAmount.ofXrp(new BigDecimal("-0.000001")))).isEqualTo(XrpCurrencyAmount.ofDrops(2L));
    // Decimals (first negative, second positive)
    assertThat(XrpCurrencyAmount.ofXrp(new BigDecimal("-0.000001"))
      .minus(XrpCurrencyAmount.ofXrp(new BigDecimal("+0.000001")))).isEqualTo(XrpCurrencyAmount.ofDrops(-2L));
    // Decimals (first negative, second negative)
    assertThat(XrpCurrencyAmount.ofXrp(new BigDecimal("-0.000001"))
      .minus(XrpCurrencyAmount.ofXrp(new BigDecimal("-0.000001")))).isEqualTo(XrpCurrencyAmount.ofDrops(0L));

    // Overflow
    assertThrows(IllegalStateException.class, () -> {
      XrpCurrencyAmount.ofDrops(UnsignedLong.MAX_VALUE, true) // <-- Min drops
        .minus(XrpCurrencyAmount.ofDrops(1L)); // <-- plus just one more
    });

    assertThat(
      XrpCurrencyAmount.ofDrops(HALF_XRP_IN_DROPS).minus(XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS))).isEqualTo(
      (XrpCurrencyAmount.ofDrops(UnsignedLong.valueOf(500_000L), true)));
    assertThat(XrpCurrencyAmount.ofDrops(HALF_XRP_IN_DROPS * -1)
      .minus(XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS * -1))).isEqualTo(
      (XrpCurrencyAmount.ofDrops(UnsignedLong.valueOf(500_000L), false)));
  }

  @Test
  public void timesXrp() {
    assertThat(
      XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS).times(XrpCurrencyAmount.ofDrops(TWO_XRP_IN_DROPS))).isEqualTo(
      XrpCurrencyAmount.ofDrops(TWO_XRP_IN_DROPS * ONE_XRP_IN_DROPS));

    assertThat(XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS).times(XrpCurrencyAmount.ofDrops(0L))).isEqualTo(
      XrpCurrencyAmount.ofDrops(0L));

    // Overflow
    assertThrows(IllegalStateException.class, () -> {
      XrpCurrencyAmount.ofDrops(MAX_XRP_IN_DROPS) // <-- Max drops
        .times(XrpCurrencyAmount.ofDrops(MAX_XRP_IN_DROPS));
    });

    // Neither Negative
    {
      final XrpCurrencyAmount value = XrpCurrencyAmount.ofDrops(UnsignedLong.valueOf(2L), false)
        .times(XrpCurrencyAmount.ofDrops(UnsignedLong.valueOf(3L), false));
      assertThat(value.value()).isEqualTo(UnsignedLong.valueOf(6L));
      assertThat(value.isNegative()).isFalse();
    }

    // First Negative
    {
      final XrpCurrencyAmount value = XrpCurrencyAmount.ofDrops(UnsignedLong.valueOf(2L), false)
        .times(XrpCurrencyAmount.ofDrops(UnsignedLong.valueOf(3L), true));
      assertThat(value.value()).isEqualTo(UnsignedLong.valueOf(6L));
      assertThat(value.isNegative()).isTrue();
    }

    {
      // Second Negative
      final XrpCurrencyAmount value = XrpCurrencyAmount.ofDrops(UnsignedLong.valueOf(2L), true)
        .times(XrpCurrencyAmount.ofDrops(UnsignedLong.valueOf(3L), false));
      assertThat(value.value()).isEqualTo(UnsignedLong.valueOf(6L));
      assertThat(value.isNegative()).isTrue();
    }

    {
      // Both Negative
      final XrpCurrencyAmount value = XrpCurrencyAmount.ofDrops(UnsignedLong.valueOf(2L), true)
        .times(XrpCurrencyAmount.ofDrops(UnsignedLong.valueOf(3L), true));
      assertThat(value.value()).isEqualTo(UnsignedLong.valueOf(6L));
      assertThat(value.isNegative()).isTrue();
    }
  }

  @Test
  public void toStringXrp() {
    // Negative values.
    assertThat(XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS * -1).toString()).isEqualTo("-1000000");
    assertThat(XrpCurrencyAmount.ofDrops(-2L).toString()).isEqualTo("-2");
    assertThat(XrpCurrencyAmount.ofDrops(-1L).toString()).isEqualTo("-1");
    assertThat(XrpCurrencyAmount.ofXrp(new BigDecimal("-1")).toString()).isEqualTo("-1000000");
    assertThat(XrpCurrencyAmount.ofDrops(UnsignedLong.valueOf(123456789L), true).toString()).isEqualTo("-123456789");

    // Positive values
    assertThat(XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS).toString()).isEqualTo("1000000");
    assertThat(XrpCurrencyAmount.ofDrops(2L).toString()).isEqualTo("2");
    assertThat(XrpCurrencyAmount.ofDrops(1L).toString()).isEqualTo("1");
    assertThat(XrpCurrencyAmount.ofXrp(new BigDecimal("1")).toString()).isEqualTo("1000000");
    assertThat(XrpCurrencyAmount.ofDrops(UnsignedLong.valueOf(123456789L)).toString()).isEqualTo("123456789");
  }

  @Test
  public void testJsonSerialization() throws JsonProcessingException, JSONException {
    XrpCurrencyAmount amount = XrpCurrencyAmount.ofDrops(1000000);
    XrpCurrencyAmountWrapper wrapper = ImmutableXrpCurrencyAmountWrapper.builder()
      .value(amount)
      .build();
    assertSerializesAndDeserializes(wrapper, "{\"value\":\"1000000\"}");
  }

  private void assertSerializesAndDeserializes(XrpCurrencyAmountWrapper wrapper, String json)
    throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    XrpCurrencyAmountWrapper deserialized = objectMapper.readValue(
      serialized,
      XrpCurrencyAmountWrapper.class
    );
    assertThat(deserialized).isEqualTo(wrapper);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableXrpCurrencyAmountWrapper.class)
  @JsonDeserialize(as = ImmutableXrpCurrencyAmountWrapper.class)
  interface XrpCurrencyAmountWrapper {

    XrpCurrencyAmount value();
  }
}
