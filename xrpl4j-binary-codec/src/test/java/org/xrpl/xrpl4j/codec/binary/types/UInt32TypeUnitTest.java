package org.xrpl.xrpl4j.codec.binary.types;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UInt32TypeUnitTest {

  private final UInt32Type codec = new UInt32Type();

  @Test
  void decode() {
    assertThat(codec.fromHex("00000000").valueOf()).isEqualTo(UnsignedLong.valueOf(0));
    assertThat(codec.fromHex("0000000F").valueOf()).isEqualTo(UnsignedLong.valueOf(15));
    assertThat(codec.fromHex("FFFFFFFF").valueOf()).isEqualTo(UnsignedLong.valueOf(4294967295L));
  }

  @Test
  void encode() {
    assertThat(codec.fromJson("0").toHex()).isEqualTo("00000000");
    assertThat(codec.fromJson("15").toHex()).isEqualTo("0000000F");
    assertThat(codec.fromJson("65535").toHex()).isEqualTo("0000FFFF");
    assertThat(codec.fromJson("4294967295").toHex()).isEqualTo("FFFFFFFF");
  }

  @Test
  void encodeOutOfBounds() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> codec.fromJson("4294967296"));
  }

}