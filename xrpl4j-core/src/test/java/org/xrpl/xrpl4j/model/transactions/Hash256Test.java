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

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Hash256}.
 */
public class Hash256Test {

  @Test
  public void hashEquality() {
    assertThat(Hash256.of("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"))
      .isEqualTo(Hash256.of("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"));

    assertThat(Hash256.of("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"))
      .isEqualTo(Hash256.of("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"));

    assertThat(Hash256.of("0000000000000000000000000000000000000000000000000000000000000000"))
      .isNotEqualTo(Hash256.of("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"));

    assertThat(Hash256.of("0000000000000000000000000000000000000000000000000000000000000000")).isNotEqualTo(null);
    assertThat(Hash256.of("0000000000000000000000000000000000000000000000000000000000000000"))
      .isNotEqualTo(new Object());
  }

  @Test
  public void hashHashcode() {
    assertThat(Hash256.of("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF").hashCode())
      .isEqualTo(Hash256.of("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF").hashCode());

    assertThat(Hash256.of("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF").hashCode())
      .isEqualTo(Hash256.of("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff").hashCode());

    assertThat(Hash256.of("0000000000000000000000000000000000000000000000000000000000000000").hashCode())
      .isNotEqualTo(Hash256.of("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF").hashCode());
  }

}
