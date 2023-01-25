package org.xrpl.xrpl4j.codec.binary.definitions;

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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.binary.FieldHeader;

/**
 * Unit test for {@link FieldInstance}.
 */
class FieldInstanceTest {

  @Test
  void compareToWithHeaders() {
    FieldInstance smaller = FieldInstance.builder()
      .name("foo")
      .nth(65536)
      .type("type")
      .header(FieldHeader.builder()
        .fieldCode(0)
        .typeCode(65535)
        .build())
      .isSigningField(true)
      .isSerialized(true)
      .isVariableLengthEncoded(true)
      .build();

    FieldInstance bigger = FieldInstance.builder()
      .name("foo")
      .nth(65535)
      .type("type")
      .header(FieldHeader.builder()
        .fieldCode(1)
        .typeCode(65535)
        .build())
      .isSigningField(true)
      .isSerialized(true)
      .isVariableLengthEncoded(true)
      .build();

    assertThat(smaller.compareTo(smaller)).isEqualTo(0);
    assertThat(smaller.compareTo(bigger)).isEqualTo(-1);
    assertThat(bigger.compareTo(smaller)).isEqualTo(1);
  }

}
