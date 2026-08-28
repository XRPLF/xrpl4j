package org.xrpl.xrpl4j.codec.addresses;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: core
 * %%
 * Copyright (C) 2020 - 2023 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.io.BaseEncoding;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.exceptions.DecodeException;
import org.xrpl.xrpl4j.codec.addresses.exceptions.EncodeException;
import org.xrpl.xrpl4j.crypto.keys.Entropy;

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
  public void decodeSeedRejectsInvalidLength() {
    // Only 29-, 31-, and 51-character seeds are decodable; any other length is rejected overtly, up front.
    // Cases: empty; the 29-char secp256k1 seed minus one char (28); the 31-char ed25519 seed plus one char (32).
    for (String badSeed : new String[] {"", "sn259rEFXrQrWyx3Q7XneWcwV6df", "sEdTM1uX8pu2do5XvTnutH6HsouMaM2X"}) {
      DecodeException thrown = assertThrows(DecodeException.class, () -> seedCodec.decodeSeed(badSeed));
      assertThat(thrown.getMessage()).contains("Invalid seed length");
    }
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
  public void encodeDecodeElGamalSeed() {
    Entropy entropy = Entropy.of(
      BaseEncoding.base16().decode("4D4BD86DD8503732AB0B96C2D8DF13AC9D390D4337A83144427AC7A12145DBF4")
    );
    String encoded = seedCodec.encodeSeed(unsignedByteArrayFromHex(
      entropy.value().hexValue()), KeyType.SECP256K1);
    String decoded = seedCodec.decodeSeed(encoded).bytes().hexValue();
    assertThat(decoded).isEqualTo(entropy.value().hexValue());
  }

  /**
   * 32-byte entropy exists only for ElGamal secp256k1 seeds. Under the ED25519 version prefix it encodes to a
   * 53-character seed that {@link SeedCodec#decodeSeed(String)} cannot decode, so it must be rejected at encode time
   * rather than returned as unrecoverable key material.
   */
  @Test
  public void encodeSeedRejectsThirtyTwoByteEntropyForEd25519() {
    // The guard is gated on SECP256K1 (not "exclude ED25519"), so any non-SECP256K1 type is rejected for 32-byte
    // entropy; ED25519 is the only such type today. Assert the message to lock that SECP256K1-gated intent.
    EncodeException thrown = assertThrows(
      EncodeException.class,
      () -> seedCodec.encodeSeed(
        unsignedByteArrayFromHex("4D4BD86DD8503732AB0B96C2D8DF13AC9D390D4337A83144427AC7A12145DBF4"),
        KeyType.ED25519
      )
    );
    assertThat(thrown).hasMessageContaining("32-byte entropy is only supported for SECP256K1 seeds");
  }

  /**
   * Guards the round-trip property this codec must hold: every seed {@link SeedCodec#encodeSeed} accepts must be
   * decodable back to the same entropy and key type.
   */
  @Test
  public void everyEncodableSeedIsDecodable() {
    String sixteen = "CF2DE378FBDD7E2EE87D486DFB5A7BFF";
    String thirtyTwo = "4D4BD86DD8503732AB0B96C2D8DF13AC9D390D4337A83144427AC7A12145DBF4";

    for (Object[] testCase : new Object[][] {
      {sixteen, KeyType.ED25519}, {sixteen, KeyType.SECP256K1}, {thirtyTwo, KeyType.SECP256K1}
    }) {
      String hex = (String) testCase[0];
      KeyType keyType = (KeyType) testCase[1];

      Decoded decoded = seedCodec.decodeSeed(seedCodec.encodeSeed(unsignedByteArrayFromHex(hex), keyType));

      assertThat(decoded.bytes().hexValue()).isEqualTo(hex);
      assertThat(decoded.type()).isPresent().get().isEqualTo(keyType);
    }
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

  @Test
  public void encodeElGamalSeedWithLessThanThirtyTwoBytes() {
    assertThrows(
      EncodeException.class,
      () -> seedCodec
        .encodeSeed(unsignedByteArrayFromHex("CF2DE378FBDD7E2EE87D486DFB5A7BFFFF"), KeyType.SECP256K1),
      "entropy must have length 32."
    );
  }

  @Test
  public void encodeElGamalSeedWithMoreThanThirtyTwoBytes() {
    assertThrows(
      EncodeException.class,
      () -> seedCodec
        .encodeSeed(
          unsignedByteArrayFromHex("CF2DE378FBDD7E2EE87D486DFB5A7BFFFFCF2DE378FBDD7E2EE87D486DFB5A7BFFFFFF"),
          KeyType.SECP256K1),
      "entropy must have length 32."
    );
  }
}
