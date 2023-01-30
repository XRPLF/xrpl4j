package org.xrpl.xrpl4j.crypto.keys;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.xrpl.xrpl4j.crypto.TestConstants.EC_PRIVATE_KEY;
import static org.xrpl.xrpl4j.crypto.TestConstants.ED_PRIVATE_KEY;

import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.Base58;
import org.xrpl.xrpl4j.codec.addresses.KeyType;
import org.xrpl.xrpl4j.crypto.TestConstants;

/**
 * Unit tests for {@link PrivateKey}.
 */
public class PrivateKeyTest {

  @Test
  public void valueEd25519() {
    assertThat(Base58.encode(ED_PRIVATE_KEY.value().toByteArray())).isEqualTo(TestConstants.ED_PRIVATE_KEY_B58);
  }

  @Test
  public void valueSecp256k1() {
    assertThat(Base58.encode(EC_PRIVATE_KEY.value().toByteArray())).isEqualTo(TestConstants.EC_PRIVATE_KEY_B58);
  }

  @Test
  public void keyTypeEd25519() {
    assertThat(ED_PRIVATE_KEY.keyType()).isEqualTo(KeyType.ED25519);
  }

  @Test
  public void keyTypeSecp256k1() {
    assertThat(EC_PRIVATE_KEY.keyType()).isEqualTo(KeyType.SECP256K1);
  }

  @Test
  void destroy() {
    PrivateKey privateKey = PrivateKey.of(ED_PRIVATE_KEY.value());
    assertThat(privateKey.isDestroyed()).isFalse();
    privateKey.destroy();
    assertThat(privateKey.isDestroyed()).isTrue();
    assertThat(privateKey.value().hexValue()).isEqualTo("");
    privateKey.destroy();
    assertThat(privateKey.isDestroyed()).isTrue();
    assertThat(privateKey.value().hexValue()).isEqualTo("");

    privateKey = PrivateKey.of(EC_PRIVATE_KEY.value());
    assertThat(privateKey.isDestroyed()).isFalse();
    privateKey.destroy();
    assertThat(privateKey.isDestroyed()).isTrue();
    assertThat(privateKey.value().hexValue()).isEqualTo("");
    privateKey.destroy();
    assertThat(privateKey.isDestroyed()).isTrue();
    assertThat(privateKey.value().hexValue()).isEqualTo("");
  }

  @Test
  void equals() {
    assertThat(ED_PRIVATE_KEY).isEqualTo(ED_PRIVATE_KEY);
    assertThat(ED_PRIVATE_KEY).isNotEqualTo(EC_PRIVATE_KEY);
    assertThat(EC_PRIVATE_KEY).isNotEqualTo(new Object());

    assertThat(EC_PRIVATE_KEY).isEqualTo(EC_PRIVATE_KEY);
    assertThat(EC_PRIVATE_KEY).isNotEqualTo(ED_PRIVATE_KEY);
    assertThat(EC_PRIVATE_KEY).isNotEqualTo(new Object());
  }

  @Test
  void testHashcode() {
    assertThat(ED_PRIVATE_KEY.hashCode()).isEqualTo(ED_PRIVATE_KEY.hashCode());
    assertThat(ED_PRIVATE_KEY.hashCode()).isNotEqualTo(EC_PRIVATE_KEY.hashCode());

    assertThat(EC_PRIVATE_KEY.hashCode()).isEqualTo(EC_PRIVATE_KEY.hashCode());
    assertThat(EC_PRIVATE_KEY.hashCode()).isNotEqualTo(ED_PRIVATE_KEY.hashCode());
  }

  @Test
  void testToString() {
    assertThat(ED_PRIVATE_KEY.toString()).isEqualTo(
      "PrivateKey{" +
        "value=[redacted], " +
        "destroyed=false" +
        "}"
    );

    assertThat(EC_PRIVATE_KEY.toString()).isEqualTo(
      "PrivateKey{" +
        "value=[redacted], " +
        "destroyed=false" +
        "}"
    );
  }

}
