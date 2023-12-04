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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UInt64TypeUnitTest {
  private final UInt64Type codec = new UInt64Type();
  private static UnsignedLong maxUint64 = UnsignedLong.valueOf("FFFFFFFFFFFFFFFF", 16);

  @Test
  void decode() {
    assertThat(codec.fromHex("0000000000000000").valueOf()).isEqualTo(UnsignedLong.valueOf(0));
    assertThat(codec.fromHex("000000000000000F").valueOf()).isEqualTo(UnsignedLong.valueOf(15));
    assertThat(codec.fromHex("00000000FFFFFFFF").valueOf()).isEqualTo(UnsignedLong.valueOf(4294967295L));
    assertThat(codec.fromHex("FFFFFFFFFFFFFFFF").valueOf()).isEqualTo(maxUint64);
  }

  @Test
  void encode() {
    assertThat(codec.fromJson("\"0\"").toHex()).isEqualTo("0000000000000000");
    assertThat(codec.fromJson("\"F\"").toHex()).isEqualTo("000000000000000F");
    assertThat(codec.fromJson("\"FFFF\"").toHex()).isEqualTo("000000000000FFFF");
    assertThat(codec.fromJson("\"FFFFFFFF\"").toHex()).isEqualTo("00000000FFFFFFFF");
    assertThat(codec.fromJson("\"FFFFFFFFFFFFFFFF\"").toHex()).isEqualTo("FFFFFFFFFFFFFFFF");
  }

  @Test
  void encodeOutOfBounds() {
    assertThrows(IllegalArgumentException.class, () -> codec.fromJson("18446744073709551616"));
  }
}
