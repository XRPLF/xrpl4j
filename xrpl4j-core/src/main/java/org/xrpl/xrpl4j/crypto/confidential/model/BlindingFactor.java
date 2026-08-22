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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;
import org.immutables.value.Value;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.model.jackson.modules.BlindingFactorDeserializer;
import org.xrpl.xrpl4j.model.jackson.modules.BlindingFactorSerializer;

import javax.security.auth.Destroyable;

/**
 * The 32-byte scalar blinding factor (ElGamal randomness {@code r}) used to encrypt an amount in Confidential MPT
 * transactions. Held as raw bytes; on the wire it is serialized as an uppercase hex string.
 *
 * <p><b>Whether this value is secret depends on the operation using it, not on this type.</b> Per XLS-0096, a blinding
 * factor plays two distinct roles:</p>
 *
 * <ul>
 *   <li><b>Disclosed</b> in {@link org.xrpl.xrpl4j.model.transactions.ConfidentialMptConvert} and
 *       {@link org.xrpl.xrpl4j.model.transactions.ConfidentialMptConvertBack}, where it is a required on-ledger field.
 *       Those operations cross the public/confidential boundary and already reveal a plaintext {@code MPTAmount}, so
 *       publishing {@code r} costs no privacy and lets validators deterministically recompute the ciphertexts instead
 *       of verifying a zero-knowledge proof.</li>
 *   <li><b>Secret</b> in {@code ConfidentialMptSend}, which has no blinding-factor field: the amount stays hidden, and
 *       an ElGamal ciphertext plus its {@code r} reveals that amount (amounts are small enough to brute-force the
 *       remaining discrete log). A Send additionally shares one {@code r} across the sender, destination, issuer, and
 *       auditor ciphertexts and reuses it as the Pedersen amount commitment's blinding factor, so leaking it unmasks
 *       the amount for every participant at once.</li>
 * </ul>
 *
 * <p>Because a given instance cannot tell which role it plays, this type fails safe: {@link #toString()} redacts the
 * value, and the type is {@link Destroyable} so callers holding a secret (Send) factor can {@link #destroy()} it once
 * it is no longer needed. Do not destroy a factor that is still referenced by a transaction awaiting serialization —
 * see {@link #hexValue()}.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableBlindingFactor.class, using = BlindingFactorSerializer.class)
@JsonDeserialize(as = ImmutableBlindingFactor.class, using = BlindingFactorDeserializer.class)
public abstract class BlindingFactor implements Destroyable {

  /**
   * The length, in bytes, of a blinding factor (a 32-byte ElGamal scalar).
   */
  public static final int LENGTH = 32;

  /**
   * Creates a blinding factor from an {@link UnsignedByteArray}.
   *
   * @param value The 32-byte scalar.
   *
   * @return A {@link BlindingFactor}.
   */
  public static BlindingFactor of(final UnsignedByteArray value) {
    return ImmutableBlindingFactor.builder().value(value).build();
  }

  /**
   * Creates a blinding factor from a hex string.
   *
   * @param hex The 64-character hex string representing the scalar.
   *
   * @return A {@link BlindingFactor}.
   */
  public static BlindingFactor of(final String hex) {
    return of(UnsignedByteArray.fromHex(hex));
  }

  /**
   * Creates a blinding factor from a 32-byte array.
   *
   * @param bytes The 32-byte scalar.
   *
   * @return A {@link BlindingFactor}.
   */
  public static BlindingFactor fromBytes(final byte[] bytes) {
    return of(UnsignedByteArray.of(bytes));
  }

  /**
   * The raw 32-byte scalar value.
   *
   * @return An {@link UnsignedByteArray}.
   */
  public abstract UnsignedByteArray value();

  /**
   * Validates that the blinding factor is exactly 32 bytes.
   */
  @Value.Check
  void check() {
    Preconditions.checkArgument(
      value().length() == LENGTH,
      "BlindingFactor must be %s bytes, but was %s bytes",
      LENGTH, value().length()
    );
  }

  /**
   * The blinding factor as an uppercase hex string, as it appears on the XRP Ledger wire format. Only
   * ConfidentialMptConvert and ConfidentialMptConvertBack put this value on the wire; a Send's blinding factor is
   * secret and is never serialized, so this is not called for one.
   *
   * <p>Deliberately <em>not</em> {@code @Value.Lazy}: caching the hex would keep a copy in an immutable {@link String}
   * that {@link #destroy()} cannot zero, which would defeat the {@link Destroyable} contract for a secret (Send)
   * factor. Recomputing from the live bytes on each call means nothing survives {@code destroy()}.</p>
   *
   * @return A 64-character hex {@link String}.
   *
   * @throws IllegalStateException if this blinding factor has been destroyed. {@link #destroy()} empties the underlying
   *   bytes, so without this check a destroyed factor would silently serialize as an empty {@code BlindingFactor}
   *   field, which the network rejects as {@code tecBAD_PROOF} — a loud failure here is easier to diagnose.
   */
  @JsonIgnore
  public String hexValue() {
    Preconditions.checkState(!isDestroyed(), "BlindingFactor has been destroyed");
    return BaseEncoding.base16().encode(value().toByteArray());
  }

  /**
   * Destroys this blinding factor by zeroing out its underlying {@link #value()}. Because {@link BlindingFactor} is an
   * immutable value type, destruction is delegated to the mutable {@link UnsignedByteArray} it wraps rather than
   * tracked via a field on this class.
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
   * A debug-friendly representation that <em>redacts</em> the value: the blinding factor is secret (with an ElGamal
   * ciphertext it reveals the encrypted amount), so it must never appear in logs. Use {@link #hexValue()} only for the
   * wire format.
   *
   * @return A {@link String} with the value redacted, plus whether this factor has been destroyed.
   */
  @Override
  public String toString() {
    return "BlindingFactor{value=[redacted], destroyed=" + isDestroyed() + "}";
  }
}
