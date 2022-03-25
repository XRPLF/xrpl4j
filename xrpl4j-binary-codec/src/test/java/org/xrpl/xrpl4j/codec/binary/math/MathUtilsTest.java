package org.xrpl.xrpl4j.codec.binary.math;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: binary-codec
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
