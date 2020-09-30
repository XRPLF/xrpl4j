package com.ripple.xrpl4j.codec.binary;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ripple.xrpl4j.codec.binary.definitions.DefaultDefinitionsProvider;
import com.ripple.xrpl4j.codec.fixtures.FixtureUtils;
import com.ripple.xrpl4j.codec.fixtures.data.FieldTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

class FieldIdCodecTest {

  private static List<FieldTest> fieldTests;

  private FieldIdCodec fieldIdCodec;

  @BeforeEach
  public void loadFixtures() throws IOException {
    ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    fieldIdCodec = new FieldIdCodec(new DefaultDefinitionsProvider(objectMapper).get(), objectMapper);
    fieldTests = FixtureUtils.getDataDrivenFixtures().fieldTests();
    assertThat(fieldTests).hasSize(125);
  }


  @Test
  void getFieldId() {
    fieldTests.forEach(fieldTest ->
        assertThat(fieldIdCodec.getFieldId(fieldTest.name()))
            .isEqualTo(FieldId.builder().typeCode(fieldTest.type()).fieldCode(fieldTest.nthOfType()).build()));
  }

  @Test
  void encode() {
    fieldTests.forEach(fieldTest ->
        assertThat(fieldIdCodec.encode(fieldTest.name())).isEqualTo(fieldTest.expectedHex()));
  }

  @Test
  void decode() {
    fieldTests.forEach(fieldTest ->
        assertThat(fieldIdCodec.decode(fieldTest.expectedHex())).isEqualTo(fieldTest.name()));
  }

  @Test
  void encodeFieldId() {
    assertThat(fieldIdCodec.encode(FieldId.builder().fieldCode(1).typeCode(1).build())).isEqualTo("11");
    assertThat(fieldIdCodec.encode(FieldId.builder().fieldCode(15).typeCode(15).build())).isEqualTo("FF");
    assertThat(fieldIdCodec.encode(FieldId.builder().fieldCode(1).typeCode(16).build())).isEqualTo("0110");
    assertThat(fieldIdCodec.encode(FieldId.builder().fieldCode(255).typeCode(255).build())).isEqualTo("00FFFF");
  }

  @Test
  void decodeFieldId() {
    assertThat(fieldIdCodec.decodeFieldId("11")).isEqualTo(FieldId.builder().fieldCode(1).typeCode(1).build());
    assertThat(fieldIdCodec.decodeFieldId("FF")).isEqualTo(FieldId.builder().fieldCode(15).typeCode(15).build());
    assertThat(fieldIdCodec.decodeFieldId("0110")).isEqualTo(FieldId.builder().fieldCode(1).typeCode(16).build());
    assertThat(fieldIdCodec.decodeFieldId("00FFFF")).isEqualTo(FieldId.builder().fieldCode(255).typeCode(255).build());
  }

}