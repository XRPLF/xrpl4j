package org.xrpl.xrpl4j.codec.addresses;

import static java.util.Arrays.copyOfRange;

import com.google.common.collect.Lists;
import com.google.common.primitives.UnsignedInteger;
import org.xrpl.xrpl4j.codec.addresses.exceptions.DecodeException;
import org.xrpl.xrpl4j.codec.addresses.exceptions.EncodeException;
import org.xrpl.xrpl4j.codec.addresses.exceptions.EncodingFormatException;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.XAddress;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "ParameterName", "MethodName"})
public class AddressCodec {

  private static final AddressCodec INSTANCE = new AddressCodec();

  public static AddressCodec getInstance() {
    return INSTANCE;
  }

  /**
   * Decodes a Base58Check encoded XRPL secret key seed value. Works for ed25519 and secp256k1 seeds.
   *
   * @param seed A Base58Check encoded XRPL keypair seed.
   * @return The decoded seed, seed type, and algorithm used to encode the seed.
   * @see "https://xrpl.org/cryptographic-keys.html#seed"
   */
  public Decoded decodeSeed(final String seed) throws EncodingFormatException {
    Objects.requireNonNull(seed);

    return AddressBase58.decode(
        seed,
        Lists.newArrayList(VersionType.ED25519, VersionType.SECP256K1),
        Lists.newArrayList(Version.ED25519_SEED, Version.FAMILY_SEED),
        Optional.of(UnsignedInteger.valueOf(16))
    );
  }

  /**
   * Encodes a byte array to a Base58Check {@link String} using the given {@link VersionType}.
   *
   * @param entropy An {@link UnsignedByteArray} containing the seed entropy to encode.
   * @param type    The cryptographic algorithm type to be encoded in the resulting seed.
   * @return A Base58Check encoded XRPL keypair seed.
   */
  public String encodeSeed(final UnsignedByteArray entropy, final VersionType type) {
    Objects.requireNonNull(entropy);
    Objects.requireNonNull(type);

    if (entropy.getUnsignedBytes().size() != 16) {
      throw new EncodeException("entropy must have length 16.");
    }

    Version version = type.equals(VersionType.ED25519) ? Version.ED25519_SEED : Version.FAMILY_SEED;
    return AddressBase58.encode(entropy, Lists.newArrayList(version), UnsignedInteger.valueOf(16));
  }

  /**
   * Encode an XRPL AccountID to a Base58Check encoded {@link String}.
   *
   * @param accountId An {@link UnsignedByteArray} containing the AccountID to be encoded.
   * @return The Base58 representation of accountId, as an {@link Address}.
   */
  public Address encodeAccountId(final UnsignedByteArray accountId) {
    Objects.requireNonNull(accountId);

    return Address.of(
      AddressBase58.encode(accountId, Lists.newArrayList(Version.ACCOUNT_ID), UnsignedInteger.valueOf(20))
    );
  }

  /**
   * Decode a Base58Check encoded XRPL AccountID.
   *
   * @param accountId The Base58 encoded AccountID to be decoded, as an {@link Address}.
   * @return An {@link UnsignedByteArray} containing the decoded AccountID.
   * @see "https://xrpl.org/base58-encodings.html"
   */
  public UnsignedByteArray decodeAccountId(final Address accountId) {
    Objects.requireNonNull(accountId);

    return AddressBase58.decode(
      accountId.value(),
      Lists.newArrayList(Version.ACCOUNT_ID),
      UnsignedInteger.valueOf(20)
    ).bytes();
  }

  /**
   * Encode an XRPL Node Public Key to a Base58Check encoded {@link String}.
   *
   * @param publicKey An {@link UnsignedByteArray} containing the public key to be encoded.
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
   * @return An {@link UnsignedByteArray} containing the decoded public key.
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
   * @return An {@link UnsignedByteArray} containing the decoded public key.
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

  /**
   * Converts an XRPL Classic Address and Destination Tag to an X-Address.
   *
   * @param classicAddress A {@link String} containing the classic address.
   * @param tag            The destination tag of the address.
   * @param test           {@code true} if the X-Address should be encoded for Testnet, {@code false} if it should be encoded
   *                       for Mainnet.
   * @return The X-Address representation of the classic address and destination tag.
   */
  public XAddress classicAddressToXAddress(
    final Address classicAddress,
    final UnsignedInteger tag,
    final boolean test
  ) {
    Objects.requireNonNull(classicAddress);
    Objects.requireNonNull(tag);

    return classicAddressToXAddress(classicAddress, Optional.of(tag), test);
  }

  /**
   * Converts an XRPL Classic Address with no Destination Tag to an X-Address.
   *
   * @param classicAddress An {@link Address} containing the classic address.
   * @param test           {@code true} if the X-Address should be encoded for Testnet,
   *                       {@code false} if it should be encoded for Mainnet.
   * @return The X-Address representation of the classic address, as an {@link XAddress}.
   */
  public XAddress classicAddressToXAddress(final Address classicAddress, final boolean test) {
    Objects.requireNonNull(classicAddress);

    return classicAddressToXAddress(classicAddress, Optional.empty(), test);
  }

  /**
   * Converts an XRPL Classic Address and and optional Destination Tag to an X-Address.
   *
   * @param classicAddress An {@link Address} containing the classic address.
   * @param tag            The destination tag of the address.
   * @param test           {@code true} if the X-Address should be encoded for Testnet,
   *                       {@code false} if it should be encoded for Mainnet.
   * @return The X-Address representation of the classic address and destination tag, as an {@link XAddress}.
   */
  public XAddress classicAddressToXAddress(
      final Address classicAddress,
      final Optional<UnsignedInteger> tag,
      final boolean test
  ) {
    Objects.requireNonNull(classicAddress);
    Objects.requireNonNull(tag);

    UnsignedByteArray accountId = decodeAccountId(classicAddress);
    return encodeXAddress(accountId, tag, test);
  }

