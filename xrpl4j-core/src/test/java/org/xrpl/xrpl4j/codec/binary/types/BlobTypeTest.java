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
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.base.Strings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link BlobType}.
 */
class BlobTypeTest {

  public static final char DOUBLE_QUOTE = '"';
  private final BlobType codec = new BlobType();

  @Test
  void decode() {
    int width = 1;
    assertThat(codec.fromHex(bytes(width), width).toHex()).isEqualTo(bytes(width));
    width = 16;
    assertThat(codec.fromHex(bytes(width), width).toHex()).isEqualTo(bytes(width));
    width = 32;
    assertThat(codec.fromHex(bytes(width), width).toHex()).isEqualTo(bytes(width));
    width = 33;
    assertThat(codec.fromHex(bytes(width), width).toHex()).isEqualTo(bytes(width));
    width = 64;
    assertThat(codec.fromHex(bytes(width), width).toHex()).isEqualTo(bytes(width));
    width = 128;
    assertThat(codec.fromHex(bytes(width), width).toHex()).isEqualTo(bytes(width));
    width = 256;
    assertThat(codec.fromHex(bytes(width), width).toHex()).isEqualTo(bytes(width));
  }

  // TODO: Make empty buffers work
  @Test
  void decodeEmpty() {
    assertThrows(IndexOutOfBoundsException.class, () -> {
      int width = 0;
      assertThat(codec.fromHex(bytes(width), width).toHex()).isEqualTo(bytes(width));
    });
  }

  @Test
  void encode() {
    assertThat(codec.fromJson(DOUBLE_QUOTE + bytes(16) + DOUBLE_QUOTE).toHex()).isEqualTo(bytes(16));
  }

  private String bytes(int size) {
    return Strings.repeat("0F", size);
  }

}
