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

import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.immutables.value.Value.Lazy;
import org.xrpl.xrpl4j.codec.addresses.AddressBase58;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.codec.addresses.Decoded;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.addresses.Version;

import java.util.Objects;

/**
 * A typed instance of an XRPL Seed, which can be decoded into an instance of {@link Decoded}.
 *
 * @see "https://xrpl.org/cryptographic-keys.html#seed"
 */
@Deprecated
public interface Seed {

  /**
   * Instantiates a new builder.
   *
   * @return A {@link ImmutableDefaultSeed.Builder}.
   */
  static ImmutableDefaultSeed.Builder builder() {
    return ImmutableDefaultSeed.builder();
  }

  /**
   * Construct an Ed25519-compatible seed from the supplied {@code passphrase}.
   *
   * @param passphrase A byte-array.
   *
   * @return A {@link Seed}.
   */
  static Seed ed25519SeedFromPassphrase(final byte[] passphrase) {
    Objects.requireNonNull(passphrase);

    final byte[] entropyBytes = new byte[16];

    // 16 bytes of deterministic entropy.
    Hashing.sha512()
      .hashBytes(passphrase) // <-- This is equivalent to the `passphrase` in the xrpl.org docs.
      .writeBytesTo(entropyBytes, 0, 16);

    final String encodedSeed = AddressBase58.encode(
      UnsignedByteArray.of(entropyBytes),
      Lists.newArrayList(Version.ED25519_SEED),
      UnsignedInteger.valueOf(16)
    );

    return Seed.builder()
      .value(encodedSeed)
      .build();
  }

  /**
   * Construct an secp256k1-compatible seed from the supplied {@code passphrase}.
   *
   * @param passphrase A byte array.
   *
   * @return A {@link Seed}.
   */
  static Seed secp256k1SeedFromPassphrase(final byte[] passphrase) {
    Objects.requireNonNull(passphrase);

    final byte[] entropyBytes = new byte[16];

    // 16 bytes of deterministic entropy.
    Hashing.sha512()
      .hashBytes(passphrase) // <-- This is equivalent to the `passphrase` in the xrpl.org docs.
      .writeBytesTo(entropyBytes, 0, 16);

    final String encodedSeed = AddressBase58.encode(
      UnsignedByteArray.of(entropyBytes),
      Lists.newArrayList(Version.FAMILY_SEED),
      UnsignedInteger.valueOf(16)
    );

    return Seed.builder()
      .value(encodedSeed)
      .build();
  }

  /**
   * The seed value, as a Base58-encoded string.
   *
   * @return A {@link String}.
   */
  String value();

  /**
   * The decoded details of this seed.
   *
   * @return An instance of {@link Decoded}.
   */
  Decoded decodedSeed();

  /**
   * Abstract implementation for immutables.
   */
  @Value.Immutable
  abstract class DefaultSeed implements Seed {

    @Override
    public abstract String value();

    @Override
    @Lazy
    public Decoded decodedSeed() {
      return AddressCodec.getInstance().decodeSeed(value());
    }

    @Override
    public String toString() {
      return value();
    }
  }


}
