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

import com.google.common.base.Strings;
import org.junit.jupiter.api.Test;

class CurrencyTypeTest {

  public static final char DOUBLE_QUOTE = '"';
  private final CurrencyType codec = new CurrencyType();

  @Test
  void decodeIso3() {
    assertThat(codec.fromHex("0000000000000000000000000000000000000000").toJson().asText()).isEqualTo("XRP");
    assertThat(codec.fromHex("0000000000000000000000005553440000000000").toJson().asText()).isEqualTo("USD");
    assertThat(codec.fromHex("0000000000000000000000007853440000000000").toJson().asText()).isEqualTo("xSD");
  }

  @Test
  void encodeIso3() {
    assertThat(codec.fromJson(DOUBLE_QUOTE + "XRP" + DOUBLE_QUOTE).toHex())
      .isEqualTo("0000000000000000000000000000000000000000");
    assertThat(codec.fromJson(DOUBLE_QUOTE + "USD" + DOUBLE_QUOTE).toHex())
      .isEqualTo("0000000000000000000000005553440000000000");
    assertThat(codec.fromJson(DOUBLE_QUOTE + "xSD" + DOUBLE_QUOTE).toHex())
      .isEqualTo("0000000000000000000000007853440000000000");
  }

  @Test
  void decodeCustom() {
    String customCode = Strings.repeat("11", 20);
    assertThat(codec.fromHex(customCode).toJson().asText()).isEqualTo(customCode);
  }

  @Test
  void encodeCustom() {
    String customCode = Strings.repeat("11", 20);
    assertThat(codec.fromJson(DOUBLE_QUOTE + customCode + DOUBLE_QUOTE).toHex())
      .isEqualTo(customCode);
  }

}
