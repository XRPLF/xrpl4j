package org.xrpl.xrpl4j.codec.addresses;


import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.BaseEncoding;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.xrpl.xrpl4j.codec.addresses.exceptions.EncodeException;

import java.util.function.Function;

public class AddressCodecTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  AddressCodec addressCodec;

  private static UnsignedByteArray unsignedByteArrayFromHex(String hexValue) {
    byte[] decodedHex = BaseEncoding.base16().decode(hexValue);
    return UnsignedByteArray.of(decodedHex);
  }

  @Before
  public void setUp() {
    addressCodec = new AddressCodec();
  }

  @Test
  public void decodeEd25519Seed() {
    String seed = "sEdTM1uX8pu2do5XvTnutH6HsouMaM2";
    Decoded decoded = addressCodec.decodeSeed(seed);
    assertThat(decoded.bytes().hexValue()).isEqualTo("4C3A1D213FBDFB14C7C28D609469B341");
    assertThat(decoded.type()).isNotEmpty().get().isEqualTo(VersionType.ED25519);
    assertThat(decoded.version()).isEqualTo(Version.ED25519_SEED);
  }

  @Test
  public void decodeSecp256k1Seed() {
    String seed = "sn259rEFXrQrWyx3Q7XneWcwV6dfL";
    Decoded decoded = addressCodec.decodeSeed(seed);
    assertThat(decoded.bytes().hexValue()).isEqualTo("CF2DE378FBDD7E2EE87D486DFB5A7BFF");
    assertThat(decoded.type()).isNotEmpty().get().isEqualTo(VersionType.SECP256K1);
    assertThat(decoded.version()).isEqualTo(Version.FAMILY_SEED);
  }

  @Test
  public void encodeSecp256k1Seed() {
    String encoded = addressCodec.encodeSeed(
        unsignedByteArrayFromHex("CF2DE378FBDD7E2EE87D486DFB5A7BFF"),
        VersionType.SECP256K1
    );

    assertThat(encoded).isEqualTo("sn259rEFXrQrWyx3Q7XneWcwV6dfL");
  }

  @Test
  public void encodeLowSecp256k1Seed() {
    String encoded = addressCodec.encodeSeed(
        unsignedByteArrayFromHex("00000000000000000000000000000000"),
        VersionType.SECP256K1
    );

    assertThat(encoded).isEqualTo("sp6JS7f14BuwFY8Mw6bTtLKWauoUs");
  }

  @Test
  public void encodeHighSecp256k1Seed() {
    String encoded = addressCodec.encodeSeed(
        unsignedByteArrayFromHex("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"),
        VersionType.SECP256K1
    );

    assertThat(encoded).isEqualTo("saGwBRReqUNKuWNLpUAq8i8NkXEPN");
  }

  @Test
  public void encodeEd25519Seed() {
    String encoded = addressCodec.encodeSeed(
        unsignedByteArrayFromHex("4C3A1D213FBDFB14C7C28D609469B341"),
        VersionType.ED25519
    );

    assertThat(encoded).isEqualTo("sEdTM1uX8pu2do5XvTnutH6HsouMaM2");
  }

  @Test
  public void encodeLowEd25519Seed() {
    String encoded = addressCodec.encodeSeed(
        unsignedByteArrayFromHex("00000000000000000000000000000000"),
        VersionType.ED25519
    );

    assertThat(encoded).isEqualTo("sEdSJHS4oiAdz7w2X2ni1gFiqtbJHqE");
  }

  @Test
  public void encodeHighEd25519Seed() {
    String encoded = addressCodec.encodeSeed(
        unsignedByteArrayFromHex("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"),
        VersionType.ED25519
    );

    assertThat(encoded).isEqualTo("sEdV19BLfeQeKdEXyYA4NhjPJe6XBfG");
  }

  @Test
  public void encodeSeedWithFewerThanSixteenBytes() {
    expectedException.expect(EncodeException.class);
    expectedException.expectMessage("entropy must have length 16.");
    addressCodec.encodeSeed(unsignedByteArrayFromHex("CF2DE378FBDD7E2EE87D486DFB5A7B"), VersionType.SECP256K1);
  }

  @Test
  public void encodeSeedWithGreaterThanSixteenBytes() {
    expectedException.expect(EncodeException.class);
    expectedException.expectMessage("entropy must have length 16.");
    addressCodec.encodeSeed(unsignedByteArrayFromHex("CF2DE378FBDD7E2EE87D486DFB5A7BFFFF"), VersionType.SECP256K1);
  }

  @Test
  public void encodeDecodeAccountId() {
    testEncodeDecode(
        accountId -> addressCodec.encodeAccountId(accountId),
        accountId -> addressCodec.decodeAccountId(accountId),
        unsignedByteArrayFromHex("BA8E78626EE42C41B46D46C3048DF3A1C3C87072"),
        "rJrRMgiRgrU6hDF4pgu5DXQdWyPbY35ErN"
    );
  }

  @Test
  public void encodeDecodeNodePublic() {
    testEncodeDecode(
        nodePublic -> addressCodec.encodeNodePublicKey(nodePublic),
        nodePublic -> addressCodec.decodeNodePublicKey(nodePublic),
        unsignedByteArrayFromHex("0388E5BA87A000CB807240DF8C848EB0B5FFA5C8E5A521BC8E105C0F0A44217828"),
        "n9MXXueo837zYH36DvMc13BwHcqtfAWNJY5czWVbp7uYTj7x17TH"
    );
  }

  @Test
  public void encodeDecodeAccountPublicKey() {
    testEncodeDecode(
        publicKey -> addressCodec.encodeAccountPublicKey(publicKey),
        publicKey -> addressCodec.decodeAccountPublicKey(publicKey),
        unsignedByteArrayFromHex("023693F15967AE357D0327974AD46FE3C127113B1110D6044FD41E723689F81CC6"),
        "aB44YfzW24VDEJQ2UuLPV2PvqcPCSoLnL7y5M1EzhdW4LnK5xMS3"
    );
  }

  private void testEncodeDecode(
      Function<UnsignedByteArray, String> encoder,
      Function<String, UnsignedByteArray> decoder,
      UnsignedByteArray bytes,
      String base58
  ) {
    String encoded = encoder.apply(bytes);
    assertThat(encoded).isEqualTo(base58);

    UnsignedByteArray decoded = decoder.apply(base58);
    assertThat(decoded).isEqualTo(bytes);
  }
}
