package org.xrpl.xrpl4j.codec.binary.types;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UIntTypeTest {

  private final UInt16Type codec = new UInt16Type();

  @Test
  void decode() {
    assertThat(codec.fromHex("0000").valueOf()).isEqualTo(UnsignedLong.valueOf(0));
    assertThat(codec.fromHex("000F").valueOf()).isEqualTo(UnsignedLong.valueOf(15));
    assertThat(codec.fromHex("FFFF").valueOf()).isEqualTo(UnsignedLong.valueOf(65535));
  }

  @Test
  void encode() throws JsonProcessingException {
    assertThat(codec.fromJson("0").toHex()).isEqualTo("0000");
    assertThat(codec.fromJson("15").toHex()).isEqualTo("000F");
    assertThat(codec.fromJson("65535").toHex()).isEqualTo("FFFF");
  }

  @Test
  void encodeOutOfBounds() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> codec.fromJson("65536"));
  }

}
