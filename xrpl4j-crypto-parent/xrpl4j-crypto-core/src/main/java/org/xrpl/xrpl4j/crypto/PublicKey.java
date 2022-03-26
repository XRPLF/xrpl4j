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
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedInteger;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;
import org.xrpl.xrpl4j.codec.addresses.AddressBase58;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.codec.addresses.Decoded;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.addresses.Version;
import org.xrpl.xrpl4j.codec.addresses.VersionType;

import java.util.Objects;

/**
 * A typed instance of an XRPL Seed, which can be decoded into an instance of {@link Decoded}.
 *
 * @deprecated consider using the variant from org.xrpl.xrpl4j.crypto.core.
 */
@Deprecated
public interface PublicKey {

  /**
   * Instantiates a new builder.
   *
   * @return A {@link ImmutableDefaultPublicKey.Builder}.
   */
  static ImmutableDefaultPublicKey.Builder builder() {
    return ImmutableDefaultPublicKey.builder();
  }

  /**
   * Construct a {@link PublicKey} from a base58-encoded {@link String}.
   *
   * @param base58EncodedPublicKey A base58-encoded {@link String}.
   *
   * @return A {@link PrivateKey}.
   */
  static PublicKey fromBase58EncodedPublicKey(final String base58EncodedPublicKey) {
    Objects.requireNonNull(base58EncodedPublicKey);
    return PublicKey.builder()
      .value(AddressCodec.getInstance().decodeAccountPublicKey(base58EncodedPublicKey))
      .build();
  }

  /**
   * Construct a {@link PrivateKey} from a base16-encoded (HEX) {@link String}.
   *
   * @param base16EncodedPublicKey A base16-encoded (HEX) {@link String}.
   *
   * @return A {@link PrivateKey}.
   */
  static PublicKey fromBase16EncodedPublicKey(final String base16EncodedPublicKey) {
    Objects.requireNonNull(base16EncodedPublicKey);
    return PublicKey.builder()
      .value(UnsignedByteArray.of(BaseEncoding.base16().decode(base16EncodedPublicKey)))
      .build();
  }

  /**
   * The key in binary (Note: will be 33 bytes).
   *
   * @return An instance of {@link UnsignedByteArray}.
   */
  UnsignedByteArray value();

  /**
   * The public-key, as a base-58 encoded {@link String}.
   *
   * @return A {@link String}.
   */
  String base58Encoded();

  /**
   * The private-key value, as a Base16-encoded (i.e., HEX) string. Note that if this is an Ed25519 private-key, then
   * this value contains a leading prefix of `ED`, in hex.
   *
   * @return A {@link String}.
   */
  String base16Encoded();

  /**
   * The type of this key.
   *
   * @return A {@link VersionType}.
   */
  VersionType versionType();

  /**
   * Abstract implementation for immutables.
   */
  @Value.Immutable
  abstract class DefaultPublicKey implements PublicKey {

    @Override
    @Derived
    public String base58Encoded() {
      return AddressBase58.encode(value(), Lists.newArrayList(Version.ACCOUNT_PUBLIC_KEY), UnsignedInteger.valueOf(33));
    }

    @Override
    @Derived
    public String base16Encoded() {
      return this.value().hexValue();
    }

    @Derived
    @Override
    public VersionType versionType() {
      return this.base16Encoded().startsWith("ED") ? VersionType.ED25519 : VersionType.SECP256K1;
    }

    @Override
    public String toString() {
      return base58Encoded();
    }
  }

}
