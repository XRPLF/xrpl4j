package com.ripple.xrpl4j.codec.binary.addresses;

import com.google.common.collect.Lists;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.codec.binary.UnsignedByteArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class AddressCodec {

  protected String encode(UnsignedByteArray bytes, List<Version> versions, UnsignedInteger expectedLength) {
    if (expectedLength.intValue() != bytes.getUnsignedBytes().size()) {
      throw new EncodeException("Length of bytes does not match expectedLength.");
    }

    return Base58.encodeChecked(bytes.toByteArray(), versions);
  }

  protected Decoded decode(
    String base58String,
    List<Version> versions
  ) {
    return decode(base58String, new ArrayList<>(), versions, Optional.empty());
  }

  protected Decoded decode(
    String base58String,
    List<Version> versions,
    List<VersionType> versionTypes
  ) {
    return decode(base58String, versionTypes, versions, Optional.empty());
  }

  protected Decoded decode(
    String base58String,
    List<Version> versions,
    UnsignedInteger expectedLength
  ) {
    return decode(base58String, new ArrayList<>(), versions, Optional.of(expectedLength));
  }

  protected Decoded decode(
    String base58String,
    List<VersionType> versionTypes,
    List<Version> versions,
    Optional<UnsignedInteger> expectedLength
  ) throws EncodingFormatException {
    byte[] withoutSum = Base58.decodeChecked(base58String);

    if (versions.size() > 1 && !expectedLength.isPresent()) {
      throw new DecodeException("expectedLength is required because there are >= 2 possible versions");
    }

    int versionLengthGuess = versions.get(0).getValues().length;
    int payloadLength = expectedLength
      .map(UnsignedInteger::intValue)
      .orElse(withoutSum.length - versionLengthGuess);

    byte[] versionBytes = Arrays.copyOfRange(
      withoutSum,
      0,
      withoutSum.length - payloadLength
    );

    byte[] payload = Arrays.copyOfRange(
      withoutSum,
      withoutSum.length - payloadLength,
      withoutSum.length
    );

    for (int i = 0; i < versions.size(); i++) {
      Version version = versions.get(i);
      if (Arrays.equals(versionBytes, version.getValuesAsBytes())) {
        return Decoded.builder()
          .version(version)
          .bytes(UnsignedByteArray.of(payload))
          .type(i < versionTypes.size() ? Optional.of(versionTypes.get(i)) : Optional.empty())
          .build();
      }
    }

    throw new DecodeException("Version is invalid. Version bytes do not match any of the provided versions.");
  }

  protected Decoded decodeSeed(
    String seed,
    List<VersionType> versionTypes,
    List<Version> versions,
    Optional<UnsignedInteger> expectedLength
  ) throws EncodingFormatException {
    return decode(seed, versionTypes, versions, expectedLength);
  }

  public Decoded decodeSeed(
    String seed,
    List<VersionType> versionTypes,
    Version version
  ) throws EncodingFormatException {
    return decodeSeed(seed, versionTypes, Lists.newArrayList(version), Optional.empty());
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
