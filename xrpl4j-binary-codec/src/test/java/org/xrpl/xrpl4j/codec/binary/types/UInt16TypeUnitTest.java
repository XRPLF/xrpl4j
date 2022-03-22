package org.xrpl.xrpl4j.codec.binary.types;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UInt16TypeUnitTest {

  private final UInt16Type codec = new UInt16Type();

  @Test
  void decode() {
    assertThat(codec.fromHex("0000").valueOf()).isEqualTo(UnsignedLong.valueOf(0));
    assertThat(codec.fromHex("000F").valueOf()).isEqualTo(UnsignedLong.valueOf(15));
    assertThat(codec.fromHex("FFFF").valueOf()).isEqualTo(UnsignedLong.valueOf(65535));
  }

  @Test
  void encode() {
    assertThat(codec.fromJson("0").toHex()).isEqualTo("0000");
    assertThat(codec.fromJson("15").toHex()).isEqualTo("000F");
    assertThat(codec.fromJson("65535").toHex()).isEqualTo("FFFF");
  }

  @Test
  void encodeOutOfBounds() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> codec.fromJson("65536"));
  }

}
