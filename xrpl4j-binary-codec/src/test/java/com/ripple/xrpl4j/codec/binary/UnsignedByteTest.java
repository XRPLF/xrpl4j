package com.ripple.xrpl4j.codec.binary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UnsignedByteTest {

  @Test
  void hexValue() {
    assertThat(UnsignedByte.of(127).hexValue()).isEqualTo("7F");
    assertThat(UnsignedByte.of(128).hexValue()).isEqualTo("80");
    assertThat(UnsignedByte.of(255).hexValue()).isEqualTo("FF");
    assertThat(UnsignedByte.of((byte) 15, (byte) 15).hexValue()).isEqualTo("FF");
  }
}