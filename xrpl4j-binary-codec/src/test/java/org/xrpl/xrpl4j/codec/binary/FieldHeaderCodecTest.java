package org.xrpl.xrpl4j.codec.binary;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.binary.definitions.DefaultDefinitionsProvider;
import org.xrpl.xrpl4j.codec.fixtures.FixtureUtils;
import org.xrpl.xrpl4j.codec.fixtures.data.FieldTest;

import java.io.IOException;
import java.util.List;

class FieldHeaderCodecTest {

  private static List<FieldTest> fieldTests;

  private FieldHeaderCodec fieldHeaderCodec;

  @BeforeEach
  public void loadFixtures() throws IOException {
    ObjectMapper objectMapper = BinaryCodecObjectMapperFactory.getObjectMapper();
    fieldHeaderCodec = new FieldHeaderCodec(new DefaultDefinitionsProvider(objectMapper).get(), objectMapper);
    fieldTests = FixtureUtils.getDataDrivenFixtures().fieldTests();
    assertThat(fieldTests).hasSize(123);
  }


  @Test
  void getFieldId() {
    fieldTests.forEach(fieldTest ->
      assertThat(fieldHeaderCodec.getFieldId(fieldTest.name()))
        .isEqualTo(FieldHeader.builder().typeCode(fieldTest.type()).fieldCode(fieldTest.nthOfType()).build()));
  }

  @Test
  void encode() {
    fieldTests.forEach(fieldTest ->
      assertThat(fieldHeaderCodec.encode(fieldTest.name())).isEqualTo(fieldTest.expectedHex()));
  }

  @Test
  void decode() {
    fieldTests.forEach(fieldTest ->
      assertThat(fieldHeaderCodec.decode(fieldTest.expectedHex())).isEqualTo(fieldTest.name()));
  }

  @Test
  void encodeFieldId() {
    assertThat(fieldHeaderCodec.encode(FieldHeader.builder().fieldCode(1).typeCode(1).build())).isEqualTo("11");
    assertThat(fieldHeaderCodec.encode(FieldHeader.builder().fieldCode(15).typeCode(15).build())).isEqualTo("FF");
    assertThat(fieldHeaderCodec.encode(FieldHeader.builder().fieldCode(1).typeCode(16).build())).isEqualTo("0110");
    assertThat(fieldHeaderCodec.encode(FieldHeader.builder().fieldCode(255).typeCode(255).build())).isEqualTo("00FFFF");
  }

  @Test
  void decodeFieldId() {
    assertThat(fieldHeaderCodec.decodeFieldId("11")).isEqualTo(FieldHeader.builder().fieldCode(1).typeCode(1).build());
    assertThat(fieldHeaderCodec.decodeFieldId("FF"))
      .isEqualTo(FieldHeader.builder().fieldCode(15).typeCode(15).build());
    assertThat(fieldHeaderCodec.decodeFieldId("0110"))
      .isEqualTo(FieldHeader.builder().fieldCode(1).typeCode(16).build());
    assertThat(fieldHeaderCodec.decodeFieldId("00FFFF"))
      .isEqualTo(FieldHeader.builder().fieldCode(255).typeCode(255).build());
  }

}
