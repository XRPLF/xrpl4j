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

import org.immutables.value.Value;

import java.util.Objects;
import java.util.Optional;

/**
 * <p>An extensible interface that can be used to identify a type of underlying key-storage, which typically treats
 * key-material as "secret information" whereas the data encrypted by these keys is generally not stored in these
 * platforms.</p>
 *
 * <p>For example, a database password would not be stored in the key-store. Instead, a private key or symmetric
 * key would be stored in the key-store, and the encrypted database password would be stored in some other system in
 * encrypted form.</p>
 */
public interface KeyStoreType {

  /**
   * Private keys are derived from a seed that is derived from a unique identifier and an in-memory server secret.
   */
  KeyStoreType DERIVED_SERVER_SECRET = KeyStoreType.of("derived_server_secret");

  /**
   * Private keys and symmetric secrets are stored in Google Cloud Platform KMS.
   */
  KeyStoreType GCP_KMS = KeyStoreType.of("gcp_kms");

  /**
   * Builder for immutables.
   *
   * @return A {@link ImmutableDefaultKeyStoreType.Builder}.
   */
  static ImmutableDefaultKeyStoreType.Builder builder() {
    return ImmutableDefaultKeyStoreType.builder();
  }

  /**
   * Builder.
   *
   * @param keystoreId A {@link String} identifying the KeyStore.
   *
   * @return A {@link KeyStoreType}.
   */
  static KeyStoreType of(final String keystoreId) {
    Objects.requireNonNull(keystoreId);
    return ImmutableDefaultKeyStoreType.builder().keystoreId(keystoreId).build();
  }

  /**
   * Helper method to construct a org.xrpl4j.crypto.keystore type from a {@link String}.
   *
   * @param keystoreTypeId A {@link String} identifying the KeyStore type to construct.
   *
   * @return A {@link KeyStoreType}.
   */
  static KeyStoreType fromKeystoreTypeId(final String keystoreTypeId) {
    return Optional.ofNullable(keystoreTypeId)
      .filter($ -> $.length() > 0)
      .map(String::toLowerCase)
      .map(KeyStoreType::of)
      .orElseThrow(() -> new IllegalArgumentException("KeyStoreType must be non-null and have at least 1 character"));
  }

  /**
   * The unique identifier of this KeyStore type.
   *
   * @return A {@link String}
   */
  String keystoreId();

  /**
   * For immutables.
   */
  @Value.Immutable
  abstract class DefaultKeyStoreType implements KeyStoreType {

  }
}
