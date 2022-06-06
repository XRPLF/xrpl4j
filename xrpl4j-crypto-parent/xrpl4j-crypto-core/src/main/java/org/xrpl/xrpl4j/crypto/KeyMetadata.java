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

import com.google.common.base.Preconditions;
import org.immutables.value.Value;

import java.util.Optional;

/**
 * Metadata about a particular key, public or private. This interface acts like a pointer to a key without having to
 * pass around the key itself. This pattern enables a single API to operate with keys that exist in the same JVM as the
 * running program, but also allows the API to operate on keys that exist outside of the JVM, such as in an external
 * key-store like an HSM or other hardware wallet.
 *
 * @deprecated consider using the variant from org.xrpl.xrpl4j.crypto.core.
 */
@Deprecated
public interface KeyMetadata {

  /**
   * Any empty instance that conformas to this interface but is otherwise empty. The purpose of this instance is to
   * enable the signing interface to work with implementations that don't have more than a single private key internally
   * without having to introduce null values into the API.
   */
  KeyMetadata EMPTY = KeyMetadata.builder()
    .platformIdentifier("n/a")
    .keyringIdentifier("n/a")
    .keyIdentifier("n/a")
    .keyVersion("n/a")
    .build();

  /**
   * A Builder for immutables.
   *
   * @return A {@link ImmutableKeyMetadata.Builder}.
   */
  static ImmutableKeyMetadata.Builder builder() {
    return ImmutableKeyMetadata.builder();
  }

  /**
   * The unique identifier of the platform that can decode this secret.
   *
   * @return A {@link String}.
   */
  String platformIdentifier();

  /**
   * The unique identifier of the keyring that holds the the private-key used to encrypt this encoded secret.
   *
   * @return A {@link String}.
   */
  String keyringIdentifier();

  /**
   * The unique identifier for the private-key used to encrypt this encoded secret.
   *
   * @return A {@link String}.
   */
  String keyIdentifier();

  /**
   * The version of the encryption key used to encrypt this secret.
   *
   * @return A {@link String}.
   */
  String keyVersion();

  /**
   * An optional password that will unlock this particular key.
   *
   * @return An {@link Optional} of type {@link String}.
   */
  Optional<String> keyPassword();

  /**
   * To satisfy immutables.
   */
  @Value.Immutable
  abstract class AbstractKeyMetadata implements KeyMetadata {

    /**
     * For immutables.
     */
    @Value.Check
    public void doChecks() {
      Preconditions.checkArgument(platformIdentifier().length() > 0, "platformIdentifier must not be empty");
      Preconditions.checkArgument(keyringIdentifier().length() > 0, "keyringIdentifier must not be empty");
      Preconditions.checkArgument(keyIdentifier().length() > 0, "keyIdentifier must not be empty");
      Preconditions.checkArgument(keyVersion().length() > 0, "keyVersion must not be empty");
    }
  }

}
