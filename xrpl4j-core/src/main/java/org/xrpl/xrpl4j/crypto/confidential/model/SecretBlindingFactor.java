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

import com.google.common.base.Preconditions;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

import javax.security.auth.Destroyable;

/**
 * A blinding factor that must never leave this process — the counterpart to the disclosed {@link BlindingFactor}.
 *
 * <p>Per XLS-0096, ConfidentialMptSend has no blinding-factor field: the amount stays hidden, and an ElGamal ciphertext
 * together with its {@code r} reveals that amount, since MPT amounts are small enough to brute-force the remaining
 * discrete log. A Send shares one {@code r} across the sender, destination, issuer, and auditor ciphertexts and reuses
 * it to blind the Pedersen amount commitment, so one leak unmasks the amount for every participant at once. The
 * Pedersen balance factors are secret for the same reason: paired with the encrypted balance they would reveal it.</p>
 *
 * <p>There is no {@code hexValue()}, no Jackson serializer, and no conversion to {@link BlindingFactor}, so a secret
 * cannot reach a transaction. Scrubbing is the one part the type cannot enforce, since {@link Destroyable} is not
 * {@code AutoCloseable}: callers must still {@link #destroy()} a factor once the proof built from it is complete.</p>
 *
 * @see BlindingFactor
 */
@Value.Immutable
public abstract class SecretBlindingFactor implements BlindingFactorValue, Destroyable {

  /**
   * Creates a secret blinding factor from an {@link UnsignedByteArray}.
   *
   * @param value The 32-byte scalar.
   *
   * @return A {@link SecretBlindingFactor}.
   */
  public static SecretBlindingFactor of(final UnsignedByteArray value) {
    return ImmutableSecretBlindingFactor.builder().value(value).build();
  }

  /**
   * Creates a secret blinding factor from a hex string. Intended for tests and known-answer vectors; production code
   * should use
   * {@link org.xrpl.xrpl4j.crypto.confidential.util.BlindingFactorGenerator#generateSecretBlindingFactor()}.
   *
   * @param hex The 64-character hex string representing the scalar.
   *
   * @return A {@link SecretBlindingFactor}.
   */
  public static SecretBlindingFactor of(final String hex) {
    return of(UnsignedByteArray.fromHex(hex));
  }

  /**
   * Creates a secret blinding factor from a 32-byte array. The bytes are copied, so the caller may scrub the array.
   *
   * @param bytes The 32-byte scalar.
   *
   * @return A {@link SecretBlindingFactor}.
   */
  public static SecretBlindingFactor fromBytes(final byte[] bytes) {
    return of(UnsignedByteArray.of(bytes));
  }

  /**
   * Validates that the blinding factor is exactly 32 bytes.
   */
  @Value.Check
  void check() {
    Preconditions.checkArgument(
      value().length() == LENGTH,
      "SecretBlindingFactor must be %s bytes, but was %s bytes",
      LENGTH, value().length()
    );
  }

  /**
   * Destroys this blinding factor by zeroing its underlying {@link #value()}. Because this is an immutable value type,
   * destruction is delegated to the mutable {@link UnsignedByteArray} it wraps rather than tracked by a field here.
   */
  @Override
  public void destroy() {
    value().destroy();
  }

  @Override
  public boolean isDestroyed() {
    return value().isDestroyed();
  }

  /**
   * Redacts the value, so a secret factor cannot reach a log.
   *
   * @return A {@link String} with the value redacted, plus whether this factor has been destroyed.
   */
  @Override
  public String toString() {
    return "SecretBlindingFactor{value=[redacted], destroyed=" + isDestroyed() + "}";
  }
}
