package org.xrpl.xrpl4j.model.immutables;

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

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;

public class FluentCompareToTest {

  static UnsignedInteger SMALLER = UnsignedInteger.valueOf(1);
  static UnsignedInteger BIGGER = UnsignedInteger.valueOf(100);
  static UnsignedInteger MAX_UINT = UnsignedInteger.MAX_VALUE;

  @Test
  public void simpleTest() {
    assert (FluentCompareTo.is(SMALLER).equalTo(SMALLER));
    assert (FluentCompareTo.is(BIGGER).equalTo(BIGGER));
    assert (FluentCompareTo.is(SMALLER).notEqualTo(BIGGER));
    assert (FluentCompareTo.isNot(SMALLER).notEqualTo(BIGGER));
    assert (FluentCompareTo.isNot(SMALLER).equalTo(SMALLER));

    assert (FluentCompareTo.is(SMALLER).lessThanOrEqualTo(SMALLER));
    assert (FluentCompareTo.is(SMALLER).lessThanOrEqualTo(BIGGER));
    assert (FluentCompareTo.is(SMALLER).lessThan(BIGGER));
    assert (FluentCompareTo.is(SMALLER).notGreaterThan(BIGGER));
    assert (FluentCompareTo.is(SMALLER).notGreaterThanEqualTo(BIGGER));
    assertThat(FluentCompareTo.is(SMALLER).greaterThan(SMALLER)).isFalse();
    assertThat(FluentCompareTo.is(SMALLER).lessThan(SMALLER)).isFalse();

    assert (FluentCompareTo.is(BIGGER).greaterThanEqualTo(BIGGER));
    assert (FluentCompareTo.is(BIGGER).greaterThanEqualTo(SMALLER));
    assert (FluentCompareTo.is(BIGGER).greaterThan(SMALLER));
    assert (FluentCompareTo.is(BIGGER).notLessThan(SMALLER));
    assert (FluentCompareTo.is(BIGGER).notLessThanOrEqualTo(SMALLER));
    assertThat(FluentCompareTo.is(BIGGER).greaterThan(BIGGER)).isFalse();
    assertThat(FluentCompareTo.is(BIGGER).lessThan(BIGGER)).isFalse();

    assert (FluentCompareTo.is(SMALLER).between(SMALLER, MAX_UINT));
    assertThat(FluentCompareTo.is(BIGGER).betweenExclusive(SMALLER, BIGGER)).isFalse();
    assert (FluentCompareTo.is(MAX_UINT).notBetween(SMALLER, BIGGER));
    assertThat(FluentCompareTo.is(BIGGER).notBetweenExclusive(SMALLER, MAX_UINT)).isFalse();

    assertThat(FluentCompareTo.is(SMALLER).getValue()).isEqualTo(UnsignedInteger.valueOf(1));
    assertThat(FluentCompareTo.is(BIGGER).getValue()).isEqualTo(UnsignedInteger.valueOf(100));
    assertThat(FluentCompareTo.is(SMALLER)).isNotEqualTo(FluentCompareTo.is(BIGGER));

    assert (FluentCompareTo.is(MAX_UINT).equalTo(MAX_UINT));
    assertThat(FluentCompareTo.is(MAX_UINT).greaterThan(MAX_UINT)).isFalse();
    assertThat(FluentCompareTo.is(MAX_UINT).lessThan(MAX_UINT)).isFalse();
  }
}
