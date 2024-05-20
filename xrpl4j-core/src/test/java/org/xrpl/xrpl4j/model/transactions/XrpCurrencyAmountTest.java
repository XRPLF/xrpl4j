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

import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

/**
 * Unit tests for {@link XrpCurrencyAmount}.
 */
public class XrpCurrencyAmountTest {

  private static final long HALF_XRP_IN_DROPS = 500_000L;
  private static final long TWO_XRP_IN_DROPS = 2_000_000L;

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
      () -> XrpCurrencyAmount.ofDrops(UnsignedLong.valueOf(MAX_XRP_IN_DROPS + 1))
    );
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
      () -> XrpCurrencyAmount.ofDrops(UnsignedLong.valueOf(MAX_XRP_IN_DROPS + 1), true)
    );
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
    // Positive
    assertThat(
      XrpCurrencyAmount.ofDrops(HALF_XRP_IN_DROPS)
        .plus(XrpCurrencyAmount.ofDrops(HALF_XRP_IN_DROPS))
    ).isEqualTo(XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS));
    // Negative
    assertThat(
      XrpCurrencyAmount.ofDrops(HALF_XRP_IN_DROPS * -1)
        .plus(XrpCurrencyAmount.ofDrops(HALF_XRP_IN_DROPS * -1))
    ).isEqualTo(XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS * -1));

    // Positive
    assertThat(
      XrpCurrencyAmount.ofXrp(new BigDecimal("0.000001"))
        .plus(XrpCurrencyAmount.ofXrp(new BigDecimal("0.000001")))
    ).isEqualTo(XrpCurrencyAmount.ofDrops(2L));
    // Negative
    assertThat(
      XrpCurrencyAmount.ofXrp(new BigDecimal("-0.000001"))
        .plus(XrpCurrencyAmount.ofXrp(new BigDecimal("-0.000001")))
    ).isEqualTo(XrpCurrencyAmount.ofDrops(2L));

    // Positive
    assertThat(
      XrpCurrencyAmount.ofXrp(new BigDecimal("0.000001"))
        .plus(XrpCurrencyAmount.ofXrp(new BigDecimal("-0.000001")))
    ).isEqualTo(XrpCurrencyAmount.ofDrops(0L));
    // Negative
    assertThat(
      XrpCurrencyAmount.ofXrp(new BigDecimal("-0.000001"))
        .plus(XrpCurrencyAmount.ofXrp(new BigDecimal("+0.000001")))
    ).isEqualTo(XrpCurrencyAmount.ofDrops(0L));

    // Overflow
    assertThrows(IllegalStateException.class, () -> {
      XrpCurrencyAmount.ofDrops(MAX_XRP_IN_DROPS) // <-- Max drops
        .plus(XrpCurrencyAmount.ofDrops(1L)); // <-- plus just one more
    });
  }

  @Test
  public void minusXrp() {
    // Positive
    assertThat(
      XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS)
        .minus(XrpCurrencyAmount.ofDrops(HALF_XRP_IN_DROPS))
    ).isEqualTo(XrpCurrencyAmount.ofDrops(HALF_XRP_IN_DROPS));
    // Negative
    assertThat(
      XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS * -1)
        .minus(XrpCurrencyAmount.ofDrops(HALF_XRP_IN_DROPS * -1))
    ).isEqualTo(XrpCurrencyAmount.ofDrops(HALF_XRP_IN_DROPS * -1));

    // Positive
    assertThat(
      XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS)
        .minus(XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS))
    ).isEqualTo((XrpCurrencyAmount.ofDrops(0L)));
    // Negative
    assertThat(
      XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS * -1)
        .minus(XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS * -1))
    ).isEqualTo((XrpCurrencyAmount.ofDrops(0L)));
    assertThat(
      XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS * -1)
        .minus(XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS * -1))
    ).isEqualTo((XrpCurrencyAmount.ofDrops(0L)));

    assertThat(XrpCurrencyAmount.ofDrops(HALF_XRP_IN_DROPS).minus(XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS)))
      .isEqualTo((XrpCurrencyAmount.ofDrops(UnsignedLong.valueOf(500_000L), true)));
    assertThat(
      XrpCurrencyAmount.ofDrops(HALF_XRP_IN_DROPS * -1).minus(XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS * -1)))
      .isEqualTo((XrpCurrencyAmount.ofDrops(UnsignedLong.valueOf(500_000L), false)));

    // Overflow
    assertThrows(IllegalStateException.class, () -> {
      XrpCurrencyAmount.ofDrops(MAX_XRP_IN_DROPS * -1) // <-- Min drops
        .minus(XrpCurrencyAmount.ofDrops(1L)); // <-- minus just one more
    });
  }

  @Test
  public void timesXrp() {
    assertThat(
      XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS)
        .times(XrpCurrencyAmount.ofDrops(TWO_XRP_IN_DROPS))
    ).isEqualTo(XrpCurrencyAmount.ofDrops(TWO_XRP_IN_DROPS * ONE_XRP_IN_DROPS));

    assertThat(
      XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS)
        .times(XrpCurrencyAmount.ofDrops(0L))
    ).isEqualTo(XrpCurrencyAmount.ofDrops(0L));

    // Overflow
    assertThrows(IllegalStateException.class, () -> {
      XrpCurrencyAmount.ofDrops(MAX_XRP_IN_DROPS) // <-- Max drops
        .times(XrpCurrencyAmount.ofDrops(MAX_XRP_IN_DROPS));
    });
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
}
