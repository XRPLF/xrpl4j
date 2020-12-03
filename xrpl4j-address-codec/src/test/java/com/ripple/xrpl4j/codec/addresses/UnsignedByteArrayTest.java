package com.ripple.xrpl4j.codec.addresses;

import static com.ripple.xrpl4j.codec.addresses.UnsignedByteArray.of;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;


public class UnsignedByteArrayTest {

  static byte MAX_BYTE = (byte) 255;

  @Test
  public void ofByteArray() {

    assertThat(of(new byte[] {0}).hexValue()).isEqualTo("00");
    assertThat(of(new byte[] {MAX_BYTE}).hexValue()).isEqualTo("FF");
    assertThat(of(new byte[] {0, MAX_BYTE}).hexValue()).isEqualTo("00FF");
    assertThat(of(new byte[] {MAX_BYTE, 0}).hexValue()).isEqualTo("FF00");
    assertThat(of(new byte[] {MAX_BYTE, MAX_BYTE}).hexValue()).isEqualTo("FFFF");
  }

  @Test
  public void ofUnsignedByteArray() {
    assertThat(of(UnsignedByte.of(0)).hexValue()).isEqualTo("00");
    assertThat(of(UnsignedByte.of(MAX_BYTE)).hexValue()).isEqualTo("FF");
    assertThat(of(UnsignedByte.of(0), UnsignedByte.of((MAX_BYTE))).hexValue()).isEqualTo("00FF");
    assertThat(of(UnsignedByte.of(MAX_BYTE), UnsignedByte.of((0))).hexValue()).isEqualTo("FF00");
    assertThat(of(UnsignedByte.of(MAX_BYTE), UnsignedByte.of((MAX_BYTE))).hexValue()).isEqualTo("FFFF");
  }

}