package org.xrpl.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import org.xrpl.xrpl4j.codec.addresses.ByteUtils;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByte;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.BinaryCodecObjectMapperFactory;
import org.xrpl.xrpl4j.codec.binary.math.MathUtils;
import org.xrpl.xrpl4j.codec.binary.serdes.BinaryParser;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Codec for XRPL Amount type.
 */
class AmountType extends SerializedType<AmountType> {

  public static final BigDecimal MAX_DROPS = new BigDecimal("1e17");
  public static final BigDecimal MIN_XRP = new BigDecimal("1e-6");

  public static final String DEFAULT_AMOUNT_HEX = "4000000000000000";
  public static final String ZERO_CURRENCY_AMOUNT_HEX = "8000000000000000";
  public static final int NATIVE_AMOUNT_BYTE_LENGTH = 8;
  public static final int CURRENCY_AMOUNT_BYTE_LENGTH = 48;
  private static final int MAX_IOU_PRECISION = 16;
  private static final int MIN_IOU_EXPONENT = -81;
  private static final int MAX_IOU_EXPONENT = 95;

  private static final ObjectMapper objectMapper = BinaryCodecObjectMapperFactory.getObjectMapper();

  public AmountType() {
    this(UnsignedByteArray.fromHex(DEFAULT_AMOUNT_HEX));
  }

  public AmountType(UnsignedByteArray byteList) {
    super(byteList);
  }

  /**
   * Validate XRP amount.
   *
   * @param amount String representing XRP amount
   *
   * @throws IllegalArgumentException if invalid amount
   */
  private static void assertXrpIsValid(String amount) {
    if (amount.contains(".")) {
      throw new IllegalArgumentException(amount + " is an illegal amount");
    }
    BigDecimal value = new BigDecimal(amount);
    if (!value.equals(BigDecimal.ZERO)) {
      if (value.compareTo(MIN_XRP) < 0 || value.compareTo(MAX_DROPS) > 0) {
        throw new IllegalArgumentException(amount + " is an illegal amount");
      }
    }
  }

  /**
   * Validate IOU.value amount
   *
   * @param decimal Decimal.js object representing IOU.value
   *
   * @throws IllegalArgumentException if invalid amount
   */
  private static void assertIouIsValid(BigDecimal decimal) {
    if (!decimal.equals(BigDecimal.ZERO)) {
      int precision = decimal.precision();
      int exponent = MathUtils.getExponent(decimal);
      if (precision > MAX_IOU_PRECISION ||
        exponent > MAX_IOU_EXPONENT ||
        exponent < MIN_IOU_EXPONENT
      ) {
        throw new Error("Decimal precision out of range");
      }
      verifyNoDecimal(decimal);
    }
  }

  /**
   * Ensure that the value after being multiplied by the exponent does not contain a decimal.
   *
   * @param decimal a Decimal object
   *
   * @throws IllegalArgumentException if invalid amount
   */
  private static void verifyNoDecimal(BigDecimal decimal) {
    BigDecimal exponent = new BigDecimal("1e" + -(MathUtils.getExponent(decimal) - 15));
    String integerNumberString = decimal.multiply(exponent).toPlainString();
    if (integerNumberString.indexOf(".") > 0) {
      throw new Error("Decimal place found in integerNumberString");
    }
  }

  @Override
  public AmountType fromParser(BinaryParser parser) {
    boolean isXrp = !parser.peek().isNthBitSet(1);
    int numBytes = isXrp ? NATIVE_AMOUNT_BYTE_LENGTH : CURRENCY_AMOUNT_BYTE_LENGTH;
    return new AmountType(parser.read(numBytes));
  }

