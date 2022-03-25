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
import org.xrpl.xrpl4j.codec.binary.serdes.BinaryParser;

class Vector256TypeTest {

  public static final String VALUE1 =
    "42426C4D4F1009EE67080A9B7965B44656D7714D104A72F9B4369F97ABF044EE";
  public static final String VALUE2 =
    "4C97EBA926031A7CF7D7B36FDE3ED66DDA5421192D63DE53FFB46E43B9DC8373";
  private static final String JSON = "[\"" + VALUE1 + "\",\"" + VALUE2 + "\"]";
  private static final String HEX = VALUE1 + VALUE2;
  private final Vector256Type codec = new Vector256Type();

  @Test
  void decode() {
    BinaryParser parser = new BinaryParser(HEX);
    assertThat(codec.fromParser(parser, 32));
  }

  @Test
  void encode() {
    assertThat(codec.fromJson(JSON).toHex()).isEqualTo(HEX);
  }

}
