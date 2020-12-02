package com.ripple.xrpl4j.codec.binary.types;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Strings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HashTypeTest {

  public static final char DOUBLE_QUOTE = '"';
  private final Hash128Type codec128 = new Hash128Type();
  private final Hash160Type codec160 = new Hash160Type();
  private final Hash256Type codec256 = new Hash256Type();

  @Test
  void decode() {
    assertThat(codec128.fromHex(bytes(16)).toHex()).isEqualTo(bytes(16));
    assertThat(codec160.fromHex(bytes(20)).toHex()).isEqualTo(bytes(20));
    assertThat(codec256.fromHex(bytes(32)).toHex()).isEqualTo(bytes(32));
  }

  @Test
  void encode() {
    assertThat(codec128.fromJSON(DOUBLE_QUOTE + bytes(16) + DOUBLE_QUOTE).toHex()).isEqualTo(bytes(16));
    assertThat(codec160.fromJSON(DOUBLE_QUOTE + bytes(20) + DOUBLE_QUOTE).toHex()).isEqualTo(bytes(20));
    assertThat(codec256.fromJSON(DOUBLE_QUOTE + bytes(32) + DOUBLE_QUOTE).toHex()).isEqualTo(bytes(32));
  }

  @Test
  void encodeOutOfBounds() {
    Assertions.assertThrows(IllegalArgumentException.class,
      () -> codec128.fromJSON(bytes(20)));
  }

  private String bytes(int size) {
    return Strings.repeat("0F", size);
  }

}
