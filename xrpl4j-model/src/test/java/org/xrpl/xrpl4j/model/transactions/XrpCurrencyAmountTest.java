package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.xrpl.xrpl4j.model.transactions.Wrappers._XrpCurrencyAmount.MAX_XRP;
import static org.xrpl.xrpl4j.model.transactions.Wrappers._XrpCurrencyAmount.MAX_XRP_IN_DROPS;
import static org.xrpl.xrpl4j.model.transactions.Wrappers._XrpCurrencyAmount.ONE_XRP_IN_DROPS;
import static org.xrpl.xrpl4j.model.transactions.Wrappers._XrpCurrencyAmount.TWO_XRP_IN_DROPS;

import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

/**
 * Unit tests for {@link XrpCurrencyAmount}.
 */
public class XrpCurrencyAmountTest {

  private static final long HALF_XRP_IN_DROPS = 500_000L;

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
      XrpCurrencyAmount.ofXrp(XrpCurrencyAmount.ofDrops(1L).toXrp())
        .plus(XrpCurrencyAmount.ofXrp(XrpCurrencyAmount.ofDrops(0L).toXrp()))
    ).isEqualTo(XrpCurrencyAmount.ofDrops(1L));
  }

  @Test
  public void minusXrp() {
    assertThat(
      XrpCurrencyAmount.ofDrops(ONE_XRP_IN_DROPS)
        .minus(XrpCurrencyAmount.ofDrops(HALF_XRP_IN_DROPS))
    ).isEqualTo(XrpCurrencyAmount.ofDrops(HALF_XRP_IN_DROPS));
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
}