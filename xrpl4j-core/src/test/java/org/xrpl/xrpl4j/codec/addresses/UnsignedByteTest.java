package org.xrpl.xrpl4j.codec.addresses;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: address-codec
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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.mockito.internal.matchers.Null;

import java.math.BigInteger;

/**
 * Unit tests for {@link UnsignedByte}.
 */
public class UnsignedByteTest {

  @Test
  void constructorTests() {
    assertThrows(IllegalArgumentException.class, () -> UnsignedByte.of(-1));
    assertThrows(NullPointerException.class, () -> UnsignedByte.of((UnsignedByte) null));
    assertThat(UnsignedByte.of(UnsignedByte.of(0))).isEqualTo(UnsignedByte.of(0));
  }

  @Test
  void hexValue() {
    assertThat(UnsignedByte.of(0).hexValue()).isEqualTo("00");
    assertThat(UnsignedByte.of(127).hexValue()).isEqualTo("7F");
    assertThat(UnsignedByte.of(128).hexValue()).isEqualTo("80");
    assertThat(UnsignedByte.of(255).hexValue()).isEqualTo("FF");
    assertThat(UnsignedByte.of((byte) 15, (byte) 15).hexValue()).isEqualTo("FF");
  }

  @Test
  void isNthBitSetAllZero() {
    UnsignedByte value = UnsignedByte.of(0);
    for (int i = 1; i <= 8; i++) {
      assertThat(value.isNthBitSet(i)).isFalse();
    }
  }

  @Test
  void isNthBitSetAllSet() {
    UnsignedByte value = UnsignedByte.of(new BigInteger("11111111", 2).intValue());
    for (int i = 1; i <= 8; i++) {
      assertThat(value.isNthBitSet(i)).isTrue();
    }
  }

  @Test
  void isNthBitSetEveryOther() {
    UnsignedByte value = UnsignedByte.of(new BigInteger("10101010", 2).intValue());
    assertThat(value.isNthBitSet(1)).isTrue();
    assertThat(value.isNthBitSet(2)).isFalse();
    assertThat(value.isNthBitSet(3)).isTrue();
    assertThat(value.isNthBitSet(4)).isFalse();
    assertThat(value.isNthBitSet(5)).isTrue();
    assertThat(value.isNthBitSet(6)).isFalse();
    assertThat(value.isNthBitSet(7)).isTrue();
    assertThat(value.isNthBitSet(8)).isFalse();
  }

  @Test
  void intValue() {
    assertThat(UnsignedByte.of(0x00).asInt()).isEqualTo(0);
    assertThat(UnsignedByte.of(0x0F).asInt()).isEqualTo(15);
    assertThat(UnsignedByte.of(0xFF).asInt()).isEqualTo(255);

    assertThat(UnsignedByte.of(0).asInt()).isEqualTo(0);
    assertThat(UnsignedByte.of(15).asInt()).isEqualTo(15);
    assertThat(UnsignedByte.of(255).asInt()).isEqualTo(255);
  }

  @Test
  void destroy() {
    UnsignedByte ub = UnsignedByte.of(0x00);
    ub.destroy();
    assertThat(ub.isDestroyed()).isTrue();
    assertThat(ub.asByte()).isEqualTo((byte) 0);
  }

}

