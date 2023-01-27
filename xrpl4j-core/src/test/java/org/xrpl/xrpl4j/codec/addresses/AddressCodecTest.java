package org.xrpl.xrpl4j.codec.addresses;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: address-codec
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


import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.exceptions.EncodingFormatException;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.XAddress;


/**
 * Unit tests for {@link AddressCodec}.
 */
@SuppressWarnings( {"ParameterName", "MethodName", "LocalVariableName"})
public class AddressCodecTest extends AbstractCodecTest {

  AddressCodec addressCodec;

  @BeforeEach
  public void setUp() {
    addressCodec = new AddressCodec();
  }

  @Test
  public void encodeDecodeAccountId() {
    testEncodeDecode(
      accountId -> addressCodec.encodeAccountId(accountId).value(),
      accountId -> addressCodec.decodeAccountId(Address.of(accountId)),
      unsignedByteArrayFromHex("BA8E78626EE42C41B46D46C3048DF3A1C3C87072"),
      "rJrRMgiRgrU6hDF4pgu5DXQdWyPbY35ErN"
    );
  }

  @Test
  public void encodeDecodeNodePublic() {
    testEncodeDecode(
      nodePublic -> addressCodec.encodeNodePublicKey(nodePublic),
      nodePublic -> addressCodec.decodeNodePublicKey(nodePublic),
      unsignedByteArrayFromHex("0388E5BA87A000CB807240DF8C848EB0B5FFA5C8E5A521BC8E105C0F0A44217828"),
      "n9MXXueo837zYH36DvMc13BwHcqtfAWNJY5czWVbp7uYTj7x17TH"
    );
  }

  @Test
  public void addressWithBadChecksum() {
    Address address = Address.of("r9cZA1mLK5R5am25ArfXFmqgNwjZgnfk59");

    assertThrows(
      EncodingFormatException.class,
      () -> addressCodec.classicAddressToXAddress(address, true),
      "Checksum does not validate"
    );
  }

  @Test
  public void xAddressWithBadChecksum() {
    XAddress xAddress = XAddress.of("XVLhHMPHU98es4dbozjVtdWzVrDjtV5fdx1mHp98tDMoQXa");

    assertThrows(
      EncodingFormatException.class,
      () -> addressCodec.xAddressToClassicAddress(xAddress),
      "Checksum does not validate"
    );
  }
}
