package com.ripple.xrpl4j.codec.binary.types;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UIntTypeTest {

  private final UInt16Type codec = new UInt16Type();

  @Test
  void decode() {
    assertThat(codec.fromHex("0000").valueOf()).isEqualTo(UnsignedLong.valueOf(0));
    assertThat(codec.fromHex("000F").valueOf()).isEqualTo(UnsignedLong.valueOf(15));
    assertThat(codec.fromHex("FFFF").valueOf()).isEqualTo(UnsignedLong.valueOf(65535));
  }

  @Test
  void encode() throws JsonProcessingException {
    assertThat(codec.fromJSON("0").toHex()).isEqualTo("0000");
    assertThat(codec.fromJSON("15").toHex()).isEqualTo("000F");
    assertThat(codec.fromJSON("65535").toHex()).isEqualTo("FFFF");
  }

  @Test
  void encodeOutOfBounds() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> codec.fromJSON("65536"));
  }

}
