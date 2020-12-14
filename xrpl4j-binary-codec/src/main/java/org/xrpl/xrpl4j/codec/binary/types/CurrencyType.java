package org.xrpl.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByte;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.serdes.BinaryParser;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Codec for currency property inside an XRPL issued currency amount json.
 */
@SuppressWarnings("AbbreviationAsWordInName")
public class CurrencyType extends Hash160Type {

  private static final Pattern ISO_REGEX = Pattern.compile("^[A-Z0-9]{3}$");

  private final Optional<String> iso;

  public CurrencyType() {
    this(UnsignedByteArray.ofSize(20));
  }

  /**
   * Required-args Constructor.
   *
   * @param list A {@link UnsignedByteArray} for intializing this CurrencyType.
   */
  public CurrencyType(final UnsignedByteArray list) {
    super(list);
    String rawISO = rawISO(list);
    boolean isNative = isNative(list);
    boolean lossLessISO = onlyIso(list) && !rawISO.equals("XRP") && ISO_REGEX.matcher(rawISO).matches();
    this.iso = isNative ? Optional.of("XRP") : lossLessISO ? Optional.of(rawISO) : Optional.empty();
  }

  @Override
  public CurrencyType fromParser(BinaryParser parser) {
    return new CurrencyType(parser.read(getWidth()));
  }

  @Override
  public CurrencyType fromJson(JsonNode node) {
    String textValue = node.textValue();
    if (!isValidRepresentation(textValue)) {
      throw new IllegalArgumentException("Unsupported Currency representation: " + textValue);
    }
    UnsignedByteArray bytes = textValue.length() == 3 ? isoToBytes(textValue) : UnsignedByteArray.fromHex(textValue);
    return new CurrencyType(bytes);
  }

  @Override
  public JsonNode toJson() {
    return iso.map(TextNode::new).orElseGet(() -> new TextNode(toHex()));
  }

  private boolean isNative(UnsignedByteArray byteList) {
    String iso = byteList.slice(12, 15).hexValue();
    return onlyIso(byteList) && iso.equals("000000");
  }

  private boolean onlyIso(UnsignedByteArray byteList) {
    for (int i = byteList.length() - 1; i >= 0; i--) {
      if (byteList.get(i).asInt() != 0 && !(i == 12 || i == 13 || i == 14)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Convert {@code list} to a {@link String} of raw ISO codes.
   *
   * @param list
   *
   * @return
   */
  private String rawISO(UnsignedByteArray list) {
    return new String(list.slice(12, 15).toByteArray());
  }

  /**
   * Ensures that a value is a valid representation of a currency.
   *
   * @return {@code true} if {@code value} is a valid representation; {@code false} otherwise.
   */
  boolean isValidRepresentation(String value) {
    return isStringRepresentation(value);
  }

  /**
   * Tests if ISO is a valid iso code.
   *
   * @return {@code true} if {@code iso} is a valid ISO Code; {@code false} otherwise.
   */
  private boolean isIsoCode(String iso) {
    return ISO_REGEX.matcher(iso).matches();
  }

  /**
   * Tests if hex is a valid hex-string.
   *
   * @return {@code true} if {@code hex} is a valid hex-encoded string; {@code false} otherwise.
   */
  private boolean isHex(String hex) {
    return HEX_REGEX.matcher(hex).matches();
  }

  /**
   * Tests if a string is a valid representation of a currency.
   *
   * @return {@code true} if {@code input} is a valid ISO or Hex representation; {@code false} otherwise.
   */
  boolean isStringRepresentation(String input) {
    return isIsoCode(input) || isHex(input);
  }

  /**
   * Convert an ISO code to a currency bytes representation.
   *
   * @return An {@link UnsignedByteArray}.
   */
  private UnsignedByteArray isoToBytes(String iso) {
    UnsignedByteArray bytes = UnsignedByteArray.ofSize(20);
    if (!iso.equals("XRP")) {
      for (int i = 0; i < iso.length(); i++) {
        bytes.set(12 + i, UnsignedByte.of(iso.charAt(i)));
      }
    }
    return bytes;
  }

}
