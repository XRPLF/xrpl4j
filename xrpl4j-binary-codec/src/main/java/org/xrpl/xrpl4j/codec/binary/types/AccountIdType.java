package org.xrpl.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.serdes.BinaryParser;

/**
 * Codec for XRPL AccountID type.
 */
public class AccountIdType extends Hash160Type {

  private static final AddressCodec addressCodec = new AddressCodec();

  public AccountIdType() {
    this(UnsignedByteArray.ofSize(20));
  }

  public AccountIdType(UnsignedByteArray list) {
    super(list);
  }

  @Override
  public AccountIdType fromParser(BinaryParser parser) {
    return new AccountIdType(parser.read(getWidth()));
  }

  @Override
  public AccountIdType fromJSON(JsonNode node) {
    String textValue = node.textValue();
    if (textValue.isEmpty()) {
      return new AccountIdType();
    }
    return HEX_REGEX.matcher(textValue).matches()
      ? new AccountIdType(UnsignedByteArray.fromHex(textValue))
      : new AccountIdType(addressCodec.decodeAccountId(textValue));
  }

  @Override
  public JsonNode toJSON() {
    return new TextNode(addressCodec.encodeAccountId(value()));
  }

}
