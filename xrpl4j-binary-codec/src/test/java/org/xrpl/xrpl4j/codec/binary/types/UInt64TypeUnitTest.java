package org.xrpl.xrpl4j.codec.binary.types;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UInt64TypeUnitTest {
  private final UInt64Type codec = new UInt64Type();
  private static UnsignedLong maxUint64 = UnsignedLong.valueOf("FFFFFFFFFFFFFFFF", 16);

  @Test
  void decode() {
    assertThat(codec.fromHex("0000000000000000").valueOf()).isEqualTo(UnsignedLong.valueOf(0));
    assertThat(codec.fromHex("000000000000000F").valueOf()).isEqualTo(UnsignedLong.valueOf(15));
    assertThat(codec.fromHex("00000000FFFFFFFF").valueOf()).isEqualTo(UnsignedLong.valueOf(4294967295L));
    assertThat(codec.fromHex("FFFFFFFFFFFFFFFF").valueOf()).isEqualTo(maxUint64);
  }

  @Test
  void encode() {
    assertThat(codec.fromJson("0").toHex()).isEqualTo("0000000000000000");
    assertThat(codec.fromJson("15").toHex()).isEqualTo("000000000000000F");
    assertThat(codec.fromJson("65535").toHex()).isEqualTo("000000000000FFFF");
    assertThat(codec.fromJson("4294967295").toHex()).isEqualTo("00000000FFFFFFFF");
    assertThat(codec.fromJson(maxUint64.toString()).toHex()).isEqualTo("FFFFFFFFFFFFFFFF");
  }

  @Test
  void encodeOutOfBounds() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> codec.fromJson("18446744073709551616"));
  }
}
