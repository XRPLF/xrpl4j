package org.xrpl.xrpl4j.keypairs;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: keypairs
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

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.BaseEncoding;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.transactions.Address;

public class Secp256k1KeyPairServiceTest {

  KeyPairService keyPairService = Secp256k1KeyPairService.getInstance();

  @Test
  public void generateSecp2561kKeyPairFromSeed() {
    String seed = "sp5fghtJtpUorTwvof1NpDXAzNwf5";
    KeyPair expectedKeyPair = KeyPair.builder()
      .privateKey("00D78B9735C3F26501C7337B8A5727FD53A6EFDBC6AA55984F098488561F985E23")
      .publicKey("030D58EB48B4420B1F7B9DF55087E0E29FEF0E8468F9A6825B01CA2C361042D435")
      .build();

    KeyPair keyPair = keyPairService.deriveKeyPair(seed);
    assertThat(keyPair).isEqualTo(expectedKeyPair);
  }

  @Test
  public void signMessageWithSecp2561k() {
    String privateKey = "00D78B9735C3F26501C7337B8A5727FD53A6EFDBC6AA55984F098488561F985E23";
    String message = BaseEncoding.base16().encode("test message".getBytes());
    String expectedSignature = "30440220583A91C95E54E6A651C47BEC22744E0B101E2C4060E7B08F63416" +
      "57DAD9BC3EE02207D1489C7395DB0188D3A56A977ECBA54B36FA9371B40319655B1B4429E33EF2D";
    String signature = keyPairService.sign(message, privateKey);

    assertThat(signature).isEqualTo(expectedSignature);
  }

  @Test
  public void verifySignatureWithSecp2561k() {
    String signature = "30440220583A91C95E54E6A651C47BEC22744E0B101E2C4060E7B08F6341657" +
      "DAD9BC3EE02207D1489C7395DB0188D3A56A977ECBA54B36FA9371B40319655B1B4429E33EF2D";
    String publicKey = "030D58EB48B4420B1F7B9DF55087E0E29FEF0E8468F9A6825B01CA2C361042D435";
    String message = BaseEncoding.base16().encode("test message".getBytes());
    assertThat(keyPairService.verify(message, signature, publicKey)).isTrue();
  }

  @Test
  public void deriveAddressFromSecp2561kPublicKey() {
    String publicKey = "030D58EB48B4420B1F7B9DF55087E0E29FEF0E8468F9A6825B01CA2C361042D435";
    Address expectedAddress = Address.of("rU6K7V3Po4snVhBBaU29sesqs2qTQJWDw1");
    Address address = keyPairService.deriveAddress(publicKey);
    Assertions.assertThat(address).isEqualTo(expectedAddress);
  }
}
