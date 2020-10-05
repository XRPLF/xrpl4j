package com.ripple.xrpl4j.codec.binary.addresses;

import static com.ripple.xrpl4j.codec.binary.addresses.AddressBase58.decode;
import static com.ripple.xrpl4j.codec.binary.addresses.AddressBase58.encode;
import static java.util.Arrays.copyOfRange;

import com.google.common.collect.Lists;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.codec.binary.UnsignedByteArray;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class AddressCodec {

  private static final class PrefixBytes {
    static byte[] MAIN = new byte[] { 0x05, 0x44 };
    static byte[] TEST = new byte[] { 0x04, (byte) 0x93};
  }

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

  public String encodeAccountPublicKey(UnsignedByteArray bytes) {
    return encode(bytes, Lists.newArrayList(Version.ACCOUNT_PUBLIC_KEY), UnsignedInteger.valueOf(33));
  }

  public UnsignedByteArray decodeAccountPublicKey(String publicKey) {
    return decode(publicKey, Lists.newArrayList(Version.ACCOUNT_PUBLIC_KEY), UnsignedInteger.valueOf(33)).bytes();
  }

  public String classicAddressToXAddress(String classicAddress, UnsignedInteger tag, boolean test) {
    return classicAddressToXAddress(classicAddress, Optional.of(tag), test);
  }

  public String classicAddressToXAddress(String classicAddress, boolean test) {
    return classicAddressToXAddress(classicAddress, Optional.empty(), test);
  }

  public String classicAddressToXAddress(String classicAddress, Optional<UnsignedInteger> tag, boolean test) {
    UnsignedByteArray accountId = decodeAccountId(classicAddress);
    return encodeXAddress(accountId, tag, test);
  }

  private String encodeXAddress(UnsignedByteArray accountId, Optional<UnsignedInteger> tag, boolean test) {
    if (accountId.getUnsignedBytes().size() != 20) {
      throw new EncodeException("AccountID must be 20 bytes.");
    }

    int flag;
    if (tag.isPresent()) {
      flag = 1;
    } else {
      flag = 0;
      tag = Optional.of(UnsignedInteger.ZERO);
    }

    UnsignedByteArray bytes = UnsignedByteArray.of(test ? PrefixBytes.TEST : PrefixBytes.MAIN)
      .concat(accountId)
      .concat(UnsignedByteArray.of(new byte[] {
        (byte) flag,
        (byte) (tag.get().intValue() & 0xff),
        (byte) ((tag.get().intValue() >>> 8) & 0xff),
        (byte) ((tag.get().intValue() >>> 16) & 0xff),
        (byte) ((tag.get().intValue() >>> 24) & 0xff),
        0, 0, 0, 0 // Four zero bytes reserved for 64-bit tags
      }));

    return Base58.encodeChecked(bytes.toByteArray());
  }

  public ClassicAddress xAddressToClassicAddress(String xAddress) {
    DecodedXAddress decodedXAddress = decodeXAddress(xAddress);
    String classicAddress = encodeAccountId(decodedXAddress.accountId());

    return ClassicAddress.builder()
      .classicAddress(classicAddress)
      .tag(decodedXAddress.tag())
      .test(decodedXAddress.test())
      .build();
  }

  private DecodedXAddress decodeXAddress(String xAddress) {
    byte[] decoded = Base58.decodeChecked(xAddress);
    boolean test = isTestAddress(decoded);
    byte[] accountId = copyOfRange(decoded, 2, 22);
    UnsignedInteger tag = tagFromDecodedXAddress(decoded);

    return DecodedXAddress.builder()
      .accountId(UnsignedByteArray.of(accountId))
      .tag(tag)
      .test(test)
      .build();
  }

  private UnsignedInteger tagFromDecodedXAddress(byte[] decoded) {
    byte flag = decoded[22];
    if (flag >= 2) {
      throw new DecodeException("Unsupported X-Address: 64-bit tags are not supported");
    }

    if (flag == 1) {
      // Little-endian to big-endian
      return UnsignedInteger.valueOf(decoded[23] & 0xff)
        .plus(UnsignedInteger.valueOf((decoded[24] & 0xff) * 0x100))
        .plus(UnsignedInteger.valueOf((decoded[25]& 0xff) * 0x10000))
        .plus(UnsignedInteger.valueOf(0x1000000).times(UnsignedInteger.valueOf(decoded[26] & 0xff)));
    } else if (flag == 0) {
      byte[] endBytes = new byte[8];
      Arrays.fill(endBytes, (byte) 0);
      if (!Arrays.equals(copyOfRange(decoded, 23, 31), endBytes)) {
        throw new DecodeException("Tag bytes in XAddress must be 0 if the address has no tag.");
      } else {
        return UnsignedInteger.ZERO;
      }
    } else {
      throw new DecodeException("Flag must be 0 to indicate no tag.");
    }
  }

  private boolean isTestAddress(byte[] decoded) {
    byte[] prefix = copyOfRange(decoded, 0, 2);
    if (Arrays.equals(PrefixBytes.MAIN, prefix)) {
      return false;
    } else if (Arrays.equals(PrefixBytes.TEST, prefix)) {
      return true;
    } else {
      throw new DecodeException("Invalid X-Address: Bad Prefix");
    }
  }


  public boolean isValidXAddress(String xAddress) {
    try {
      decodeXAddress(xAddress);
    } catch (Exception e) {
      return false;
    }

    return true;
  }

  public boolean isValidClassicAddress(String address) {
    try {
      decodeAccountId(address);
    } catch (Exception e) {
      return false;
    }

    return true;
  }
}
