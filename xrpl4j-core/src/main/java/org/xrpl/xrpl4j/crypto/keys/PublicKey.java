package org.xrpl.xrpl4j.crypto.keys;

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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedInteger;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.math.ec.ECPoint;
import org.immutables.value.Value.Derived;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Lazy;
import org.xrpl.xrpl4j.codec.addresses.AddressBase58;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.codec.addresses.Decoded;
import org.xrpl.xrpl4j.codec.addresses.KeyType;
import org.xrpl.xrpl4j.codec.addresses.PublicKeyCodec;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByte;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.addresses.Version;
import org.xrpl.xrpl4j.model.jackson.modules.PublicKeyDeserializer;
import org.xrpl.xrpl4j.model.jackson.modules.PublicKeySerializer;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.util.Objects;

/**
 * A typed instance of an XRPL Seed, which can be decoded into an instance of {@link Decoded}.
 */
@Immutable
@JsonSerialize(as = ImmutablePublicKey.class, using = PublicKeySerializer.class)
@JsonDeserialize(as = ImmutablePublicKey.class, using = PublicKeyDeserializer.class)
public interface PublicKey {

  /**
   * A one-byte prefix for ed25519 keys. In XRPL, ed25519 public keys are prefixed with a one-byte prefix (i.e., `0xED`)
   * in order to be consistent with secp256k1 public keys, which always have 33 bytes.
   */
  UnsignedByte ED2559_PREFIX = UnsignedByte.of(0xED);

  /**
   * Multi-signed transactions must contain an empty String in the SigningPublicKey field. This constant is an
   * {@link PublicKey} that can be used as the {@link Transaction#signingPublicKey()} value for multi-signed
   * transactions.
   */
  PublicKey MULTI_SIGN_PUBLIC_KEY = PublicKey.builder().value(UnsignedByteArray.empty()).build();

  /**
   * Instantiates a new builder.
   *
   * @return A {@link ImmutablePublicKey.Builder}.
   */
  static ImmutablePublicKey.Builder builder() {
    return ImmutablePublicKey.builder();
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

    if (base58EncodedPublicKey.isEmpty()) {
      return MULTI_SIGN_PUBLIC_KEY;
    }

    return PublicKey.builder()
      .value(PublicKeyCodec.getInstance().decodeAccountPublicKey(base58EncodedPublicKey))
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

    if (base16EncodedPublicKey.isEmpty()) {
      return MULTI_SIGN_PUBLIC_KEY;
    }

    return PublicKey.builder()
      .value(UnsignedByteArray.of(BaseEncoding.base16().decode(base16EncodedPublicKey.toUpperCase())))
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
  @Derived
  default String base58Value() {
    if (value().length() == 0) {
      return "";
    }

    return PublicKeyCodec.getInstance().encodeAccountPublicKey(this.value());
  }

  /**
   * The private-key value, as a Base16-encoded (i.e., HEX) string. Note that if this is an Ed25519 private-key, then
   * this value contains a leading prefix of `ED`, in hex.
   *
   * @return A {@link String}.
   */
  @Derived
  default String base16Value() {
    return this.value().hexValue();
  }

  /**
   * The type of this key.
   *
   * @return A {@link KeyType}.
   */
  @Derived
  default KeyType keyType() {
    return this.base16Value().startsWith("ED") ? KeyType.ED25519 : KeyType.SECP256K1;
  }

  /**
   * Returns the uncompressed public key value (64 bytes without the 0x04 prefix).
   *
   * <p>This method is only supported for secp256k1 keys. For secp256k1, the compressed format (33 bytes)
   * is decompressed to get the full x,y coordinates (64 bytes). The 0x04 prefix byte that indicates
   * an uncompressed point is stripped from the result.</p>
   *
   * <p>This is useful for ElGamal encryption operations which require the uncompressed public key format.</p>
   *
   * @return An {@link UnsignedByteArray} containing the 64-byte uncompressed public key (without 0x04 prefix).
   * @throws UnsupportedOperationException if this is an Ed25519 key or an empty key (e.g., MULTI_SIGN_PUBLIC_KEY).
   */
  @Lazy
  default UnsignedByteArray uncompressedValue() {
    if (value().length() == 0) {
      throw new UnsupportedOperationException(
        "Cannot compute uncompressed value for an empty public key."
      );
    }

    if (keyType() == KeyType.ED25519) {
      throw new UnsupportedOperationException(
        "Ed25519 keys do not support uncompressed format. Only secp256k1 keys can be decompressed."
      );
    }

    X9ECParameters curveParams = CustomNamedCurves.getByName("secp256k1");
    ECPoint publicKeyPoint = curveParams.getCurve().decodePoint(value().toByteArray());
    byte[] uncompressedWithPrefix = publicKeyPoint.getEncoded(false); // 65 bytes with 0x04 prefix

    // Strip the 0x04 prefix to get 64 bytes
    byte[] uncompressedWithoutPrefix = new byte[64];
    System.arraycopy(uncompressedWithPrefix, 1, uncompressedWithoutPrefix, 0, 64);

    return UnsignedByteArray.of(uncompressedWithoutPrefix);
  }

  /**
   * Derive an XRPL address from this public key.
   *
   * @return A Base58Check encoded XRPL address in Classic Address form.
   */
  @Lazy
  default Address deriveAddress() {
    return AddressCodec.getInstance().encodeAccountId(computePublicKeyHash(this.value()));
  }

  /**
   * Compute the RIPEMD160 of the SHA256 of the given public key, which can be encoded to an XRPL address.
   *
   * @param publicKey The public key that should be hashed.
   *
   * @return An {@link UnsignedByteArray} containing the non-encoded XRPL address derived from the public key.
   */
  static UnsignedByteArray computePublicKeyHash(final UnsignedByteArray publicKey) {
    Objects.requireNonNull(publicKey);

    byte[] sha256 = Hashing.sha256().hashBytes(publicKey.toByteArray()).asBytes();
    RIPEMD160Digest digest = new RIPEMD160Digest();
    digest.update(sha256, 0, sha256.length);
    byte[] ripemdSha256 = new byte[digest.getDigestSize()];
    digest.doFinal(ripemdSha256, 0);
    return UnsignedByteArray.of(ripemdSha256);
  }

}
