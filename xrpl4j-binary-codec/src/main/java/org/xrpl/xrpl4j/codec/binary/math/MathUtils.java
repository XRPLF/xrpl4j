package org.xrpl.xrpl4j.codec.binary.math;

import com.google.common.base.Strings;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

/**
 * Utility functions for XRPL-related math operations.
 */
public class MathUtils {

  /**
   * Get the exponent value for {@code value}.
   *
   * @param value A {@link BigDecimal} to obtain the exponent from.
   *
   * @return An int representing the exponent value of {@code value}.
   */
  public static int getExponent(BigDecimal value) {
    return value.precision() - value.scale() - 1;
  }

  /**
   * XRPL amounts are right-padded with 0s, so both 1, 10, 100 are converted to 10000000000000000, which, when combined
   * with the exponent field can be used to reconstruct the actual decimal value. The resulting value will have {@code
   * expectedDigits}. Examples:
   * <ul>
   *   <li>{@code toPaddedBigInteger(1, 4)} yields 1000</li>
   *   <li>{@code toPaddedBigInteger(1.23, 4)} yields 1230</li>
   *   <li>{@code toPaddedBigInteger(0.1234, 4)} yields 1234</li>
   *   <li>{@code toPaddedBigInteger(0.1234, 3)} throws an IllegalArgumentException as the numberToPad cannot fit in
   *   3 digits.</li>
   * </ul>
   *
   * @param numberToPad    A {@link BigDecimal} to pad.
   * @param expectedDigits An int representing the total number of digits that {@code numberToPad} is expected to have.
   *
   * @return A {@link BigInteger} that is correctly padded.
   * @throws IllegalArgumentException if {@code numberToPad} does not have a total digit count equal to {@code
   *                                  expectedDigits}.
   */
  public static BigInteger toPaddedBigInteger(final BigDecimal numberToPad, int expectedDigits) {
    Objects.requireNonNull(numberToPad);

    String unscaled = numberToPad.abs().stripTrailingZeros().unscaledValue().toString();
    if (unscaled.length() > expectedDigits) {
      throw new IllegalArgumentException(numberToPad + " has more than " + expectedDigits + " digits");
    }
    return new BigInteger(Strings.padEnd(unscaled, expectedDigits, '0'));
  }

}
