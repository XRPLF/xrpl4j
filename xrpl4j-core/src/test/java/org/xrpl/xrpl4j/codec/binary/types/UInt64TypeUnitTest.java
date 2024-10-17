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
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class UInt64TypeUnitTest {
  private final UInt64Type base16Type = new UInt64Type(16);
  private final UInt64Type base10Type = new UInt64Type(10);
  private static UnsignedLong maxUint64 = UnsignedLong.valueOf("FFFFFFFFFFFFFFFF", 16);

  @Test
  void decodeBase16() {
    assertThat(base16Type.fromHex("0000000000000000").valueOf()).isEqualTo(UnsignedLong.valueOf(0));
    assertThat(base16Type.fromHex("000000000000000F").valueOf()).isEqualTo(UnsignedLong.valueOf(15));
    assertThat(base16Type.fromHex("00000000FFFFFFFF").valueOf()).isEqualTo(UnsignedLong.valueOf(4294967295L));
    assertThat(base16Type.fromHex("FFFFFFFFFFFFFFFF").valueOf()).isEqualTo(maxUint64);
  }

  @Test
  void encodeBase16() {
    assertThat(base16Type.fromJson("\"0\"").toHex()).isEqualTo("0000000000000000");
    assertThat(base16Type.fromJson("\"F\"").toHex()).isEqualTo("000000000000000F");
    assertThat(base16Type.fromJson("\"FFFF\"").toHex()).isEqualTo("000000000000FFFF");
    assertThat(base16Type.fromJson("\"FFFFFFFF\"").toHex()).isEqualTo("00000000FFFFFFFF");
    assertThat(base16Type.fromJson("\"FFFFFFFFFFFFFFFF\"").toHex()).isEqualTo("FFFFFFFFFFFFFFFF");
  }

  @Test
  void decodeBase10() {
    assertThat(base10Type.fromHex("0000000000000000").valueOf()).isEqualTo(UnsignedLong.valueOf(0));
    assertThat(base10Type.fromHex("000000000000000F").valueOf()).isEqualTo(UnsignedLong.valueOf(15));
    assertThat(base10Type.fromHex("00000000FFFFFFFF").valueOf()).isEqualTo(UnsignedLong.valueOf(4294967295L));
    assertThat(base10Type.fromHex("FFFFFFFFFFFFFFFF").valueOf()).isEqualTo(maxUint64);
  }

  @Test
  void encodeBase10() {
    assertThat(base10Type.fromJson("\"0\"").toHex()).isEqualTo("0000000000000000");
    assertThat(base10Type.fromJson("\"15\"").toHex()).isEqualTo("000000000000000F");
    assertThat(base10Type.fromJson("\"65535\"").toHex()).isEqualTo("000000000000FFFF");
    assertThat(base10Type.fromJson("\"4294967295\"").toHex()).isEqualTo("00000000FFFFFFFF");
    assertThat(base10Type.fromJson("\"18446744073709551615\"").toHex()).isEqualTo("FFFFFFFFFFFFFFFF");
  }

  @ParameterizedTest
  @ValueSource(strings = {"\"0\"", "\"15\"", "\"65535\"", "\"4294967295\"", "\"18446744073709551615\""})
  void toFromJsonBase10(String json) {
    assertThat(base10Type.fromJson(json).toJson().toString()).isEqualTo(json);
  }

  @ParameterizedTest
  @ValueSource(strings = {"\"0\"", "\"F\"", "\"FFFF\"", "\"FFFFFFFF\"", "\"FFFFFFFFFFFFFFFF\""})
  void toFromJsonBase16(String json) {
    assertThat(base16Type.fromJson(json).toJson().toString()).isEqualTo(json);
  }

  @Test
  void encodeOutOfBounds() {
    assertThrows(IllegalArgumentException.class, () -> base16Type.fromJson("18446744073709551616"));
  }
}
