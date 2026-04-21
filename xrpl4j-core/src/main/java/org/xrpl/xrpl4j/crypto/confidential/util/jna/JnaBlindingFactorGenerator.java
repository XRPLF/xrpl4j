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

import org.xrpl.xrpl4j.crypto.confidential.BlindingFactor;
import org.xrpl.xrpl4j.crypto.confidential.util.BlindingFactorGenerator;

import java.util.Objects;

/**
 * Implementation of {@link BlindingFactorGenerator} that delegates to the native mpt-crypto C library
 * via the {@link NativeMptCrypto} bridge.
 *
 * <p>This class lives in {@code xrpl4j-core} and has no compile-time dependency on JNA.
 * The {@link NativeMptCrypto} implementation is loaded from {@code xrpl4j-mpt-crypto} at runtime.</p>
 */
public class JnaBlindingFactorGenerator implements BlindingFactorGenerator {

  private static final int BLINDING_FACTOR_SIZE = 32;

  private final NativeMptCrypto nativeCrypto;

  /**
   * Constructs a new instance by reflectively loading the native bridge from {@code xrpl4j-mpt-crypto}.
   *
   * @throws IllegalStateException if {@code xrpl4j-mpt-crypto} is not on the classpath.
   */
  public JnaBlindingFactorGenerator() {
    this(NativeMptCryptoLoader.getInstance());
  }

  /**
   * Constructs a new instance with the specified {@link NativeMptCrypto} bridge.
   *
   * @param nativeCrypto The native bridge to delegate to.
   */
  public JnaBlindingFactorGenerator(final NativeMptCrypto nativeCrypto) {
    this.nativeCrypto = Objects.requireNonNull(nativeCrypto);
  }

  @Override
  public BlindingFactor generate() {
    byte[] outFactor = new byte[BLINDING_FACTOR_SIZE];
    int result = nativeCrypto.generateBlindingFactor(outFactor);
    if (result != 0) {
      throw new IllegalStateException("mpt_generate_blinding_factor failed with error code: " + result);
    }
    return BlindingFactor.fromBytes(outFactor);
  }
}
