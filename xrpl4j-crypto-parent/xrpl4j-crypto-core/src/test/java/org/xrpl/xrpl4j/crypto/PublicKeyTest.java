package org.xrpl.xrpl4j.crypto;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: crypto :: core
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.codec.addresses.VersionType;
import org.xrpl.xrpl4j.keypairs.DefaultKeyPairService;
import org.xrpl.xrpl4j.keypairs.KeyPair;
import org.xrpl.xrpl4j.keypairs.KeyPairService;

/**
 * Unit tests for {@link PublicKey}.
 */
public class PublicKeyTest {

  private KeyPairService keyPairService;

  @BeforeEach
  public void setUp() {
    this.keyPairService = new DefaultKeyPairService();
  }

  @Test
  public void fromBase58EncodedStringEd25519() {
    final String base58EncodedPublicKey = "aKEusmsH9dJvjfeEg8XhDfpEgmhcK1epAtFJfAQbACndz5mUA73B";
    assertThat(PublicKey.fromBase58EncodedPublicKey(base58EncodedPublicKey).base58Encoded())
      .isEqualTo(base58EncodedPublicKey);
  }

  @Test
  public void fromBase58EncodedStringSecp256k1() {
    final String base58EncodedPublicKey = "aB4ifx88a26RYRSSzeKW8HpbXfbpzQFRsX6dMNmMwEVHUTKzfWdk";
    assertThat(PublicKey.fromBase58EncodedPublicKey(base58EncodedPublicKey).base58Encoded())
      .isEqualTo(base58EncodedPublicKey);
  }

  @Test
  public void fromBase16EncodedStringEd25519() {
    final Seed seed = Seed.ed25519SeedFromPassphrase("hello".getBytes());
    final KeyPair keyPair = keyPairService.deriveKeyPair(seed.value());
    final String publicKeyString = keyPair.publicKey();
    final PublicKey publicKey = PublicKey.fromBase16EncodedPublicKey(publicKeyString);

    final String expectedBase58EncodedPublicKey = "aKEusmsH9dJvjfeEg8XhDfpEgmhcK1epAtFJfAQbACndz5mUA73B";
    final String expectedBase16EncodedPublicKey = "ED94F8F262A639D6C88B9EFC29F4AA8B1B8E0B7D9143A17733179A388FD26CC3AE";

    assertThat(publicKey.base58Encoded()).isEqualTo(expectedBase58EncodedPublicKey);
    assertThat(publicKey.base16Encoded()).isEqualTo(expectedBase16EncodedPublicKey);
    assertThat(AddressCodec.getInstance().encodeAccountPublicKey(publicKey.value()))
      .isEqualTo(expectedBase58EncodedPublicKey);
  }

  @Test
  public void fromBase16EncodedStringSecp256k1() {
    final Seed seed = Seed.secp256k1SeedFromPassphrase("hello".getBytes());
    final KeyPair keyPair = keyPairService.deriveKeyPair(seed.value());
    final String publicKeyString = keyPair.publicKey();
    final PublicKey publicKey = PublicKey.fromBase16EncodedPublicKey(publicKeyString);

    final String expectedBase58EncodedPublicKey = "aB4ifx88a26RYRSSzeKW8HpbXfbpzQFRsX6dMNmMwEVHUTKzfWdk";
    final String expectedBase16EncodedPublicKey = "027535A4E90B2189CF9885563F45C4F454B3BFAB21930089C3878A9427B4D648D9";

    assertThat(publicKey.base58Encoded()).isEqualTo(expectedBase58EncodedPublicKey);
    assertThat(publicKey.base16Encoded()).isEqualTo(expectedBase16EncodedPublicKey);
    assertThat(AddressCodec.getInstance().encodeAccountPublicKey(publicKey.value()))
      .isEqualTo(expectedBase58EncodedPublicKey);
  }

  @Test
  public void versionTypeSecp256k1() {
    final Seed seed = Seed.secp256k1SeedFromPassphrase("hello".getBytes());
    final String publicKeyString = keyPairService.deriveKeyPair(seed.value()).publicKey();
    final PublicKey privateKey = PublicKey.fromBase16EncodedPublicKey(publicKeyString);
    assertThat(privateKey.versionType()).isEqualTo(VersionType.SECP256K1);
  }

  @Test
  public void versionTypeEd25519() {
    final Seed seed = Seed.ed25519SeedFromPassphrase("hello".getBytes());
    final String publicKeyString = keyPairService.deriveKeyPair(seed.value()).publicKey();
    final PublicKey privateKey = PublicKey.fromBase16EncodedPublicKey(publicKeyString);
    assertThat(privateKey.versionType()).isEqualTo(VersionType.ED25519);
  }
}
