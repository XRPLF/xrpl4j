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
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.serdes.BinaryParser;
import org.xrpl.xrpl4j.model.transactions.Address;

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
  public AccountIdType fromJson(JsonNode node) {
    String textValue = node.textValue();
    if (textValue.isEmpty()) {
      return new AccountIdType();
    }
    return HEX_REGEX.matcher(textValue).matches() ?
      new AccountIdType(UnsignedByteArray.fromHex(textValue))
      : new AccountIdType(addressCodec.decodeAccountId(Address.of(textValue)));
  }

  @Override
  public JsonNode toJson() {
    return new TextNode(addressCodec.encodeAccountId(value()).value());
  }

}
