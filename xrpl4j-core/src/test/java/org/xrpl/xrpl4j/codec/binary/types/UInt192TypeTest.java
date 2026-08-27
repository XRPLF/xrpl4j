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

import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Test;

public class UInt192TypeTest {

  private final UInt192Type codec = new UInt192Type();

  @Test
  void decode() {
    assertThat(codec.fromHex("000000000000000000000000000000000000000000000000").toHex()).isEqualTo("000000000000000000000000000000000000000000000000");
    assertThat(codec.fromHex("00000000000000000000000000000000000000000000000F").toHex()).isEqualTo("00000000000000000000000000000000000000000000000F");
    assertThat(codec.fromHex("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF").toHex()).isEqualTo("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
  }

  @Test
  void encode() {
    assertThat(codec.fromJson(new TextNode("013411307C0D97D1EC6B2989138679ACDCB75C37CA30F0A6")).toHex()).isEqualTo("013411307C0D97D1EC6B2989138679ACDCB75C37CA30F0A6");
  }

}
