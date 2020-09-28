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

  private final UIntCodec codec = new UIntCodec(8);

  @Test
  void decode() {
    assertThat(codec.decode("00")).isEqualTo("0");
    assertThat(codec.decode("0F")).isEqualTo("15");
    assertThat(codec.decode("FF")).isEqualTo("255");
  }

  @Test
  void encode() {
    assertThat(codec.encode("0")).isEqualTo("00");
    assertThat(codec.encode("00")).isEqualTo("00");
    assertThat(codec.encode("15")).isEqualTo("0F");
    assertThat(codec.encode("255")).isEqualTo("FF");
  }

  @Test
  void decodeOutOfBounds() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> codec.decode("100"));
  }

  @Test
  void encodeOutOfBounds() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> codec.encode("256"));
  }

}