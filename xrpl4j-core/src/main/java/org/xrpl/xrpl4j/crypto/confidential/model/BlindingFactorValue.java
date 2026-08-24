package org.xrpl.xrpl4j.crypto.confidential.model;

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

import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

/**
 * The raw bytes of a blinding factor (the ElGamal randomness {@code r}), whether that value is disclosed on the ledger
 * ({@link BlindingFactor}) or must stay secret ({@link SecretBlindingFactor}).
 *
 * <p>Exists so {@link org.xrpl.xrpl4j.crypto.confidential.util.MptAmountEncryptor} can accept either, and is
 * deliberately the only API that does. Everything that serializes a factor takes {@link BlindingFactor} and everything
 * that proves knowledge of one takes {@link SecretBlindingFactor}, so the two roles cannot be transposed.</p>
 */
public interface BlindingFactorValue {

  /**
   * The length, in bytes, of a blinding factor (a 32-byte secp256k1 scalar).
   */
  int LENGTH = 32;

  /**
   * The raw 32-byte scalar value.
   *
   * @return An {@link UnsignedByteArray}.
   */
  UnsignedByteArray value();

  /**
   * Whether the underlying bytes have been destroyed. A disclosed {@link BlindingFactor} offers no {@code destroy()},
   * but its {@link #value()} can be destroyed independently, so this is queryable on either kind -- JNA wrappers check
   * it before handing bytes to native code, where a zero-length buffer would be read out of bounds.
   *
   * @return {@code true} if the underlying {@link UnsignedByteArray} has been destroyed.
   */
  default boolean isDestroyed() {
    return value().isDestroyed();
  }
}
