package org.xrpl.xrpl4j.codec.addresses;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

public class UnsignedByteTest {

  @Test
  public void hexValue() {
    assertThat(UnsignedByte.of(0).hexValue()).isEqualTo("00");
    assertThat(UnsignedByte.of(127).hexValue()).isEqualTo("7F");
    assertThat(UnsignedByte.of(128).hexValue()).isEqualTo("80");
    assertThat(UnsignedByte.of(255).hexValue()).isEqualTo("FF");
    assertThat(UnsignedByte.of((byte) 15, (byte) 15).hexValue()).isEqualTo("FF");
  }

  @Test
  public void isNthBitSetAllZero() {
    UnsignedByte value = UnsignedByte.of(0);
    for (int i = 1; i <= 8; i++) {
      assertThat(value.isNthBitSet(i)).isFalse();
    }
  }

  @Test
  public void isNthBitSetAllSet() {
    UnsignedByte value = UnsignedByte.of(new BigInteger("11111111", 2).intValue());
    for (int i = 1; i <= 8; i++) {
      assertThat(value.isNthBitSet(i)).isTrue();
    }
  }

  @Test
  public void isNthBitSetEveryOther() {
    UnsignedByte value = UnsignedByte.of(new BigInteger("10101010", 2).intValue());
    assertThat(value.isNthBitSet(1)).isTrue();
    assertThat(value.isNthBitSet(2)).isFalse();
    assertThat(value.isNthBitSet(3)).isTrue();
    assertThat(value.isNthBitSet(4)).isFalse();
    assertThat(value.isNthBitSet(5)).isTrue();
    assertThat(value.isNthBitSet(6)).isFalse();
    assertThat(value.isNthBitSet(7)).isTrue();
    assertThat(value.isNthBitSet(8)).isFalse();
  }

  @Test
  public void intValue() {
    assertThat(UnsignedByte.of(0x00).asInt()).isEqualTo(0);
    assertThat(UnsignedByte.of(0x0F).asInt()).isEqualTo(15);
    assertThat(UnsignedByte.of(0xFF).asInt()).isEqualTo(255);

    assertThat(UnsignedByte.of(0).asInt()).isEqualTo(0);
    assertThat(UnsignedByte.of(15).asInt()).isEqualTo(15);
    assertThat(UnsignedByte.of(255).asInt()).isEqualTo(255);
  }

}

