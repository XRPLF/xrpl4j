package org.xrpl.xrpl4j.crypto.confidential.util.jna;

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

/**
 * Singleton loader for the {@link NativeMptCrypto} implementation from the {@code xrpl4j-mpt-crypto} module.
 *
 * <p>The native bridge is loaded once via {@link Class#forName} to avoid a compile-time dependency on
 * {@code xrpl4j-mpt-crypto} or JNA. If the module is not on the classpath, an
 * {@link IllegalStateException} is thrown with a clear error message.</p>
 */
final class NativeMptCryptoLoader {

  private static final String IMPL_CLASS = "org.xrpl.xrpl4j.confidential.jna.MptCryptoImpl";

  private static final NativeMptCrypto INSTANCE = load();

  private NativeMptCryptoLoader() {
  }

  /**
   * Returns the singleton {@link NativeMptCrypto} instance.
   *
   * @return The {@link NativeMptCrypto} singleton.
   *
   * @throws IllegalStateException if {@code xrpl4j-mpt-crypto} is not on the classpath.
   */
  static NativeMptCrypto getInstance() {
    return INSTANCE;
  }

  private static NativeMptCrypto load() {
    try {
      Class<?> clazz = Class.forName(IMPL_CLASS);
      return (NativeMptCrypto) clazz.getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      throw new IllegalStateException(
        "xrpl4j-mpt-crypto module is not on the classpath. " +
          "Add a dependency on org.xrpl:xrpl4j-mpt-crypto to use the native encryptor/decryptor.",
        e
      );
    }
  }
}
