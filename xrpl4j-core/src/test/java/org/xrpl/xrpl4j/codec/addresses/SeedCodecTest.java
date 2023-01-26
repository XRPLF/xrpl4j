package org.xrpl.xrpl4j.codec.addresses;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.exceptions.EncodeException;

/**
 * Unit tests for {@link SeedCodec}.
 */
class SeedCodecTest extends AbstractCodecTest {

  private SeedCodec seedCodec;

  @BeforeEach
  void setUp() {
    seedCodec = new SeedCodec();
  }

  @Test
  void getInstance() {
    assertThat(SeedCodec.getInstance()).isNotNull();
  }

  @Test
  public void decodeEd25519Seed() {
    String seed = "sEdTM1uX8pu2do5XvTnutH6HsouMaM2";
    Decoded decoded = seedCodec.decodeSeed(seed);
    assertThat(decoded.bytes().hexValue()).isEqualTo("4C3A1D213FBDFB14C7C28D609469B341");
    assertThat(decoded.type()).isNotEmpty().get().isEqualTo(KeyType.ED25519);
    assertThat(decoded.version()).isEqualTo(Version.ED25519_SEED);
  }

  @Test
  public void decodeSecp256k1Seed() {
    String seed = "sn259rEFXrQrWyx3Q7XneWcwV6dfL";
    Decoded decoded = seedCodec.decodeSeed(seed);
    assertThat(decoded.bytes().hexValue()).isEqualTo("CF2DE378FBDD7E2EE87D486DFB5A7BFF");
    assertThat(decoded.type()).isNotEmpty().get().isEqualTo(KeyType.SECP256K1);
    assertThat(decoded.version()).isEqualTo(Version.FAMILY_SEED);
  }

  @Test
  public void encodeSecp256k1Seed() {
    String encoded = seedCodec.encodeSeed(
      unsignedByteArrayFromHex("CF2DE378FBDD7E2EE87D486DFB5A7BFF"),
      KeyType.SECP256K1
    );

    assertThat(encoded).isEqualTo("sn259rEFXrQrWyx3Q7XneWcwV6dfL");
  }

  @Test
  public void encodeLowSecp256k1Seed() {
    String encoded = seedCodec.encodeSeed(
      unsignedByteArrayFromHex("00000000000000000000000000000000"),
      KeyType.SECP256K1
    );

    assertThat(encoded).isEqualTo("sp6JS7f14BuwFY8Mw6bTtLKWauoUs");
  }

  @Test
  public void encodeHighSecp256k1Seed() {
    String encoded = seedCodec.encodeSeed(
      unsignedByteArrayFromHex("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"),
      KeyType.SECP256K1
    );

    assertThat(encoded).isEqualTo("saGwBRReqUNKuWNLpUAq8i8NkXEPN");
  }

  @Test
  public void encodeEd25519Seed() {
    String encoded = seedCodec.encodeSeed(
      unsignedByteArrayFromHex("4C3A1D213FBDFB14C7C28D609469B341"),
      KeyType.ED25519
    );

    assertThat(encoded).isEqualTo("sEdTM1uX8pu2do5XvTnutH6HsouMaM2");
  }

  @Test
  public void encodeLowEd25519Seed() {
    String encoded = seedCodec.encodeSeed(
      unsignedByteArrayFromHex("00000000000000000000000000000000"),
      KeyType.ED25519
    );

    assertThat(encoded).isEqualTo("sEdSJHS4oiAdz7w2X2ni1gFiqtbJHqE");
  }

  @Test
  public void encodeHighEd25519Seed() {
    String encoded = seedCodec.encodeSeed(
      unsignedByteArrayFromHex("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"),
      KeyType.ED25519
    );

    assertThat(encoded).isEqualTo("sEdV19BLfeQeKdEXyYA4NhjPJe6XBfG");
  }

  @Test
  public void encodeSeedWithFewerThanSixteenBytes() {
    assertThrows(
      EncodeException.class,
      () -> seedCodec.encodeSeed(unsignedByteArrayFromHex("CF2DE378FBDD7E2EE87D486DFB5A7B"), KeyType.SECP256K1),
      "entropy must have length 16."
    );
  }

  @Test
  public void encodeSeedWithGreaterThanSixteenBytes() {
    assertThrows(
      EncodeException.class,
      () -> seedCodec
        .encodeSeed(unsignedByteArrayFromHex("CF2DE378FBDD7E2EE87D486DFB5A7BFFFF"), KeyType.SECP256K1),
      "entropy must have length 16."
    );
  }
}