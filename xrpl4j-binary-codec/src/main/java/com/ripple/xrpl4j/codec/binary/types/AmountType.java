package com.ripple.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.ripple.xrpl4j.codec.binary.serdes.BinaryParser;
import com.ripple.xrpl4j.codec.binary.serdes.UnsignedByteList;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.OptionalInt;

class AmountType extends SerializedType<AmountType> {

  public static final BigDecimal MAX_DROPS = new BigDecimal("1e17");
  public static final BigDecimal MIN_XRP = new BigDecimal("1e-6");

  public static final String DEFAULT_AMOUNT_HEX = "4000000000000000";
  public static final int NATIVE_AMOUNT_BYTE_LENGTH = 8;
  public static final int CURRENCY_AMOUNT_BYTE_LENGTH = 48;

  private final int byteLength;

  public AmountType() {
    this(new UnsignedByteList(DEFAULT_AMOUNT_HEX), 8);
  }

  public AmountType(UnsignedByteList byteList, int byteLength) {
    super(byteList);
    this.byteLength = byteLength;
  }

  @Override
  public AmountType fromParser(BinaryParser parser, OptionalInt lengthHint) {
    boolean isXRP = !parser.peek().isNthBitSet(1);
    int numBytes = isXRP ? NATIVE_AMOUNT_BYTE_LENGTH : CURRENCY_AMOUNT_BYTE_LENGTH;
    return new AmountType(new UnsignedByteList(parser.read(numBytes)), numBytes);
  }

  @Override
  public AmountType fromJSON(JsonNode value) {
    if (value.isValueNode()) {
      assertXrpIsValid(value.asText());
      UInt64 number = new UInt64().fromJSON(value.asText());
      byte[] rawBytes = number.toBytes();
      rawBytes[0] |= 0x40;
      return new AmountType(new UnsignedByteList(rawBytes), 8);
    }
    // FIXME support object amount
    throw new UnsupportedOperationException("amount object not yet implemented"); // FIXME
  }

  @Override
  public JsonNode toJSON() {
    if (this.isNative()) {
      byte[] rawBytes = toBytes();
      rawBytes[0] &= 0x3f;
      BigInteger value = new BigInteger(rawBytes);
      if (!this.isPositive()) {
        value = value.negate();
      }
      return new TextNode(value.toString());
    }
    else {
      throw new UnsupportedOperationException("amount object not yet implemented"); // FIXME
    }
  }

  /**
   * Returns true if this amount is a native XRP amount
   *
   * @return
   */
  private boolean isNative() {
    // 1st bit in 1st byte is set to 0 for native XRP
    return (toBytes()[0] & 0x80) == 0;
  }

  private boolean isPositive() {
    // 2nd bit in 1st byte is set to 1 for positive amounts
    return (toBytes()[0] & 0x40) > 0;
  }

  /**
   * Validate XRP amount
   *
   * @param amount String representing XRP amount
   * @returns void, but will throw if invalid amount
   */
  private static void assertXrpIsValid(String amount) {
    if (amount.contains(".")) {
      throw new IllegalArgumentException(amount + " is an illegal amount");
    }
    BigDecimal value = new BigDecimal(amount);
    if (!value.equals(BigInteger.ZERO)) {
      if (value.compareTo(MIN_XRP) < 0 || value.compareTo(MAX_DROPS) > 0) {
        throw new IllegalArgumentException(amount + " is an illegal amount");
      }
    }
  }

}
