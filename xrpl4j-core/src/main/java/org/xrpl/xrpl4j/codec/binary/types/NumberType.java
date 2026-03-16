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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.base.Strings;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.serdes.BinaryParser;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;

public class NumberType extends SerializedType<NumberType> {

  /**
   * The fixed width of a serialized Number in bytes (8 for mantissa + 4 for exponent).
   */
  public static final int WIDTH = 12;

  private static final BigInteger MIN_MANTISSA = new BigInteger("1000000000000000000"); // 10^18
  private static final BigInteger MAX_MANTISSA = new BigInteger("9999999999999999999"); // 10^19 - 1
  private static final BigInteger MAX_INT64 = new BigInteger("9223372036854775807"); // 2^63 - 1
  private static final int MIN_EXPONENT = -32768;
  private static final int MAX_EXPONENT = 32768;

  private static final int DEFAULT_VALUE_EXPONENT = Integer.MIN_VALUE;

  public NumberType() {
    this(UnsignedByteArray.ofSize(WIDTH));
  }

  public NumberType(UnsignedByteArray bytes) {
    super(bytes);
  }

  @Override
  public NumberType fromParser(BinaryParser parser) {
    return new NumberType(parser.read(WIDTH));
  }

  // xrpld implementation: src/libxrpl/basics/Number.cpp -> doNormalize
  @Override
  public NumberType fromJson(JsonNode node) {
    String value = node.isInt() || node.isLong() ? String.valueOf(node.asLong()) : node.asText();
    BigDecimal decimal = new BigDecimal(value).stripTrailingZeros();

    long mantissa;
    int exponent;

    if (decimal.signum() == 0) {
      mantissa = 0;
      exponent = DEFAULT_VALUE_EXPONENT;
    } else {
      final boolean negative = decimal.signum() < 0;
      BigDecimal abs = decimal.abs();
      BigInteger absMantissa = abs.unscaledValue();
      exponent = -abs.scale();

      // Grow mantissa until it reaches MIN_MANTISSA
      while (absMantissa.compareTo(MIN_MANTISSA) < 0 && exponent > MIN_EXPONENT) {
        absMantissa = absMantissa.multiply(BigInteger.TEN);
        exponent--;
      }

      // Shrink mantissa until it fits within MAX_MANTISSA, tracking last digit for rounding
      BigInteger lastDigit = null;
      while (absMantissa.compareTo(MAX_MANTISSA) > 0) {
        if (exponent >= MAX_EXPONENT) {
          throw new IllegalArgumentException("Mantissa and exponent are too large");
        }
        lastDigit = absMantissa.mod(BigInteger.TEN);
        absMantissa = absMantissa.divide(BigInteger.TEN);
        exponent++;
      }

      // Handle underflow: if exponent too small or mantissa too small, throw error
      if (exponent < MIN_EXPONENT || absMantissa.compareTo(MIN_MANTISSA) < 0) {
        throw new IllegalArgumentException("Underflow: value too small to represent");
      }

      // Handle overflow: if exponent exceeds MAX_EXPONENT after growing.
      if (exponent > MAX_EXPONENT) {
        throw new IllegalArgumentException("Exponent overflow: value too large to represent");
      }

      // If unsigned mantissa exceeds int64 max, shrink one more time
      if (absMantissa.compareTo(MAX_INT64) > 0) {
        if (exponent >= MAX_EXPONENT) {
          throw new IllegalArgumentException("Exponent overflow: value too large to represent");
        }
        lastDigit = absMantissa.mod(BigInteger.TEN);
        absMantissa = absMantissa.divide(BigInteger.TEN);
        exponent++;
      }

      // Round up if last removed digit was >= 5
      if (lastDigit != null && lastDigit.compareTo(BigInteger.valueOf(5)) >= 0) {
        absMantissa = absMantissa.add(BigInteger.ONE);
        // After rounding, mantissa may exceed MAX_INT64 again
        if (absMantissa.compareTo(MAX_INT64) > 0) {
          if (exponent >= MAX_EXPONENT) {
            throw new IllegalArgumentException("Exponent overflow: value too large to represent");
          }
          lastDigit = absMantissa.mod(BigInteger.TEN);
          absMantissa = absMantissa.divide(BigInteger.TEN);
          exponent++;
          if (lastDigit.compareTo(BigInteger.valueOf(5)) >= 0) {
            absMantissa = absMantissa.add(BigInteger.ONE);
          }
        }
      }

      mantissa = negative ? -absMantissa.longValueExact() : absMantissa.longValueExact();
    }

    ByteBuffer buffer = ByteBuffer.allocate(WIDTH);
    buffer.putLong(mantissa);
    buffer.putInt(exponent);
    return new NumberType(UnsignedByteArray.of(buffer.array()));
  }

  // xrpld implementation: src/libxrpl/basics/Number.cpp -> to_string
  @Override
  public JsonNode toJson() {
    byte[] bytes = toBytes();
    ByteBuffer buffer = ByteBuffer.wrap(bytes);
    long mantissa = buffer.getLong();
    int exponent = buffer.getInt();

    // Canonical zero: mantissa=0, exponent=DEFAULT_VALUE_EXPONENT
    if (mantissa == 0 && exponent == DEFAULT_VALUE_EXPONENT) {
      return new TextNode("0");
    }

    boolean negative = mantissa < 0;
    BigInteger mantissaAbs = BigInteger.valueOf(mantissa).abs();

    // If mantissa < MIN_MANTISSA, it was shrunk for int64 serialization. Restore it.
    if (mantissaAbs.compareTo(BigInteger.ZERO) != 0 && mantissaAbs.compareTo(MIN_MANTISSA) < 0) {
      mantissaAbs = mantissaAbs.multiply(BigInteger.TEN);
      exponent--;
    }

    int rangeLog = 18;

    // Use scientific notation for exponents outside [-28, -8] (when exponent != 0)
    if (exponent != 0 && (exponent < -(rangeLog + 10) || exponent > -(rangeLog - 10))) {
      // Strip trailing zeros from mantissa
      int exp = exponent;
      while (mantissaAbs.compareTo(BigInteger.ZERO) != 0 &&
        mantissaAbs.mod(BigInteger.TEN).equals(BigInteger.ZERO) &&
        exp < MAX_EXPONENT) {
        mantissaAbs = mantissaAbs.divide(BigInteger.TEN);
        exp++;
      }
      String sign = negative ? "-" : "";
      return new TextNode(sign + mantissaAbs + "e" + exp);
    }

    // Decimal rendering for -28 <= exponent <= -8, or exponent == 0
    int padPrefix = rangeLog + 12; // 30
    int padSuffix = rangeLog + 8;  // 26

    String mantissaStr = mantissaAbs.toString();
    String rawValue = Strings.repeat("0", padPrefix) + mantissaStr + Strings.repeat("0", padSuffix);
    int offset = exponent + padPrefix + rangeLog + 1; // exponent + 49

    String integerPart = rawValue.substring(0, offset).replaceFirst("^0+", "");
    if (integerPart.isEmpty()) {
      integerPart = "0";
    }
    String fractionPart = rawValue.substring(offset).replaceFirst("0+$", "");

    String result = (negative ? "-" : "") + integerPart + (fractionPart.isEmpty() ? "" : "." + fractionPart);
    return new TextNode(result);
  }
}
