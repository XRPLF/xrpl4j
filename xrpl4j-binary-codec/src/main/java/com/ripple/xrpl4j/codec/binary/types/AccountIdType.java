package com.ripple.xrpl4j.codec.binary.types;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.ripple.xrpl4j.codec.addresses.AddressCodec;
import com.ripple.xrpl4j.codec.addresses.UnsignedByteArray;
import com.ripple.xrpl4j.codec.binary.serdes.BinaryParser;

import java.util.OptionalInt;

public class AccountIdType extends Hash160Type {

  private static final AddressCodec addressCodec = new AddressCodec();

  public AccountIdType() {
    this(UnsignedByteArray.ofSize(20));
  }

  public AccountIdType(UnsignedByteArray list) {
    super(list);
  }

  @Override
  public AccountIdType fromParser(BinaryParser parser, OptionalInt lengthHint) {
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
