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

import org.junit.jupiter.api.Test;

class AccountIdTypeTest {

  public static final char DOUBLE_QUOTE = '"';
  private static final AccountIdType codec = new AccountIdType();

  @Test
  void decode() {
    assertThat(codec.fromHex("5E7B112523F68D2F5E879DB4EAC51C6698A69304").toJson().asText())
      .isEqualTo("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59");
  }

  @Test
  void encode() {
    assertThat(codec.fromJson(DOUBLE_QUOTE + "r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59" + DOUBLE_QUOTE).toHex())
      .isEqualTo("5E7B112523F68D2F5E879DB4EAC51C6698A69304");
  }

}
