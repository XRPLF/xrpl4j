package org.xrpl.xrpl4j.codec.binary.types;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: binary-codec
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.binary.serdes.BinaryParser;

/**
 * Unit tests for {@link NumberType}
 */
class NumberTypeTest {

  private final NumberType codec = new NumberType();

  @Test
  void positiveNormalValue() {
    String value = "99";
    NumberType num = codec.fromJson(new TextNode(value));
    assertThat(num.toJson().asText()).isEqualTo(value);
  }

  @Test
  void largePositiveValue() {
    String value = "10000000000";
    NumberType num = codec.fromJson(new TextNode(value));
    assertThat(num.toJson().asText()).isEqualTo(value);
  }

  @Test
  void veryLargePositiveValue() {
    // Scientific notation triggers when abs(value) >= 10^11
    String value = "100000000000";
    NumberType num = codec.fromJson(new TextNode(value));
    assertThat(num.toJson().asText()).isEqualTo("1e11");
  }


  @Test
  void negativeNormalValue() {
    String value = "-123";
    NumberType num = codec.fromJson(new TextNode(value));
    assertThat(num.toJson().asText()).isEqualTo(value);
  }

  @Test
  void largeNegativeValue() {
    // Scientific notation triggers when abs(value) >= 10^11
    String value = "-10000000000";
    NumberType num = codec.fromJson(new TextNode(value));
    assertThat(num.toJson().asText()).isEqualTo(value);
  }


  @Test
  void veryLargeNegativeValue() {
    // Scientific notation triggers when abs(value) >= 10^11
    String value = "-100000000000";
    NumberType num = codec.fromJson(new TextNode(value));
    assertThat(num.toJson().asText()).isEqualTo("-1e11");
  }

  @Test
  void verySmallPositiveValue() {
    // Scientific notation triggers when abs(value) < 10^-10
    String value = "0.00000000001";
    NumberType num = codec.fromJson(new TextNode(value));
    assertThat(num.toJson().asText()).isEqualTo("1e-11");
  }

  @Test
  void smallPositiveValue() {
    // Scientific notation triggers when abs(value) < 10^-10
    String value = "0.0001";
    NumberType num = codec.fromJson(new TextNode(value));
    assertThat(num.toJson().asText()).isEqualTo(value);
  }

  @Test
  void roundTripZero() {
    String value = "0";
    NumberType num = codec.fromJson(new TextNode(value));
    assertThat(num.toJson().asText()).isEqualTo("0");
  }

  @Test
  void roundTripDecimal() {
    String value = "123.456";
    NumberType num = codec.fromJson(new TextNode(value));
    assertThat(num.toJson().asText()).isEqualTo(value);
  }

  @Test
  void roundTripScientificNotationPositive() {
    String value = "1.23e5";
    NumberType num = codec.fromJson(new TextNode(value));
    // Scientific notation triggers when abs(value) >= 10^11
    assertThat(num.toJson().asText()).isEqualTo("123000");
  }

  @Test
  void roundTripScientificNotationNegative() {
    String value = "-4.56e-7";
    NumberType num = codec.fromJson(new TextNode(value));
    // Scientific notation triggers when abs(value) < 10^-10
    assertThat(num.toJson().asText()).isEqualTo("-0.000000456");
  }

  @Test
  void negativeMediumValue() {
    String value = "-987654321";
    NumberType num = codec.fromJson(new TextNode(value));
    assertThat(num.toJson().asText()).isEqualTo(value);
  }

  @Test
  void positiveMediumValue() {
    String value = "987654321";
    NumberType num = codec.fromJson(new TextNode(value));
    assertThat(num.toJson().asText()).isEqualTo(value);
  }

  @Test
  void roundTripViaParser() {
    String value = "123456.789";
    NumberType num = codec.fromJson(new TextNode(value));
    BinaryParser parser = new BinaryParser(num.toHex());
    NumberType parsedNum = codec.fromParser(parser);
    assertThat(parsedNum.toJson().asText()).isEqualTo(num.toJson().asText());
  }

  @Test
  void zeroViaParser() {
    String value = "0";
    NumberType num = codec.fromJson(new TextNode(value));
    BinaryParser parser = new BinaryParser(num.toHex());
    NumberType parsedNum = codec.fromParser(parser);
    assertThat(parsedNum.toJson().asText()).isEqualTo("0");
  }

  @Test
  void normalizationWithTrailingZeros() {
    String value = "123.45000";
    NumberType num = codec.fromJson(new TextNode(value));
    assertThat(num.toJson().asText()).isEqualTo("123.45");
  }

  @Test
  void normalizationWithLeadingZeros() {
    String value = "0000123.45";
    NumberType num = codec.fromJson(new TextNode(value));
    assertThat(num.toJson().asText()).isEqualTo("123.45");
  }

