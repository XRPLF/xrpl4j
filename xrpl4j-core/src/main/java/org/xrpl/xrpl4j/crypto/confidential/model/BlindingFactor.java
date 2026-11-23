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
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.model.jackson.modules.BlindingFactorDeserializer;
import org.xrpl.xrpl4j.model.jackson.modules.BlindingFactorSerializer;

import java.util.Objects;

/**
 * A blinding factor (the ElGamal randomness {@code r}) that the protocol publishes on the ledger. On the wire it is an
 * uppercase hex string, in the field literally named {@code BlindingFactor}.
 *
 * <p>Only {@link org.xrpl.xrpl4j.model.transactions.ConfidentialMptConvert} and
 * {@link org.xrpl.xrpl4j.model.transactions.ConfidentialMptConvertBack} carry that field, and disclosing {@code r}
 * there is intentional. Both cross the boundary between public and confidential balances and so already reveal a
 * plaintext {@code MPTAmount}; publishing the randomness that encrypted an already-visible amount costs no privacy, and
 * it lets validators recompute the ciphertexts deterministically instead of verifying a zero-knowledge proof.</p>
 *
 * <p>Deliberately not {@link javax.security.auth.Destroyable}: zeroing a factor that a pending transaction still
 * references would serialize an empty field. Send's randomness and the Pedersen balance factors use
 * {@link SecretBlindingFactor} instead.</p>
 *
 * <p>Deliberately hand-written rather than an Immutables {@code @Value.Immutable} abstract class, for the same reason
 * as {@link SecretBlindingFactor}: Immutables can't generate a {@code private} accessor for the stored bytes (private
 * methods can't be abstract), so the backing array would only ever be package-encapsulated, reachable by any other
 * class in this package. Construction copies the bytes it's given, and {@link #value()} hands out a fresh copy on
 * every call, so nothing outside this class -- not even a sibling in the same package -- can mutate the bytes this
 * object actually holds.</p>
 *
 * @see SecretBlindingFactor
 */
@JsonSerialize(as = BlindingFactor.class, using = BlindingFactorSerializer.class)
@JsonDeserialize(as = BlindingFactor.class, using = BlindingFactorDeserializer.class)
public final class BlindingFactor {

  /**
   * The length, in bytes, of a blinding factor (a 32-byte secp256k1 scalar).
   */
  public static final int LENGTH = 32;

  private final UnsignedByteArray rawValue;

  /**
   * Required-args constructor. Private because construction always goes through {@link #of(UnsignedByteArray)},
   * which copies the caller's bytes before storing them here.
   *
   * @param rawValue The 32-byte scalar this factor privately holds; already a defensive copy.
   */
  private BlindingFactor(final UnsignedByteArray rawValue) {
    this.rawValue = rawValue;
  }

  /**
   * Creates a blinding factor from an {@link UnsignedByteArray}. The bytes are copied, so the caller may continue to
   * use or scrub the array afterward without affecting this factor.
   *
   * @param value The 32-byte scalar.
   *
   * @return A {@link BlindingFactor}.
   */
  public static BlindingFactor of(final UnsignedByteArray value) {
    Objects.requireNonNull(value);
    final UnsignedByteArray copy = UnsignedByteArray.of(value.toByteArray());
    Preconditions.checkArgument(
      copy.length() == LENGTH,
      "BlindingFactor must be %s bytes, but was %s bytes",
      LENGTH, copy.length()
    );
    return new BlindingFactor(copy);
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
   * A defensive copy of the raw 32-byte scalar value.
   *
   * @return An {@link UnsignedByteArray}.
   */
  public UnsignedByteArray value() {
    return UnsignedByteArray.of(rawValue.toByteArray());
  }

  /**
   * The blinding factor as an uppercase hex string, as it appears on the XRP Ledger wire format.
   *
   * @return A 64-character hex {@link String}.
   */
  @JsonIgnore
  public String hexValue() {
    return BaseEncoding.base16().encode(rawValue.toByteArray());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof BlindingFactor)) {
      return false;
    }

    BlindingFactor that = (BlindingFactor) obj;
    return this.rawValue.equals(that.rawValue);
  }

  @Override
  public int hashCode() {
    return rawValue.hashCode();
  }

  /**
   * Renders the value in full, unlike {@link SecretBlindingFactor#toString()} -- this one is an on-ledger field.
   *
   * @return A {@link String} containing the hex form of this blinding factor.
   */
  @Override
  public String toString() {
    return "BlindingFactor{value=" + hexValue() + "}";
  }
}
