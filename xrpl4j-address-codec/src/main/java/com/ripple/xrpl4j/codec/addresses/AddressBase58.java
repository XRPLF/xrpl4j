package com.ripple.xrpl4j.codec.addresses;

import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.codec.addresses.exceptions.DecodeException;
import com.ripple.xrpl4j.codec.addresses.exceptions.EncodeException;
import com.ripple.xrpl4j.codec.addresses.exceptions.EncodingFormatException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Extension of {@link Base58} to provide XRPL Address specific Base58 encoding and decoding functionality.
 */
public class AddressBase58 extends Base58 {

  /**
   * Encodes the given {@link UnsignedByteArray} to a Base58Check {@link String}.
   *
   * @param bytes An {@link UnsignedByteArray} of data to encode.
   * @param versions A {@link List<Version>} which should be prepended to bytes.
   * @param expectedLength The expected length of the result.
   * @return The Base58Check encoded {@link String} of the given parameters.
   */
  public static String encode(UnsignedByteArray bytes, List<Version> versions, UnsignedInteger expectedLength) {
    if (expectedLength.intValue() != bytes.getUnsignedBytes().size()) {
      throw new EncodeException("Length of bytes does not match expectedLength.");
    }

    return encodeChecked(bytes.toByteArray(), versions);
  }

  /**
   * Concatenates the given {@link Version} bytes with the given input bytes, and encodes the result as Base58Check.
   *
   * @param bytes The bytes to encode.
   * @param versions The {@link Version} to encode with.
   * @return A {@link String} containing the Base58Check encoded bytes.
   */
  public static String encodeChecked(byte[] bytes, List<Version> versions) {
    int versionsLength = 0;
    for (Version version : versions) {
      versionsLength += version.getValues().length;
    }
    byte[] versionsBytes = new byte[versionsLength];
    for (int i = 0; i < versions.size(); i++) {
      for (int j = 0; j < versions.get(i).getValues().length; j++) {
        versionsBytes[i + j] = (byte) versions.get(i).getValues()[j];
      }
    }

    byte[] bytesAndVersions = new byte[bytes.length + versionsLength];
    System.arraycopy(versionsBytes, 0, bytesAndVersions, 0, versionsLength);
    System.arraycopy(bytes, 0, bytesAndVersions, versionsLength, bytes.length);

    return encodeChecked(bytesAndVersions);
  }

  /**
   * Decode a Base58Check {@link String}.
   *
   * @param base58Value
   * @param versions
   * @return
   */
  public static Decoded decode(
    String base58Value,
    List<Version> versions
  ) {
    return decode(base58Value, new ArrayList<>(), versions, Optional.empty());
  }

  public static Decoded decode(
    String base58Value,
    List<Version> versions,
    UnsignedInteger expectedLength
  ) {
    return decode(base58Value, new ArrayList<>(), versions, Optional.of(expectedLength));
  }

  /**
   * Decode a Base58Check {@link String}.
   *
   * @param base58Value The Base58Check encoded {@link String} to be decoded.
   * @param versionTypes A {@link List} of {@link VersionType}s which can be associated with the result of this method.
   * @param versions A {@link List} of {@link Version}s to try decoding with.
   * @param expectedLength The expected length of the decoded value.
   * @return A {@link Decoded} containing the decoded value, version, and type.
   * @throws EncodingFormatException If more than one version is supplied without an expectedLength value present,
   *  or if the version bytes of the Base58 value are invalid.
   */
  public static Decoded decode(
    String base58Value,
    List<VersionType> versionTypes,
    List<Version> versions,
    Optional<UnsignedInteger> expectedLength
  ) throws EncodingFormatException {
    byte[] withoutSum = Base58.decodeChecked(base58Value);

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
