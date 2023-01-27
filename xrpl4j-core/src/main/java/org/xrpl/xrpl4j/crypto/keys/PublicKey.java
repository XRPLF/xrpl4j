package org.xrpl.xrpl4j.crypto.keys;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedInteger;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.immutables.value.Value.Derived;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Lazy;
import org.xrpl.xrpl4j.codec.addresses.AddressBase58;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.codec.addresses.Decoded;
import org.xrpl.xrpl4j.codec.addresses.KeyType;
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
   * Multi-signed transactions must contain an empty String in the SigningPublicKey field. This constant
   * is an {@link PublicKey} that can be used as the {@link Transaction#signingPublicKey()} value for multi-signed
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
    
    return AddressBase58.encode(value(), Lists.newArrayList(Version.ACCOUNT_PUBLIC_KEY), UnsignedInteger.valueOf(33));
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
  default KeyType versionType() {
    return this.base16Value().startsWith("ED") ? KeyType.ED25519 : KeyType.SECP256K1;
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
