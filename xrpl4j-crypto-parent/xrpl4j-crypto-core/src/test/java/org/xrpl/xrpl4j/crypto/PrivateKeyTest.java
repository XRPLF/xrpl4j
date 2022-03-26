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
import org.xrpl.xrpl4j.codec.addresses.VersionType;
import org.xrpl.xrpl4j.keypairs.DefaultKeyPairService;
import org.xrpl.xrpl4j.keypairs.KeyPairService;

/**
 * Unit tests for {@link PrivateKey}.
 */
public class PrivateKeyTest {

  private KeyPairService keyPairService;

  @BeforeEach
  public void setUp() {
    this.keyPairService = new DefaultKeyPairService();
  }

  @Test
  public void fromBase58EncodedStringEd25519() {
    final String base58EncodedPrivateKey = "pDcQTi2uFBAzQ7cY2mYQtk9QuQBoLU6rJypEf8EYPQoouh";
    assertThat(PrivateKey.fromBase58EncodedPrivateKey(base58EncodedPrivateKey).base58Encoded())
      .isEqualTo(base58EncodedPrivateKey);
  }

  @Test
  public void fromBase58EncodedStringSecp256k1() {
    final String base58EncodedPrivateKey = "rEjDwJp2Pm3NrUtcf8v17jWopvqPJxyi5RTrDfhcJcWSi";
    assertThat(PrivateKey.fromBase58EncodedPrivateKey(base58EncodedPrivateKey).base58Encoded())
      .isEqualTo(base58EncodedPrivateKey);
  }

  @Test
  public void fromBase16EncodedStringEd25519() {
    final Seed seed = Seed.ed25519SeedFromPassphrase("hello".getBytes());
    final String privateKeyString = keyPairService.deriveKeyPair(seed.value()).privateKey();
    final PrivateKey privateKey = PrivateKey.fromBase16EncodedPrivateKey(privateKeyString);

    final String expectedBase58EncodedPrivateKey = "pDcQTi2uFBAzQ7cY2mYQtk9QuQBoLU6rJypEf8EYPQoouh";
    final String expectedBase16EncodedPrivateKey = "EDB224AFDCCEC7AA4E245E35452585D4FBBE37519BCA3929578BFC5BBD4640E163";

    assertThat(privateKey.base58Encoded()).isEqualTo(expectedBase58EncodedPrivateKey);
    assertThat(privateKey.base16Encoded()).isEqualTo(expectedBase16EncodedPrivateKey);
  }

  @Test
  public void fromBase16EncodedStringSecp256k1() {
    final Seed seed = Seed.secp256k1SeedFromPassphrase("hello".getBytes());
    final String privateKeyString = keyPairService.deriveKeyPair(seed.value()).privateKey();
    final PrivateKey publicKey = PrivateKey.fromBase16EncodedPrivateKey(privateKeyString);

    final String expectedBase58EncodedPrivateKey = "rEjDwJp2Pm3NrUtcf8v17jWopvqPJxyi5RTrDfhcJcWSi";
    final String expectedBase16EncodedPrivateKey = "00DAD3C2B4BF921398932C889DE5335F89D90249355FC6FFB73F1256D2957F9F17";

    assertThat(publicKey.base58Encoded()).isEqualTo(expectedBase58EncodedPrivateKey);
    assertThat(publicKey.base16Encoded()).isEqualTo(expectedBase16EncodedPrivateKey);
  }

  @Test
  public void versionTypeSecp256k1() {
    final Seed seed = Seed.secp256k1SeedFromPassphrase("hello".getBytes());
    final String privateKeyString = keyPairService.deriveKeyPair(seed.value()).privateKey();
    final PrivateKey privateKey = PrivateKey.fromBase16EncodedPrivateKey(privateKeyString);
    assertThat(privateKey.versionType()).isEqualTo(VersionType.SECP256K1);
  }

  @Test
  public void versionTypeEd25519() {
    final Seed seed = Seed.ed25519SeedFromPassphrase("hello".getBytes());
    final String privateKeyString = keyPairService.deriveKeyPair(seed.value()).privateKey();
    final PrivateKey privateKey = PrivateKey.fromBase16EncodedPrivateKey(privateKeyString);
    assertThat(privateKey.versionType()).isEqualTo(VersionType.ED25519);
  }

}
