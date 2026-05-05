package org.xrpl.xrpl4j.codec.binary.types;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: binary-codec
 * %%
 * Copyright (C) 2020 - 2026 XRPL Foundation and its contributors
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

import org.junit.jupiter.api.Test;

class SerializedTypeTest {

  @Test
  void getTypeByNameReturnsKnownTypes() {
    assertThat(SerializedType.getTypeByName("AccountID")).isInstanceOf(AccountIdType.class);
    assertThat(SerializedType.getTypeByName("Amount")).isInstanceOf(AmountType.class);
    assertThat(SerializedType.getTypeByName("UInt32")).isInstanceOf(UInt32Type.class);
    assertThat(SerializedType.getTypeByName("Hash256")).isInstanceOf(Hash256Type.class);
  }

  @Test
  void getTypeByNameThrowsOnUnknownType() {
    assertThatThrownBy(() -> SerializedType.getTypeByName("TEST32"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Unknown serialized type 'TEST32'")
      .hasMessageContaining("xrpl4j is out of date");
  }

  @Test
  void getTypeByNameThrowsOnNull() {
    assertThatThrownBy(() -> SerializedType.getTypeByName(null))
      .isInstanceOf(NullPointerException.class);
  }
}
