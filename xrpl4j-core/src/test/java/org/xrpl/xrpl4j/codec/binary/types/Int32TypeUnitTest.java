package org.xrpl.xrpl4j.codec.binary.types;

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

import org.junit.jupiter.api.Test;

public class Int32TypeUnitTest {

  private final Int32Type codec = new Int32Type();

  @Test
  void decode() {
    assertThat(codec.fromHex("00000000").toJson().asInt()).isEqualTo(0);
    assertThat(codec.fromHex("0000000F").toJson().asInt()).isEqualTo(15);
    assertThat(codec.fromHex("FFFFFFFB").toJson().asInt()).isEqualTo(-5);
    assertThat(codec.fromHex("80000000").toJson().asInt()).isEqualTo(Integer.MIN_VALUE);
    assertThat(codec.fromHex("7FFFFFFF").toJson().asInt()).isEqualTo(Integer.MAX_VALUE);
  }

  @Test
  void encode() {
    assertThat(codec.fromJson("0").toHex()).isEqualTo("00000000");
    assertThat(codec.fromJson("15").toHex()).isEqualTo("0000000F");
    assertThat(codec.fromJson("-5").toHex()).isEqualTo("FFFFFFFB");
    assertThat(codec.fromJson(String.valueOf(Integer.MIN_VALUE)).toHex()).isEqualTo("80000000");
    assertThat(codec.fromJson(String.valueOf(Integer.MAX_VALUE)).toHex()).isEqualTo("7FFFFFFF");
  }

  @Test
  void roundTrip() {
    for (int value : new int[] {0, 1, -1, 5, -5, Integer.MIN_VALUE, Integer.MAX_VALUE}) {
      String hex = codec.fromJson(String.valueOf(value)).toHex();
      assertThat(codec.fromHex(hex).toJson().asInt()).isEqualTo(value);
    }
  }

}
