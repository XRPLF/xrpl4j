package com.ripple.xrpl4j.codec.binary;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

class ByteUtilsTest {

  @Test
  void toByteArraySingleByteMinValue() {
    assertThat(ByteUtils.toByteArray(0, 1)).isEqualTo(new byte[]{ 0 });
  }

  @Test
  void toByteArrayPadded() {
    assertThat(ByteUtils.toByteArray(1, 2)).isEqualTo(new byte[]{ 0, 1 });
  }

  @Test
  void toByteArraySingleByteMaxValue() {
    assertThat(ByteUtils.toByteArray(15, 1)[0]).isEqualTo((byte) 15);
  }

  @Test
  void checkSizeMaxValue() {
    ByteUtils.checkSize(4, BigInteger.valueOf(15));
  }

  @Test
  void checkSizeExceedMaxValue() {
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> ByteUtils.checkSize(4, BigInteger.valueOf(17)));
  }

  @Test
  void checkSizeMinValue() {
    ByteUtils.checkSize(4, BigInteger.valueOf(0));
  }

}