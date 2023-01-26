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

import com.google.common.io.BaseEncoding;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.KeyType;

/**
 * Unit tests for {@link Seed}.
 */
public class SeedTest {

  @Test
  public void testEd25519SeedFromPassphrase() {
    Seed seed = Seed.ed25519SeedFromPassphrase("hello".getBytes());
    assertThat(seed.decodedSeed().type().get()).isEqualTo(KeyType.ED25519);
    assertThat(BaseEncoding.base64().encode(seed.decodedSeed().bytes().toByteArray()))
      .isEqualTo("m3HSJL1i83hdltRq0+o9cw==");
  }

  @Test
  public void testSecp256k1SeedFromPassphrase() {
    Seed seed = Seed.secp256k1SeedFromPassphrase("hello".getBytes());
    assertThat(seed.decodedSeed().type().get()).isEqualTo(KeyType.SECP256K1);
    assertThat(BaseEncoding.base64().encode(seed.decodedSeed().bytes().toByteArray()))
      .isEqualTo("m3HSJL1i83hdltRq0+o9cw==");
  }

}
