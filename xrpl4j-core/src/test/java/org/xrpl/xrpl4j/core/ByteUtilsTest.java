package org.xrpl.xrpl4j.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

/**
 * Unit tests for {@link ByteUtils}.
 */
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
  public void toByteArrayFromBigIntSingleByteMinValue() {
    assertThat(ByteUtils.toByteArray(BigInteger.ZERO, 1)).isEqualTo(new byte[] {0});
  }

  @Test
  public void toByteArrayFromBigIntPadded() {
    assertThat(ByteUtils.toByteArray(BigInteger.ONE, 2)).isEqualTo(new byte[] {0, 1});
  }

  @Test
  public void toByteArrayFromBigIntSingleByteMaxValue() {
    assertThat(ByteUtils.toByteArray(BigInteger.valueOf(15), 1)[0]).isEqualTo((byte) 15);
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

  @Test
  void toUnsignedLong() {
    UnsignedByteArray unsignedByteArray = UnsignedByteArray.fromHex("0F1234FF");
    UnsignedLong unsignedLong = ByteUtils.toUnsignedLong(unsignedByteArray);
    assertThat(unsignedLong).isEqualTo(UnsignedLong.valueOf(252851455));
  }

  @Test
  void paddedWithNoLengthAndEvenNibbles() {
    String hex = "ABCDEF1234";
    String padded = ByteUtils.padded(hex);
    assertThat(padded).isEqualTo(hex);
  }

  @Test
  void paddedWithNoLengthAndOddNibbles() {
    String hex = "ABCDEF123";
    String padded = ByteUtils.padded(hex);
    assertThat(padded).isEqualTo("0" + hex);
  }

  @Test
  void paddedWithLengthAndEvenNibbles() {
    String hex = "ABCDEF1234";
    String padded = ByteUtils.padded(hex, 12);
    assertThat(padded).isEqualTo("00" + hex);
  }

  @Test
  void paddedWithLengthAndOddNibbles() {
    String hex = "ABCDEF123";
    String padded = ByteUtils.padded(hex, 12);
    assertThat(padded).isEqualTo("000" + hex);
  }
}
