package org.xrpl.xrpl4j.codec.addresses;

import static org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray.of;

import org.assertj.core.api.Assertions;
import org.junit.Test;


public class UnsignedByteArrayTest {

  static byte MAX_BYTE = (byte) 255;

  @Test
  public void ofByteArray() {

    Assertions.assertThat(UnsignedByteArray.of(new byte[] {0}).hexValue()).isEqualTo("00");
    Assertions.assertThat(UnsignedByteArray.of(new byte[] {MAX_BYTE}).hexValue()).isEqualTo("FF");
    Assertions.assertThat(UnsignedByteArray.of(new byte[] {0, MAX_BYTE}).hexValue()).isEqualTo("00FF");
    Assertions.assertThat(UnsignedByteArray.of(new byte[] {MAX_BYTE, 0}).hexValue()).isEqualTo("FF00");
    Assertions.assertThat(UnsignedByteArray.of(new byte[] {MAX_BYTE, MAX_BYTE}).hexValue()).isEqualTo("FFFF");
  }

  @Test
  public void ofUnsignedByteArray() {
    Assertions.assertThat(of(UnsignedByte.of(0)).hexValue()).isEqualTo("00");
    Assertions.assertThat(of(UnsignedByte.of(MAX_BYTE)).hexValue()).isEqualTo("FF");
    Assertions.assertThat(of(UnsignedByte.of(0), UnsignedByte.of((MAX_BYTE))).hexValue()).isEqualTo("00FF");
    Assertions.assertThat(of(UnsignedByte.of(MAX_BYTE), UnsignedByte.of((0))).hexValue()).isEqualTo("FF00");
    Assertions.assertThat(of(UnsignedByte.of(MAX_BYTE), UnsignedByte.of((MAX_BYTE))).hexValue()).isEqualTo("FFFF");
  }

}
