package org.xrpl.xrpl4j.codec.addresses;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

public class ByteUtilsTest {

  @Test
  public void toByteArraySingleByteMinValue() {
    assertThat(ByteUtils.toByteArray(0, 1)).isEqualTo(new byte[] {0});
  }

  @Test
  public void toByteArrayPadded() {
    assertThat(ByteUtils.toByteArray(1, 2)).isEqualTo(new byte[] {0, 1});
  }

  @Test
  public void toByteArraySingleByteMaxValue() {
    assertThat(ByteUtils.toByteArray(15, 1)[0]).isEqualTo((byte) 15);
  }

  @Test
  public void checkSizeMaxValue() {
    ByteUtils.checkSize(4, BigInteger.valueOf(15));
  }

  @Test
  public void checkSizeExceedMaxValue() {
    assertThrows(
      IllegalArgumentException.class,
      () -> ByteUtils.checkSize(4, BigInteger.valueOf(17))
    );
  }

  @Test
  public void checkSizeMinValue() {
    ByteUtils.checkSize(4, BigInteger.valueOf(0));
  }

}
