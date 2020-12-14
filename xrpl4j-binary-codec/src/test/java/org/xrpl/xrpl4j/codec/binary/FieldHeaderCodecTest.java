package org.xrpl.xrpl4j.codec.binary;

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
    assertThat(fieldTests).hasSize(125);
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
