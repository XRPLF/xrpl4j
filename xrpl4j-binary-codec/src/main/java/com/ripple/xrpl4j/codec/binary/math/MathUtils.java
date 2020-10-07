package com.ripple.xrpl4j.codec.binary.math;

import com.google.common.base.Strings;

import java.math.BigDecimal;
import java.math.BigInteger;

public class MathUtils {

  public static int getExponent(BigDecimal value) {
    return value.precision() - value.scale() - 1;
  }

  /**
   * XRPL amounts are right padded with 0s, so both 1, 10, 100 are converted to 10000000000000000, which,
   * when combined with the exponent field can be used to reconstruct the actual decimal value.
   * The resulting value will have {@code expectedDigits}.
   * Examples:
   * {@code toPaddedBigInteger(1, 4)} yields 1000
   * {@code toPaddedBigInteger(1.23, 4)} yields 1230
   * {@code toPaddedBigInteger(0.1234, 4)} yields 1234
   * {@code toPaddedBigInteger(0.1234, 3)} throws an IllegalArgumentException as the number cannot fit in 3 digits.
   *
   * @param number number to pad.
   * @param expectedDigits how many total digits the padded number should have.
   * @return
   */
  public static BigInteger toPaddedBigInteger(BigDecimal number, int expectedDigits) {
    String unscaled = number.abs().stripTrailingZeros().unscaledValue().toString();
    if (unscaled.length() > expectedDigits) {
      throw new IllegalArgumentException(number + " has more than " + expectedDigits + " digits");
    }
    return new BigInteger(Strings.padEnd(unscaled, expectedDigits, '0'));
  }

}
