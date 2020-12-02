package com.ripple.xrpl4j.codec.binary.math;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

class MathUtilsTest {

  @Test
  void getExponent() {
    assertThat(MathUtils.getExponent(new BigDecimal("0"))).isEqualTo(0);
    assertThat(MathUtils.getExponent(new BigDecimal("2"))).isEqualTo(0);
    assertThat(MathUtils.getExponent(new BigDecimal("-2"))).isEqualTo(0);
    assertThat(MathUtils.getExponent(new BigDecimal("2.2"))).isEqualTo(0);
    assertThat(MathUtils.getExponent(new BigDecimal("20"))).isEqualTo(1);
    assertThat(MathUtils.getExponent(new BigDecimal("20.2"))).isEqualTo(1);
    assertThat(MathUtils.getExponent(new BigDecimal("0.1"))).isEqualTo(-1);
    assertThat(MathUtils.getExponent(new BigDecimal("0.11"))).isEqualTo(-1);
    assertThat(MathUtils.getExponent(new BigDecimal("0.011"))).isEqualTo(-2);
  }

  @Test
  void toPaddedBigInteger() {
    assertThat(MathUtils.toPaddedBigInteger(new BigDecimal("1"), 1)).isEqualTo(BigInteger.valueOf(1));
    assertThat(MathUtils.toPaddedBigInteger(new BigDecimal("1"), 2)).isEqualTo(BigInteger.valueOf(10));
    assertThat(MathUtils.toPaddedBigInteger(new BigDecimal("1.0"), 2)).isEqualTo(BigInteger.valueOf(10));
    assertThat(MathUtils.toPaddedBigInteger(new BigDecimal("1.1"), 2)).isEqualTo(BigInteger.valueOf(11));
    assertThat(MathUtils.toPaddedBigInteger(new BigDecimal("1.1"), 3)).isEqualTo(BigInteger.valueOf(110));
    assertThat(MathUtils.toPaddedBigInteger(new BigDecimal("1111111111111111.0"), 16))
      .isEqualTo(new BigInteger("1111111111111111"));
  }
}
