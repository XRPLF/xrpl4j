package com.ripple.xrpl4j.codec.binary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ripple.xrpl4j.codec.binary.definitions.DefaultDefinitionsProvider;
import com.ripple.xrpl4j.codec.binary.definitions.DefinitionsProvider;
import com.ripple.xrpl4j.codec.fixtures.FixtureUtils;
import com.ripple.xrpl4j.codec.fixtures.data.FieldTest;
import com.ripple.xrpl4j.codec.fixtures.data.ValueTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

class FieldCodecTest {

  private static List<FieldTest> fieldTests;

  private FieldCodec fieldCodec;

  @BeforeEach
  public void loadFixtures() throws IOException {
    ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    fieldCodec = new FieldCodec(new DefaultDefinitionsProvider(objectMapper).get(), objectMapper);
    fieldTests = FixtureUtils.getDataDrivenFixtures().fieldTests();
    assertThat(fieldTests).hasSize(125);
  }


  @Test
  void getFieldId() {
    fieldTests.forEach(fieldTest ->
        assertThat(fieldCodec.getFieldId(fieldTest.name()))
            .isEqualTo(FieldId.builder().typeCode(fieldTest.type()).fieldCode(fieldTest.nthOfType()).build()));
  }

  @Test
  void encode() {
    fieldTests.forEach(fieldTest ->
        assertThat(fieldCodec.encode(fieldTest.name())).isEqualTo(fieldTest.expectedHex()));
  }

  @Test
  void encodeFieldId() {
    assertThat(fieldCodec.encode(FieldId.builder().fieldCode(1).typeCode(1).build())).isEqualTo("11");
    assertThat(fieldCodec.encode(FieldId.builder().fieldCode(15).typeCode(15).build())).isEqualTo("FF");
    assertThat(fieldCodec.encode(FieldId.builder().fieldCode(1).typeCode(16).build())).isEqualTo("0110");
    assertThat(fieldCodec.encode(FieldId.builder().fieldCode(16).typeCode(16).build())).isEqualTo("001010");
  }

}