package com.ripple.xrpl4j.codec.binary.addresses;

import static com.ripple.xrpl4j.codec.binary.addresses.AddressBase58.decode;
import static com.ripple.xrpl4j.codec.binary.addresses.AddressBase58.encode;

import com.google.common.collect.Lists;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.codec.binary.UnsignedByteArray;

import java.util.List;
import java.util.Optional;

public class AddressCodec {

  protected Decoded decodeSeed(
    String seed,
    List<VersionType> versionTypes,
    List<Version> versions,
    Optional<UnsignedInteger> expectedLength
  ) throws EncodingFormatException {
    return decode(seed, versionTypes, versions, expectedLength);
  }

  public Decoded decodeSeed(String seed) throws EncodingFormatException {
    return decodeSeed(
      seed,
      Lists.newArrayList(VersionType.ED25519, VersionType.SECP256K1),
      Lists.newArrayList(Version.ED25519_SEED, Version.FAMILY_SEED),
      Optional.of(UnsignedInteger.valueOf(16))
    );
  }

  public String encodeSeed(UnsignedByteArray entropy, VersionType type) {
    if (entropy.getUnsignedBytes().size() != 16) {
      throw new EncodeException("entropy must have length 16.");
    }

    Version version = type.equals(VersionType.ED25519) ? Version.ED25519_SEED : Version.FAMILY_SEED;
    return encode(entropy, Lists.newArrayList(version), UnsignedInteger.valueOf(16));
  }

  public String encodeAccountId(UnsignedByteArray bytes) {
    return encode(bytes, Lists.newArrayList(Version.ACCOUNT_ID), UnsignedInteger.valueOf(20));
  }

  public UnsignedByteArray decodeAccountId(String accountId) {
    return decode(accountId, Lists.newArrayList(Version.ACCOUNT_ID), UnsignedInteger.valueOf(20)).bytes();
  }

  public String encodeNodePublic(UnsignedByteArray bytes) {
    return encode(bytes, Lists.newArrayList(Version.NODE_PUBLIC), UnsignedInteger.valueOf(33));
  }

  public UnsignedByteArray decodeNodePublic(String node) {
    return decode(node, Lists.newArrayList(Version.NODE_PUBLIC), UnsignedInteger.valueOf(33)).bytes();
  }

  public String encodeAccountPublic(UnsignedByteArray bytes) {
    return encode(bytes, Lists.newArrayList(Version.ACCOUNT_PUBLIC_KEY), UnsignedInteger.valueOf(33));
  }

  public UnsignedByteArray decodeAccountPublic(String publicKey) {
    return decode(publicKey, Lists.newArrayList(Version.ACCOUNT_PUBLIC_KEY), UnsignedInteger.valueOf(33)).bytes();
  }
}
