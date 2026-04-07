package org.xrpl.xrpl4j.confidential.jna;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: mpt-crypto
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

import org.xrpl.xrpl4j.crypto.confidential.util.jna.NativeMptCrypto;

/**
 * JNA-backed implementation of {@link NativeMptCrypto} that delegates to the native mpt-crypto
 * library via {@link MptCryptoLibrary}.
 *
 * <p>This class is loaded reflectively by {@code xrpl4j-core} when {@code xrpl4j-mpt-crypto}
 * is on the classpath. It must have a public no-arg constructor.</p>
 */
public class MptCryptoImpl implements NativeMptCrypto {

  private final MptCryptoLibrary lib;

  /**
   * Constructs a new instance using the default native library singleton.
   */
  public MptCryptoImpl() {
    this(MptCryptoLibrary.INSTANCE);
  }

  /**
   * Constructs a new instance with the specified native library binding.
   *
   * @param lib The JNA library binding to delegate to.
   */
  MptCryptoImpl(final MptCryptoLibrary lib) {
    this.lib = lib;
  }

  @Override
  public int encryptAmount(long amount, byte[] pubkey, byte[] blindingFactor, byte[] outCiphertext) {
    return lib.mpt_encrypt_amount(amount, pubkey, blindingFactor, outCiphertext);
  }

  @Override
  public int decryptAmount(byte[] ciphertext, byte[] privkey, long[] outAmount) {
    return lib.mpt_decrypt_amount(ciphertext, privkey, outAmount);
  }

  @Override
  public int generateBlindingFactor(byte[] outFactor) {
    return lib.mpt_generate_blinding_factor(outFactor);
  }
}
