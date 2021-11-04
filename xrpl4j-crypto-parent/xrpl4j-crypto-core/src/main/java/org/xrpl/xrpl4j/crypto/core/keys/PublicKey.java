package org.xrpl.xrpl4j.crypto.core.keys;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
import org.xrpl.xrpl4j.crypto.core.signing.UnsignedByteArrayDeserializer;
import org.xrpl.xrpl4j.crypto.core.signing.UnsignedByteArraySerializer;

import java.util.Objects;

/**
 * A typed instance of an XRPL Seed, which can be decoded into an instance of {@link Decoded}.
 */
@JsonSerialize(as = ImmutableDefaultPublicKey.class)
@JsonDeserialize(as = ImmutableDefaultPublicKey.class)
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
  @JsonSerialize(using = UnsignedByteArraySerializer.class)
  @JsonDeserialize(using = UnsignedByteArrayDeserializer.class)
  UnsignedByteArray value();

  /**
   * The public-key, as a base-58 encoded {@link String}.
   *
   * @return A {@link String}.
   */
  String base58Value();

  /**
   * The private-key value, as a Base16-encoded (i.e., HEX) string. Note that if this is an Ed25519 private-key, then
   * this value contains a leading prefix of `ED`, in hex.
   *
   * @return A {@link String}.
   */
  String base16Value();

  /**
   * The private-key value, as a Base16-encoded (i.e., HEX) string. Note that if this is an Ed25519 private-key, then
   * this value contains a leading prefix of `ED`, in hex.
   *
   * @return A {@link String}.
   */
  default String hexValue() {
    return base16Value();
  }

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
  @JsonSerialize(as = ImmutableDefaultPublicKey.class)
  @JsonDeserialize(as = ImmutableDefaultPublicKey.class)
  abstract class DefaultPublicKey implements PublicKey {

    @Override
    @Derived
    public String base58Value() {
      return AddressBase58.encode(value(), Lists.newArrayList(Version.ACCOUNT_PUBLIC_KEY), UnsignedInteger.valueOf(33));
    }

    @Override
    @Derived
    public String base16Value() {
      return this.value().hexValue();
    }

    @Override
    @Derived
    public String hexValue() {
      return base16Value();
    }

    @Derived
    @Override
    public VersionType versionType() {
      return this.hexValue().startsWith("ED") ? VersionType.ED25519 : VersionType.SECP256K1;
    }

    @Override
    public String toString() {
      return hexValue();
    }
  }

}
