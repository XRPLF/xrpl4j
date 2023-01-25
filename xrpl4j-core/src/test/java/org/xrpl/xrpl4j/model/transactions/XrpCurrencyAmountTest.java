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
    assertThat(XrpCurrencyAmount.ofDrops(0L).value()).isEqualTo(UnsignedLong.ZERO);
    assertThat(XrpCurrencyAmount.ofDrops(1L).value()).isEqualTo(UnsignedLong.ONE);
    assertThat(XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS).value()).isEqualTo(UnsignedLong.valueOf(ONE_XRP_IN_DROPS));
    assertThat(XrpCurrencyAmount.ofDrops(MAX_XRP_IN_DROPS).value()).isEqualTo(UnsignedLong.valueOf(MAX_XRP_IN_DROPS));
    assertThrows(IllegalStateException.class, () -> XrpCurrencyAmount.ofDrops(MAX_XRP_IN_DROPS + 1));
  }

  @Test
  public void ofDropsUnsignedLong() {
    assertThat(XrpCurrencyAmount.ofDrops(UnsignedLong.ZERO).value()).isEqualTo(UnsignedLong.ZERO);
    assertThat(XrpCurrencyAmount.ofDrops(UnsignedLong.ONE).value()).isEqualTo(UnsignedLong.ONE);
    assertThat(XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS).value()).isEqualTo(UnsignedLong.valueOf(ONE_XRP_IN_DROPS));
    assertThat(XrpCurrencyAmount.ofDrops(MAX_XRP_IN_DROPS).value()).isEqualTo(UnsignedLong.valueOf(MAX_XRP_IN_DROPS));

    // Too big
    assertThrows(IllegalStateException.class,
      () -> XrpCurrencyAmount.ofDrops(UnsignedLong.valueOf(MAX_XRP_IN_DROPS + 1)));
  }

  @Test
  public void ofXrpBigDecimal() {
    assertThat(XrpCurrencyAmount.ofXrp(BigDecimal.ZERO).value()).isEqualTo(UnsignedLong.ZERO);
    assertThat(XrpCurrencyAmount.ofXrp(new BigDecimal("0.000001")).value()).isEqualTo(UnsignedLong.ONE);
    assertThat(XrpCurrencyAmount.ofXrp(new BigDecimal("0.1")).value()).isEqualTo(UnsignedLong.valueOf(100000));
    assertThat(XrpCurrencyAmount.ofXrp(BigDecimal.ONE).value()).isEqualTo(UnsignedLong.valueOf(ONE_XRP_IN_DROPS));
    assertThat(XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(MAX_XRP)).value())
      .isEqualTo(UnsignedLong.valueOf(MAX_XRP_IN_DROPS));

    // Too big
    assertThrows(IllegalStateException.class, () -> XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(MAX_XRP + 1)));
    // Too small
    assertThrows(IllegalArgumentException.class, () -> XrpCurrencyAmount.ofXrp(new BigDecimal("0.0000001")));
  }

  @Test
  public void toXrp() {
    assertThat(XrpCurrencyAmount.ofDrops(0L).toXrp()).isEqualTo(BigDecimal.ZERO);
    assertThat(XrpCurrencyAmount.ofDrops(1L).toXrp()).isEqualTo(new BigDecimal("0.000001"));
    assertThat(XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS).toXrp()).isEqualTo(BigDecimal.ONE);
    assertThat(XrpCurrencyAmount.ofDrops(33).toXrp()).isEqualTo(new BigDecimal("0.000033"));
    assertThat(XrpCurrencyAmount.ofDrops(MAX_XRP_IN_DROPS).toXrp()).isEqualTo(new BigDecimal(MAX_XRP));

    // Too small (the largest number with the largest decimal component (99B plus nearly 1 drop)).
    assertThat(XrpCurrencyAmount.ofDrops(MAX_XRP_IN_DROPS - 1).toXrp()).isEqualTo(new BigDecimal("99999999999.999999"));
  }

  @Test
  public void plusXrp() {
    assertThat(
      XrpCurrencyAmount.ofDrops(HALF_XRP_IN_DROPS)
        .plus(XrpCurrencyAmount.ofDrops(HALF_XRP_IN_DROPS))
    ).isEqualTo(XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS));

    assertThat(
      XrpCurrencyAmount.ofXrp(new BigDecimal("0.000001"))
        .plus(XrpCurrencyAmount.ofXrp(new BigDecimal("0.000001")))
    ).isEqualTo(XrpCurrencyAmount.ofDrops(2L));
  }

  @Test
  public void minusXrp() {
    assertThat(
      XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS)
        .minus(XrpCurrencyAmount.ofDrops(HALF_XRP_IN_DROPS))
    ).isEqualTo(XrpCurrencyAmount.ofDrops(HALF_XRP_IN_DROPS));

    assertThat(
      XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS)
        .minus(XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS))
    ).isEqualTo((XrpCurrencyAmount.ofDrops(0L)));

    assertThrows(IllegalStateException.class,
      () -> XrpCurrencyAmount.ofDrops(HALF_XRP_IN_DROPS)
        .minus(XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS))
    );
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
  }

  @Test
  public void toStringXrp() {
    assertThat(XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS).toString()).isEqualTo("1000000");
    assertThat(XrpCurrencyAmount.ofDrops(1L).toString()).isEqualTo("1");
    assertThat(XrpCurrencyAmount.ofXrp(new BigDecimal("1")).toString()).isEqualTo("1000000");
    assertThat(XrpCurrencyAmount.ofDrops(UnsignedLong.valueOf(123456789L)).toString()).isEqualTo("123456789");
  }
}
