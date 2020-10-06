package com.ripple.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.ripple.xrpl4j.codec.binary.UnsignedByte;
import com.ripple.xrpl4j.codec.binary.serdes.BinaryParser;
import com.ripple.xrpl4j.codec.binary.serdes.UnsignedByteList;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.regex.Pattern;

public class CurrencyType extends Hash160Type {

  private static final Pattern ISO_REGEX = Pattern.compile("^[A-Z0-9]{3}$");

  private static final Pattern HEX_REGEX = Pattern.compile("^[A-Z0-9]{40}$");

  private final Optional<String> iso;
  private final boolean isNative;

  public CurrencyType() {
    this(new UnsignedByteList(20));
  }

  public CurrencyType(UnsignedByteList list) {
    super(list);
    String rawISO = rawISO(list);
    this.isNative = isNative(list);
    boolean lossLessISO = onlyIso(list) && !rawISO.equals("XRP") && ISO_REGEX.matcher(rawISO).matches();
    this.iso = this.isNative ? Optional.of("XRP") : lossLessISO ? Optional.of(rawISO) : Optional.empty();
  }

  @Override
  public CurrencyType fromParser(BinaryParser parser, OptionalInt lengthHint) {
    return new CurrencyType(parser.read(getWidth()));
  }

  @Override
  public CurrencyType fromJSON(JsonNode node) {
    String textValue = node.textValue();
    if (!isValidRepresentation(textValue)) {
      throw new IllegalArgumentException("Unsupported Currency representation: " + textValue);
    }
    UnsignedByteList bytes = textValue.length() == 3 ? isoToBytes(textValue) : new UnsignedByteList(textValue);
    return new CurrencyType(bytes);
  }

  @Override
  public JsonNode toJSON() {
    return iso.map(TextNode::new).orElseGet(() -> new TextNode(toHex()));
  }

  public boolean isNative() {
    return isNative;
  }

  private boolean isNative(UnsignedByteList byteList) {
    String iso = byteList.slice(12, 15).toHex();
    return onlyIso(byteList) && iso.equals("000000");
  }

  private boolean onlyIso(UnsignedByteList byteList) {
    for (int i = byteList.getLength() - 1; i >= 0; i--) {
      if (byteList.get(i).asInt() != 0 && !(i == 12 || i == 13 || i == 14)) {
        return false;
      }
    }
    return true;
  }

  private String rawISO(UnsignedByteList list) {
    return new String(list.slice(12, 15).toBytes());
  }

  /**
   * Ensures that a value is a valid representation of a currency
   */
  boolean isValidRepresentation(String value) {
    return isStringRepresentation(value);
  }

  /**
   * Tests if ISO is a valid iso code
   */
  private boolean isIsoCode(String iso) {
    return ISO_REGEX.matcher(iso).matches();
  }

  /**
   * Tests if hex is a valid hex-string
   */
  private boolean isHex(String hex) {
    return HEX_REGEX.matcher(hex).matches();
  }

  /**
   * Tests if a string is a valid representation of a currency
   */
  boolean isStringRepresentation(String input) {
    return isIsoCode(input) || isHex(input);
  }

  /**
   * Convert an ISO code to a currency bytes representation
   */
  private UnsignedByteList isoToBytes(String iso) {
    UnsignedByteList bytes = new UnsignedByteList(20);
    if (!iso.equals("XRP")) {
      for (int i = 0; i < iso.length(); i++) {
        bytes.set(12 + i, UnsignedByte.of(iso.charAt(i)));
      }
    }
    return bytes;
  }

}
