package com.ripple.xrpl4j.codec.binary.types;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AmountTypeTest {

  private final AmountType codec = new AmountType();

  @Test
  void decode() {
    assertThat(codec.fromHex("4000000000000064").toJSON().asText()).isEqualTo("100");
    assertThat(codec.fromHex("416345785D8A0000").toJSON().asText()).isEqualTo("100000000000000000");
  }

  @Test
  void encode() {
    assertThat(codec.fromJSON("100").toHex()).isEqualTo("4000000000000064");
    assertThat(codec.fromJSON("100000000000000000").toHex()).isEqualTo("416345785D8A0000");
  }

  @Test
  void encodeOutOfBounds() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> codec.fromJSON("416345785D8A0001"));
  }

}
