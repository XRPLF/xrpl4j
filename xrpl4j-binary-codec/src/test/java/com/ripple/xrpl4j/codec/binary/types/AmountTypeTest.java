package com.ripple.xrpl4j.codec.binary.types;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AmountTypeTest {

  private final AmountType codec = new AmountType();

  @Test
  void decodeXrpAmount() {
    assertThat(codec.fromHex("4000000000000064").toJSON().asText()).isEqualTo("100");
    assertThat(codec.fromHex("416345785D8A0000").toJSON().asText()).isEqualTo("100000000000000000");
  }

  @Test
  void encodeXrpAmount() {
    assertThat(codec.fromJSON("100").toHex()).isEqualTo("4000000000000064");
    assertThat(codec.fromJSON("100000000000000000").toHex()).isEqualTo("416345785D8A0000");
  }

  @Test
  void encodeOutOfBounds() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> codec.fromJSON("416345785D8A0001"));
  }

  @Test
  void encodeCurrencyAmount() {
    assertThat(codec.fromJSON("{\"value\":\"0\",\"currency\":\"USD\",\"issuer\":\"rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw\"}")
        .toHex()).isEqualTo("800000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");

    assertThat(codec.fromJSON("{\"value\":\"1\",\"currency\":\"USD\",\"issuer\":\"rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw\"}")
        .toHex()).isEqualTo("D4838D7EA4C6800000000000000000000000000000000000000000000000000000000000000000000000000000000000");

    assertThat(codec.fromJSON("{\"value\":\"2\",\"currency\":\"USD\",\"issuer\":\"rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw\"}")
        .toHex()).isEqualTo("D4871AFD498D000000000000000000000000000000000000000000000000000000000000000000000000000000000000");

    assertThat(codec.fromJSON("{\"value\":\"-2\",\"currency\":\"USD\",\"issuer\":\"rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw\"}")
        .toHex()).isEqualTo("94871AFD498D000000000000000000000000000000000000000000000000000000000000000000000000000000000000");

    assertThat(codec.fromJSON("{\"value\":\"2.1\",\"currency\":\"USD\",\"issuer\":\"rDgZZ3wyprx4ZqrGQUkquE9Fs2Xs8XBcdw\"}")
        .toHex()).isEqualTo("D48775F05A07400000000000000000000000000000000000000000000000000000000000000000000000000000000000");
  }

  @Test
  void decodeCurrencyAmount() {
    assertThat(
        codec.fromHex("D48775F05A07400000000000000000000000000000000000000000000000000000000000000000000000000000000000")
        .toJSON().toString())
        .isEqualTo("{\"value\":\"2.1\",\"currency\":\"\",\"issuer\":\"\"}");
  }

}