  /**
   * Encodes and AccountID, destination tag, and the XRPL network into an X-Address.
   *
   * @param accountId An {@link UnsignedByteArray} containing an XRPL AccountID.
   * @param tag       (Optional) The destination tag of the account.
   * @param test      {@code true} if the X-Address should be encoded for Testnet,
   *                  {@code false} if it should be encoded for Mainnet.
   * @return The X-Address representation of the AccountID and destination tag, as an {@link XAddress}.
   */
  private XAddress encodeXAddress(
      final UnsignedByteArray accountId,
      final Optional<UnsignedInteger> tag,
      final boolean test
  ) {
    Objects.requireNonNull(accountId);
    Objects.requireNonNull(tag);

    if (accountId.getUnsignedBytes().size() != 20) {
      throw new EncodeException("AccountID must be 20 bytes.");
    }

    int flag;
    UnsignedInteger normalizedTag;
    if (tag.isPresent()) {
      flag = 1;
      normalizedTag = tag.get();
    } else {
      flag = 0;
      normalizedTag = UnsignedInteger.ZERO;
    }

    UnsignedByteArray bytes = UnsignedByteArray.of(test ? PrefixBytes.TEST : PrefixBytes.MAIN)
        .append(accountId)
        .append(UnsignedByteArray.of(new byte[] {
            (byte) flag,
            (byte) (normalizedTag.intValue() & 0xff),
            (byte) ((normalizedTag.intValue() >>> 8) & 0xff),
            (byte) ((normalizedTag.intValue() >>> 16) & 0xff),
            (byte) ((normalizedTag.intValue() >>> 24) & 0xff),
            0, 0, 0, 0 // Four zero bytes reserved for 64-bit tags
        }));

    return XAddress.of(Base58.encodeChecked(bytes.toByteArray()));
  }

  /**
   * Decodes an X-Address to a Classic Address and Destination Tag.
   *
   * @param xAddress The {@link XAddress} to be decoded.
   * @return The {@link ClassicAddress} decoded from xAddress.
   */
  public ClassicAddress xAddressToClassicAddress(final XAddress xAddress) {
    Objects.requireNonNull(xAddress);

    DecodedXAddress decodedXAddress = decodeXAddress(xAddress);
    Address classicAddress = encodeAccountId(decodedXAddress.accountId());

    return ClassicAddress.builder()
        .classicAddress(classicAddress)
        .tag(decodedXAddress.tag())
        .test(decodedXAddress.test())
        .build();
  }

  /**
   * Decodes an X-Address to an AccountID, destination tag, and a boolean for XRPL-testnet or XRPL-mainnet.
   *
   * @param xAddress The {@link XAddress} to be decoded.
   * @return The {@link DecodedXAddress} decoded from xAddress.
   */
  private DecodedXAddress decodeXAddress(final XAddress xAddress) {
    Objects.requireNonNull(xAddress);

    byte[] decoded = Base58.decodeChecked(xAddress.value());
    boolean test = isTestAddress(decoded);
    byte[] accountId = copyOfRange(decoded, 2, 22);
    UnsignedInteger tag = tagFromDecodedXAddress(decoded);

    return DecodedXAddress.builder()
        .accountId(UnsignedByteArray.of(accountId))
        .tag(tag)
        .test(test)
        .build();
  }

  private UnsignedInteger tagFromDecodedXAddress(final byte[] decoded) {
    Objects.requireNonNull(decoded);

    byte flag = decoded[22];
    if (flag >= 2) {
      throw new DecodeException("Unsupported X-Address: 64-bit tags are not supported");
    }

    if (flag == 1) {
      // Little-endian to big-endian
      return UnsignedInteger.valueOf(decoded[23] & 0xff)
          .plus(UnsignedInteger.valueOf((decoded[24] & 0xff) * 0x100))
          .plus(UnsignedInteger.valueOf((decoded[25] & 0xff) * 0x10000))
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

  private boolean isTestAddress(final byte[] decoded) {
    Objects.requireNonNull(decoded);

    byte[] prefix = copyOfRange(decoded, 0, 2);
    if (Arrays.equals(PrefixBytes.MAIN, prefix)) {
      return false;
    } else if (Arrays.equals(PrefixBytes.TEST, prefix)) {
      return true;
    } else {
      throw new DecodeException("Invalid X-Address: Bad Prefix");
    }
  }

  /**
   * Tests if the given X-Address is a valid X-Address.
   *
   * @param xAddress A potentially valid {@link XAddress}.
   * @return {@code true} if the given address is a valid X-Address, {@code false} if not.
   */
  public boolean isValidXAddress(final XAddress xAddress) {
    try {
      decodeXAddress(xAddress);
    } catch (Exception e) {
      return false;
    }

    return true;
  }

  /**
   * Tests if the given Address is a valid Classic Address.
   *
   * @param address A potentially valid classic {@link Address}.
   * @return {@code true} if the given address is a valid Classic Address, {@code false} if not.
   */
  public boolean isValidClassicAddress(final Address address) {
    try {
      decodeAccountId(address);
    } catch (Exception e) {
      return false;
    }

    return true;
  }

  private static final class PrefixBytes {
    static byte[] MAIN = new byte[] {0x05, 0x44};
    static byte[] TEST = new byte[] {0x04, (byte) 0x93};
  }
}