  @Override
  public AmountType fromJson(JsonNode value) throws JsonProcessingException {
    if (value.isValueNode()) {
      assertXrpIsValid(value.asText());
      UInt64Type number = new UInt64Type().fromJson(value.asText());
      byte[] rawBytes = number.toBytes();
      rawBytes[0] |= 0x40;
      return new AmountType(UnsignedByteArray.of(rawBytes));
    }

    Amount amount = objectMapper.treeToValue(value, Amount.class);
    BigDecimal number = new BigDecimal(amount.value());

    UnsignedByteArray result = number.unscaledValue().equals(BigInteger.ZERO) ?
      UnsignedByteArray.fromHex(ZERO_CURRENCY_AMOUNT_HEX) :
      getAmountBytes(number);

    UnsignedByteArray currency = new CurrencyType().fromJson(value.get("currency")).value();
    UnsignedByteArray issuer = new AccountIdType().fromJson(value.get("issuer")).value();

    result.append(currency);
    result.append(issuer);

    return new AmountType(result);
  }

  private UnsignedByteArray getAmountBytes(BigDecimal number) {
    BigInteger paddedNumber = MathUtils.toPaddedBigInteger(number, 16);
    byte[] amountBytes = ByteUtils.toByteArray(paddedNumber, 8);
    amountBytes[0] |= 0x80;
    if (number.compareTo(BigDecimal.ZERO) > 0) {
      amountBytes[0] |= 0x40;
    }

    int exponent = MathUtils.getExponent(number);
    if (exponent > MAX_IOU_EXPONENT || exponent < MIN_IOU_EXPONENT) {
      throw new IllegalArgumentException("exponent out of range");
    }
    UnsignedByte exponentByte = UnsignedByte.of(97 + exponent - 15);
    amountBytes[0] |= exponentByte.asInt() >>> 2;
    amountBytes[1] |= (exponentByte.asInt() & 0x03) << 6;

    return UnsignedByteArray.of(amountBytes);
  }

  @Override
  public JsonNode toJson() {
    if (this.isNative()) {
      byte[] rawBytes = toBytes();
      rawBytes[0] &= 0x3f;
      BigInteger value = new BigInteger(rawBytes);
      if (!this.isPositive()) {
        value = value.negate();
      }
      return new TextNode(value.toString());
    } else {
      BinaryParser parser = new BinaryParser(this.toHex());
      UnsignedByteArray mantissa = parser.read(8);
      final SerializedType<?> currency = new CurrencyType().fromParser(parser);
      final SerializedType<?> issuer = new AccountIdType().fromParser(parser);

      UnsignedByte b1 = mantissa.get(0);
      UnsignedByte b2 = mantissa.get(1);

      boolean isPositive = b1.isNthBitSet(2);
      String sign = isPositive ? "" : "-";

      int exponent = ((b1.asInt() & 0x3f) << 2) + ((b2.asInt() & 0xff) >> 6) - 97;
      mantissa.set(0, UnsignedByte.of(0));
      mantissa.set(1, UnsignedByte.of(b2.asInt() & 0x3f));

      BigDecimal value = new BigDecimal(new BigInteger(sign + mantissa.hexValue(), 16))
        .multiply(new BigDecimal("1e" + exponent))
        .stripTrailingZeros();

      assertIouIsValid(value);

      Amount amount = Amount.builder()
        .currency(currency.toJson().asText())
        .issuer(issuer.toJson().asText())
        .value(value.toPlainString())
        .build();

      return objectMapper.valueToTree(amount);
    }
  }

  /**
   * Returns true if this amount is a "native" XRP amount.
   *
   * @return {@code true} if this AmountType is native; {@code false} otherwise.
   */
  private boolean isNative() {
    // 1st bit in 1st byte is set to 0 for native XRP
    return (toBytes()[0] & 0x80) == 0;
  }

  /**
   * Determines if this AmountType is positive.
   *
   * @return {@code true} if this AmountType is positive; {@code false} otherwise.
   */
  private boolean isPositive() {
    // 2nd bit in 1st byte is set to 1 for positive amounts
    return (toBytes()[0] & 0x40) > 0;
  }

}
