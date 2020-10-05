package com.ripple.xrpl4j.codec.binary.addresses;

import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.codec.binary.UnsignedByteArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class AddressBase58 extends Base58 {

  public static String encode(UnsignedByteArray bytes, List<Version> versions, UnsignedInteger expectedLength) {
    if (expectedLength.intValue() != bytes.getUnsignedBytes().size()) {
      throw new EncodeException("Length of bytes does not match expectedLength.");
    }

    return Base58.encodeChecked(bytes.toByteArray(), versions);
  }

  public static Decoded decode(
    String base58String,
    List<Version> versions
  ) {
    return decode(base58String, new ArrayList<>(), versions, Optional.empty());
  }

  public static Decoded decode(
    String base58String,
    List<Version> versions,
    UnsignedInteger expectedLength
  ) {
    return decode(base58String, new ArrayList<>(), versions, Optional.of(expectedLength));
  }

  public static Decoded decode(
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

}