  @Test
  void integerWithExponent() {
    String value = "123e2";
    NumberType num = codec.fromJson(new TextNode(value));
    assertThat(num.toJson().asText()).isEqualTo("12300");
  }

  @Test
  void negativeDecimalWithExponent() {
    String value = "-1.2e2";
    NumberType num = codec.fromJson(new TextNode(value));
    assertThat(num.toJson().asText()).isEqualTo("-120");
  }

  @Test
  void decimalWithoutExponent() {
    String value = "0.5";
    NumberType num = codec.fromJson(new TextNode(value));
    BinaryParser parser = new BinaryParser(num.toHex());
    NumberType parsedNum = codec.fromParser(parser);
    assertThat(parsedNum.toJson().asText()).isEqualTo("0.5");
  }

  @Test
  void roundsUpMantissa() {
    String value = "9223372036854775895";
    NumberType num = codec.fromJson(new TextNode(value));
    assertThat(num.toJson().asText()).isEqualTo("9223372036854775900");
  }

  @Test
  void roundsDownMantissa() {
    String value = "9323372036854775804";
    NumberType num = codec.fromJson(new TextNode(value));
    assertThat(num.toJson().asText()).isEqualTo("9323372036854775800");
  }

  @Test
  void smallValueWithTrailingZeros() {
    String value = "0.002500";
    NumberType num = codec.fromJson(new TextNode(value));
    assertThat(num.toJson().asText()).isEqualTo("0.0025");
  }

  @Test
  void largeValueWithTrailingZeros() {
    String value = "9900000000000000000000";
    NumberType num = codec.fromJson(new TextNode(value));
    assertThat(num.toJson().asText()).isEqualTo("99e20");
  }

  @Test
  void smallValueWithLeadingZeros() {
    String value = "0.0000000000000000000099";
    NumberType num = codec.fromJson(new TextNode(value));
    assertThat(num.toJson().asText()).isEqualTo("99e-22");
  }

  @Test
  void mantissaGreaterThanMaxMantissa() {
    String value = "9999999999999999999999";
    NumberType num = codec.fromJson(new TextNode(value));
    assertThat(num.toJson().asText()).isEqualTo("1e22");
  }

  @Test
  void mantissaGreaterThanMaxInt64() {
    String value = "92233720368547758079";
    NumberType num = codec.fromJson(new TextNode(value));
    assertThat(num.toJson().asText()).isEqualTo("922337203685477581e2");
  }

  @Test
  void throwsOnExponentOverflowValueTooLarge() {
    // 1e40000 has exponent 40000, after normalization exponent = 40000 - 18 = 39982
    // which exceeds MAX_EXPONENT (32768)
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> codec.fromJson(new TextNode("1e40000"))
    );
    assertThat(exception.getMessage()).isEqualTo("Exponent overflow: value too large to represent");
  }

  @Test
  void throwsOnUnderflowValueTooSmall() {
    // 1e-40000 has exponent -40000, which is less than MIN_EXPONENT (-32768)
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> codec.fromJson(new TextNode("1e-40000"))
    );
    assertThat(exception.getMessage()).isEqualTo("Underflow: value too small to represent");
  }

  @Test
  void throwsWithInvalidInputNonNumberString() {
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> codec.fromJson(new TextNode("abc123"))
    );
    // BigDecimal will throw NumberFormatException which gets wrapped
    assertThat(exception).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void throwsMantissaAndExponentAreTooLarge() {
    // A value where mantissa > MAX_MANTISSA and exponent >= MAX_EXPONENT simultaneously
    // in the shrink loop. 99999999999999999999e32768 has 20-digit mantissa (> MAX_MANTISSA)
    // and exponent = 32768 (= MAX_EXPONENT), triggering the error before shrinking.
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> codec.fromJson(new TextNode("99999999999999999999e32768"))
    );
    assertThat(exception.getMessage()).isEqualTo("Mantissa and exponent are too large");
  }

  @Test
  void throwsExponentOverflowWhenMantissaExceedsInt64AtMaxExponent() {
    // A value where after shrinking to MAX_MANTISSA range, the mantissa still exceeds MAX_INT64,
    // and exponent is already at MAX_EXPONENT so it cannot shrink further.
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> codec.fromJson(new TextNode("9.3e32786"))
    );
    assertThat(exception.getMessage()).isEqualTo("Exponent overflow: value too large to represent");
  }

  @Test
  void throwsUnderflowWhenMantissaTooSmallAfterGrow() {
    // A value where the mantissa can't grow to MIN_MANTISSA because exponent hits MIN_EXPONENT.
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> codec.fromJson(new TextNode("1e-32768"))
    );
    assertThat(exception.getMessage()).isEqualTo("Underflow: value too small to represent");
  }
}
