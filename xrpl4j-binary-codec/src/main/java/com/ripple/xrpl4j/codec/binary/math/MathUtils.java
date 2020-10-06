package com.ripple.xrpl4j.codec.binary.math;

import com.google.common.base.Strings;

import java.math.BigDecimal;
import java.math.BigInteger;

public class MathUtils {

  public static int getExponent(BigDecimal value) {
    return value.precision() - value.scale() - 1;
  }

  public static BigInteger toPaddedBigInteger(BigDecimal number, int expectedDigits) {
    String unscaled = number.abs().stripTrailingZeros().unscaledValue().toString();
    if (unscaled.length() > expectedDigits) {
      throw new IllegalArgumentException(number + " has more than " + expectedDigits + " digits");
    }
    return new BigInteger(Strings.padEnd(unscaled, expectedDigits, '0'));
  }

}
