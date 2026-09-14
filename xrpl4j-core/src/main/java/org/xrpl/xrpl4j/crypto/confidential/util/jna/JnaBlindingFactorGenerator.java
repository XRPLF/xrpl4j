package org.xrpl.xrpl4j.crypto.confidential.util.jna;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2026 XRPL Foundation and its contributors
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

import org.xrpl.xrpl4j.crypto.confidential.model.SecretBlindingFactor;
import org.xrpl.xrpl4j.crypto.confidential.util.BlindingFactorGenerator;

import java.util.Arrays;
import java.util.Objects;

/**
 * Implementation of {@link BlindingFactorGenerator} that delegates to the native mpt-crypto C library
 * via {@link MptCryptoLibrary}.
 *
 * <p>This class calls {@code mpt_generate_blinding_factor} from the native library to produce
 * a 32-byte random blinding factor suitable for ElGamal encryption.</p>
 */
public class JnaBlindingFactorGenerator implements BlindingFactorGenerator {

  private final MptCryptoLibrary lib;

  /**
   * Constructs a new instance using the default {@link MptCryptoLibrary} singleton.
   *
   * @throws UnsatisfiedLinkError if the native mpt-crypto library cannot be loaded.
   */
  public JnaBlindingFactorGenerator() {
    this(MptCryptoLibrary.getInstance());
  }

  /**
   * Constructs a new instance with the specified {@link MptCryptoLibrary}.
   *
   * @param lib The native library to delegate to.
   */
  public JnaBlindingFactorGenerator(final MptCryptoLibrary lib) {
    this.lib = Objects.requireNonNull(lib);
  }

  @Override
  public SecretBlindingFactor generate() {
    byte[] outFactor = generateBytes();
    try {
      return SecretBlindingFactor.fromBytes(outFactor);
    } finally {
      Arrays.fill(outFactor, (byte) 0);
    }
  }

  /**
   * Draws 32 random bytes from the native CSPRNG. Callers scrub the returned array in a {@code finally} -- safe
   * because {@code fromBytes} copies it.
   *
   * @return A newly generated 32-byte scalar, which the caller is responsible for scrubbing.
   */
  private byte[] generateBytes() {
    byte[] outFactor = new byte[SecretBlindingFactor.LENGTH];
    int result = lib.mpt_generate_blinding_factor(outFactor);
    if (result != 0) {
      Arrays.fill(outFactor, (byte) 0);
      throw new IllegalStateException("mpt_generate_blinding_factor failed with error code: " + result);
    }
    return outFactor;
  }
}
