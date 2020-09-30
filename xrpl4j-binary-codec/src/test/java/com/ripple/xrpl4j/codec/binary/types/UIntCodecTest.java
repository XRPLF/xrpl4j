package com.ripple.xrpl4j.codec.binary.types;

import static org.assertj.core.api.Assertions.assertThat;

import com.ripple.xrpl4j.codec.fixtures.FixtureUtils;
import com.ripple.xrpl4j.codec.fixtures.data.ValueTest;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

class UIntCodecTest {

  private final UIntCodec codec = new UIntCodec(16);

  @Test
  void decode() {
    assertThat(codec.decode("00")).isEqualTo("0");
    assertThat(codec.decode("0F")).isEqualTo("15");
    assertThat(codec.decode("FFFF")).isEqualTo("65535");
  }

  @Test
  void encode() {
    assertThat(codec.encode("0")).isEqualTo("0000");
    assertThat(codec.encode("00")).isEqualTo("0000");
    assertThat(codec.encode("15")).isEqualTo("000F");
    assertThat(codec.encode("65535")).isEqualTo("FFFF");
  }

  @Test
  void decodeOutOfBounds() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> codec.decode("10000"));
  }

  @Test
  void encodeOutOfBounds() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> codec.encode("65536"));
  }

}