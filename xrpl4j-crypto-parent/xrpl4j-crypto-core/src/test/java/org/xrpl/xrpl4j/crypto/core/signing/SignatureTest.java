package org.xrpl.xrpl4j.crypto.core.signing;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

import java.util.Arrays;

/**
 * Unit tests for {@link Signature}.
 */
class SignatureTest {

  private Signature signature;

  @BeforeEach
  void setUp() {
    signature = Signature.builder()
      .value(UnsignedByteArray.of(new byte[32]))
      .build();
  }

  @Test
  void value() {
    assertThat(Arrays.equals(signature.value().toByteArray(), new byte[32])).isTrue();
  }

  @Test
  void base16Value() {
    assertThat(signature.base16Value()).isEqualTo("0000000000000000000000000000000000000000000000000000000000000000");
  }

  @Test
  void hexValue() {
    assertThat(signature.base16Value()).isEqualTo(signature.hexValue());
    assertThat(signature.base16Value()).isEqualTo("0000000000000000000000000000000000000000000000000000000000000000");
  }
}