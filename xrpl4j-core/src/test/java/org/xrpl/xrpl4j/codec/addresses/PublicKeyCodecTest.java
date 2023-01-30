package org.xrpl.xrpl4j.codec.addresses;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link PublicKeyCodec}.
 */
class PublicKeyCodecTest extends AbstractCodecTest {

  private PublicKeyCodec publicKeyCodec;

  @BeforeEach
  public void setUp() {
    publicKeyCodec = PublicKeyCodec.getInstance();
  }

  @Test
  public void encodeDecodeNodePublic() {
    testEncodeDecode(
      nodePublic -> publicKeyCodec.encodeNodePublicKey(nodePublic),
      nodePublic -> publicKeyCodec.decodeNodePublicKey(nodePublic),
      unsignedByteArrayFromHex("0388E5BA87A000CB807240DF8C848EB0B5FFA5C8E5A521BC8E105C0F0A44217828"),
      "n9MXXueo837zYH36DvMc13BwHcqtfAWNJY5czWVbp7uYTj7x17TH"
    );
  }

  @Test
  public void encodeDecodeAccountPublicKey() {
    testEncodeDecode(
      publicKey -> publicKeyCodec.encodeAccountPublicKey(publicKey),
      publicKey -> publicKeyCodec.decodeAccountPublicKey(publicKey),
      unsignedByteArrayFromHex("023693F15967AE357D0327974AD46FE3C127113B1110D6044FD41E723689F81CC6"),
      "aB44YfzW24VDEJQ2UuLPV2PvqcPCSoLnL7y5M1EzhdW4LnK5xMS3"
    );
  }

}
