package org.xrpl.xrpl4j.codec.addresses;

import com.google.common.collect.Lists;
import com.google.common.primitives.UnsignedInteger;

import java.util.Objects;

/**
 * A Codec for encoding/decoding various seed primitives.
 */
@SuppressWarnings( {"OptionalUsedAsFieldOrParameterType", "ParameterName", "MethodName"})
public class PublicKeyCodec {

  private static final PublicKeyCodec INSTANCE = new PublicKeyCodec();

  public static PublicKeyCodec getInstance() {
    return INSTANCE;
  }

  /**
   * Encode an XRPL Node Public Key to a Base58Check encoded {@link String}.
   *
   * @param publicKey An {@link UnsignedByteArray} containing the public key to be encoded.
   *
   * @return The Base58 representation of publicKey.
   */
  public String encodeNodePublicKey(final UnsignedByteArray publicKey) {
    Objects.requireNonNull(publicKey);
    return AddressBase58.encode(publicKey, Lists.newArrayList(Version.NODE_PUBLIC), UnsignedInteger.valueOf(33));
  }

  /**
   * Decode a Base58Check encoded XRPL Node Public Key.
   *
   * @param publicKey The Base58 encoded public key to be decoded.
   *
   * @return An {@link UnsignedByteArray} containing the decoded public key.
   *
   * @see "https://xrpl.org/base58-encodings.html"
   */
  public UnsignedByteArray decodeNodePublicKey(final String publicKey) {
    Objects.requireNonNull(publicKey);

    return AddressBase58.decode(
      publicKey,
      Lists.newArrayList(Version.NODE_PUBLIC),
      UnsignedInteger.valueOf(33)
    ).bytes();
  }

  /**
   * Encode an XRPL Account Public Key to a Base58Check encoded {@link String}.
   *
   * @param publicKey An {@link UnsignedByteArray} containing the public key to be encoded.
   *
   * @return The Base58 representation of publicKey.
   */
  public String encodeAccountPublicKey(final UnsignedByteArray publicKey) {
    Objects.requireNonNull(publicKey);

    return AddressBase58.encode(publicKey, Lists.newArrayList(Version.ACCOUNT_PUBLIC_KEY), UnsignedInteger.valueOf(33));
  }

  /**
   * Decode a Base58Check encoded XRPL Account Public Key.
   *
   * @param publicKey The Base58 encoded public key to be decoded.
   *
   * @return An {@link UnsignedByteArray} containing the decoded public key.
   *
   * @see "https://xrpl.org/base58-encodings.html"
   */
  public UnsignedByteArray decodeAccountPublicKey(final String publicKey) {
    Objects.requireNonNull(publicKey);

    return AddressBase58.decode(
      publicKey,
      Lists.newArrayList(Version.ACCOUNT_PUBLIC_KEY),
      UnsignedInteger.valueOf(33)
    ).bytes();
  }

}
