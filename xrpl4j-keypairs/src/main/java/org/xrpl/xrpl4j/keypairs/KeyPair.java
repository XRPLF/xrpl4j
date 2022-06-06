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

import org.immutables.value.Value.Immutable;

/**
 * Represents an XRPL public/private key pair.
 *
 * @deprecated This class will go away in a future version. Prefer xrpl4j-crypto variants instead.
 */
@Immutable
@Deprecated
public interface KeyPair {

  static ImmutableKeyPair.Builder builder() {
    return ImmutableKeyPair.builder();
  }

  /**
   * The private key of this {@link KeyPair}.
   *
   * @return A {@link String} containing the private key.
   */
  String privateKey();

  /**
   * The public key of this {@link KeyPair}.
   *
   * @return A {@link String} containing the public key.
   */
  String publicKey();

}
