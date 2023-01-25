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

/**
 * Unit tests for {@link STArrayType}.
 */
@SuppressWarnings("AbbreviationAsWordInName")
class STArrayTypeTest {

  public static final String MEMO =
    "{\"Memo\":" +
      "{\"MemoType\":\"687474703A2F2F6578616D706C652E636F6D2F6D656D6F2F67656E65726963\"," +
      "\"MemoData\":\"72656E74\"}}";
  public static final String MEMO_HEX =
    "EA7C1F687474703A2F2F6578616D706C652E636F6D2F6D656D6F2F67656E657269637D0472656E74E1";
  private static final String JSON = "[" + MEMO + "," + MEMO + "]";
  private static final String HEX = MEMO_HEX + MEMO_HEX + STArrayType.ARRAY_END_MARKER_HEX;
  private final STArrayType codec = new STArrayType();

  @Test
  void decode() {
    assertThat(codec.fromHex(HEX).toJson().toString()).isEqualTo(JSON);
  }

  @Test
  void encode() {
    assertThat(codec.fromJson(JSON).toHex()).isEqualTo(HEX);
  }

}
