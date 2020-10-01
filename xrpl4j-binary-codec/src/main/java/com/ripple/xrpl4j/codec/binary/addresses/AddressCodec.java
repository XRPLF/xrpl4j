package com.ripple.xrpl4j.codec.binary.addresses;

import com.google.common.collect.Lists;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.codec.binary.UnsignedByte;
import com.ripple.xrpl4j.codec.binary.UnsignedByteArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class AddressCodec {

  public String encodeSeed(List<UnsignedByte> entropy, VersionType type) {
    return null;
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

  public Decoded decodeSeed(
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
    Version version,
    Optional<UnsignedInteger> expectedLength
  ) throws EncodingFormatException {
    return decodeSeed(seed, versionTypes, Lists.newArrayList(version), expectedLength);
  }

  public Decoded decodedSeed(String seed) throws EncodingFormatException {
    return decodeSeed(
      seed,
      Lists.newArrayList(VersionType.ED25519, VersionType.SECP256K1),
      Lists.newArrayList(Version.ED25519_SEED, Version.FAMILY_SEED),
      Optional.of(UnsignedInteger.valueOf(16))
    );
  }
}
