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
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
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
 * <p>Deliberately hand-written rather than an Immutables {@code @Value.Immutable} abstract class: Immutables can't
 * generate a {@code private} accessor for the stored bytes (private methods can't be abstract), so the backing array
 * would only ever be package-encapsulated, reachable by any other class in this package. A plain, final class with a
 * genuinely {@code private final} field is the only way to make sure {@link #destroy()} is the sole way to reach the
 * bytes this object actually holds. Construction copies the bytes it's given, and {@link #value()} hands out a fresh
 * copy on every call, so nothing outside this class -- not even a sibling in the same package -- can mutate or
 * destroy them.</p>
 *
 * @see BlindingFactor
 */
public final class SecretBlindingFactor implements Destroyable {

  /**
   * The length, in bytes, of a blinding factor (a 32-byte secp256k1 scalar).
   */
  public static final int LENGTH = 32;

  private final UnsignedByteArray rawValue;

  private final AtomicBoolean destroyed = new AtomicBoolean(false);

  /**
   * Required-args constructor. Private because construction always goes through {@link #of(UnsignedByteArray)},
   * which copies the caller's bytes before storing them here.
   *
   * @param rawValue The 32-byte scalar this factor privately holds; already a defensive copy.
   */
  private SecretBlindingFactor(final UnsignedByteArray rawValue) {
    this.rawValue = rawValue;
  }

  /**
   * Creates a secret blinding factor from an {@link UnsignedByteArray}. The bytes are copied, so the caller may
   * continue to use or scrub the array afterward without affecting this factor.
   *
   * @param value The 32-byte scalar.
   *
   * @return A {@link SecretBlindingFactor}.
   */
  public static SecretBlindingFactor of(final UnsignedByteArray value) {
    Objects.requireNonNull(value);
    final UnsignedByteArray copy = UnsignedByteArray.of(value.toByteArray());
    Preconditions.checkArgument(
      copy.length() == LENGTH,
      "SecretBlindingFactor must be %s bytes, but was %s bytes",
      LENGTH, copy.length()
    );
    return new SecretBlindingFactor(copy);
  }

  /**
   * Creates a secret blinding factor from a hex string. Intended for tests and known-answer vectors; production code
   * should use {@link org.xrpl.xrpl4j.crypto.confidential.util.BlindingFactorGenerator#generate()}.
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
   * A defensive copy of the raw 32-byte scalar value.
   *
   * @return An {@link UnsignedByteArray}.
   *
   * @throws IllegalStateException if this factor has been destroyed. A destroyed secret has no valid value to hand
   *   out, and returning zeroed bytes instead would let a caller that forgot to check {@link #isDestroyed()} silently
   *   operate on garbage rather than fail where the mistake actually happened.
   */
  public UnsignedByteArray value() {
    checkNotDestroyed();
    return UnsignedByteArray.of(rawValue.toByteArray());
  }

  /**
   * Discloses this factor as a {@link BlindingFactor}, for the {@code BlindingFactor} field that
   * {@link org.xrpl.xrpl4j.model.transactions.ConfidentialMptConvert} and
   * {@link org.xrpl.xrpl4j.model.transactions.ConfidentialMptConvertBack} publish on the ledger.
   *
   * <p>Every factor is generated secret, so publishing one is always an explicit act performed here -- grep for calls
   * to this method to audit every point at which randomness reaches the wire. Only Convert and ConvertBack may do so:
   * both already reveal a plaintext {@code MPTAmount}, so the randomness that encrypted it costs no privacy. A Send's
   * randomness must never be passed through this method.</p>
   *
   * <p>The returned factor holds its own copy, so destroying this one afterwards does not disturb it.</p>
   *
   * @return A {@link BlindingFactor} carrying the same scalar.
   *
   * @throws IllegalStateException if this factor has been destroyed.
   */
  public BlindingFactor toBlindingFactor() {
    return BlindingFactor.of(this.value());
  }

  /**
   * Destroys this blinding factor by zeroing the bytes it privately holds and marking it destroyed. The
   * {@code compareAndSet} ensures the zeroing runs exactly once even if two threads call this concurrently.
   */
  @Override
  public void destroy() {
    if (destroyed.compareAndSet(false, true)) {
      rawValue.destroy();
    }
  }

  @Override
  public boolean isDestroyed() {
    return destroyed.get();
  }

  private void checkNotDestroyed() {
    if (isDestroyed()) {
      throw new IllegalStateException("SecretBlindingFactor has already been destroyed");
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof SecretBlindingFactor)) {
      return false;
    }

    SecretBlindingFactor that = (SecretBlindingFactor) obj;
    return this.rawValue.equals(that.rawValue);
  }

  @Override
  public int hashCode() {
    return rawValue.hashCode();
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
