package org.xrpl.xrpl4j.model.transactions;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
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

/**
 * Unit tests for {@link MpTokenNumericAmount}.
 */
public class MpTokenNumericAmountTest {

  @Test
  void testToString() {
    MpTokenNumericAmount amount = MpTokenNumericAmount.of(UnsignedLong.ZERO);
    assertThat(amount.toString()).isEqualTo("0");

    amount = MpTokenNumericAmount.of(100000L);
    assertThat(amount.toString()).isEqualTo("100000");

    amount = MpTokenNumericAmount.of(UnsignedLong.MAX_VALUE);
    assertThat(amount.toString()).isEqualTo("18446744073709551615");
  }
}

