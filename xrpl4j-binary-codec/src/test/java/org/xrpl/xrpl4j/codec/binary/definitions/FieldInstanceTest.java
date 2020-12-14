package org.xrpl.xrpl4j.codec.binary.definitions;

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