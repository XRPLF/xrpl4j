package org.xrpl.xrpl4j.crypto.keys;

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
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.Base58;
import org.xrpl.xrpl4j.codec.addresses.KeyType;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.addresses.exceptions.DecodeException;
import org.xrpl.xrpl4j.crypto.keys.Seed.DefaultSeed;

import java.math.BigInteger;
import javax.security.auth.DestroyFailedException;

/**
 * Unit tests for {@link Seed}.
 */
@SuppressWarnings("deprecation")
public class SeedTest {

  private final Seed edSeed = Seed.ed25519SeedFromPassphrase(Passphrase.of("hello"));
  private final Seed ecSeed = Seed.secp256k1SeedFromPassphrase(Passphrase.of("hello"));

  @Test
  void constructorWithNullSeed() {
    assertThrows(NullPointerException.class, () -> {
      DefaultSeed nullSeed = null;
      new DefaultSeed(nullSeed);
    });
  }

  @Test
  void constructorWithNullUnsignedByteArray() {
    assertThrows(NullPointerException.class, () -> new DefaultSeed((UnsignedByteArray) null));
  }

  @Test
  void constructorWithUnsignedByteArray() {
    Seed originalSeed = Seed.fromBase58EncodedSecret(Base58EncodedSecret.of("sEdSvUyszZFDFkkxQLm18ry3yeZ2FDM"));
    byte[] originalSeedBytes = Base58.decode("sEdSvUyszZFDFkkxQLm18ry3yeZ2FDM");
    assertThat(new DefaultSeed(UnsignedByteArray.of(originalSeedBytes))).isEqualTo(originalSeed);
  }

  @Test
  void constructorWithSeed() {
    final Seed originalSeed = Seed.fromBase58EncodedSecret(Base58EncodedSecret.of("sEdSvUyszZFDFkkxQLm18ry3yeZ2FDM"));
    final Seed copiedSeed = new DefaultSeed((DefaultSeed) originalSeed);
    assertThat(originalSeed).isEqualTo(copiedSeed);
    assertThat(copiedSeed).isEqualTo(originalSeed);
    assertThat(originalSeed.decodedSeed().bytes().hexValue()).isEqualTo(copiedSeed.decodedSeed().bytes().hexValue());
  }

  @Test
  void testRandomEd25519SeedGeneration() {
    Seed originalSeed = Seed.ed25519Seed();
    Seed copiedSeed = new DefaultSeed((DefaultSeed) originalSeed);
    assertThat(originalSeed.equals(copiedSeed)).isTrue();
    assertThat(originalSeed.decodedSeed().bytes().hexValue()).isEqualTo(copiedSeed.decodedSeed().bytes().hexValue());
  }

  @Test
  void testRandomSecp256k1SeedGeneration() {
    Seed originalSeed = Seed.secp256k1Seed();
    Seed copiedSeed = new DefaultSeed((DefaultSeed) originalSeed);
    assertThat(originalSeed.equals(copiedSeed)).isTrue();
    assertThat(originalSeed.decodedSeed().bytes().hexValue()).isEqualTo(copiedSeed.decodedSeed().bytes().hexValue());
  }

  @Test
  void testSecp256k1SeedFromNullEntropy() {
    assertThrows(NullPointerException.class, () -> {
      Seed.secp256k1SeedFromEntropy(null);
    });
  }

  @Test
  void testEd25519SeedFromEntropyNullEntropy() {
    assertThrows(NullPointerException.class, () -> {
      Seed.ed25519SeedFromEntropy(null);
    });
  }

  @Test
  void testEd25519SeedFromPassphraseWithNull() {
    assertThrows(NullPointerException.class, () -> {
      Seed.ed25519SeedFromPassphrase(null);
    });
  }

  @Test
  void testSecp256k1SeedFromPassphraseWithNull() {
    assertThrows(NullPointerException.class, () -> {
      Seed.secp256k1SeedFromPassphrase(null);
    });
  }

  @Test
  public void testEd25519SeedFromPassphrase() throws DestroyFailedException {
    //noinspection OptionalGetWithoutIsPresent
    assertThat(edSeed.decodedSeed().type().get()).isEqualTo(KeyType.ED25519);
    assertThat(BaseEncoding.base64().encode(edSeed.decodedSeed().bytes().toByteArray()))
      .isEqualTo("m3HSJL1i83hdltRq0+o9cw==");
    assertThat(edSeed.isDestroyed()).isFalse();
    edSeed.destroy();
    assertThat(edSeed.isDestroyed()).isTrue();
  }

  @Test
  public void testSecp256k1SeedFromPassphrase() throws DestroyFailedException {
    //noinspection OptionalGetWithoutIsPresent
    assertThat(ecSeed.decodedSeed().type().get()).isEqualTo(KeyType.SECP256K1);
    assertThat(BaseEncoding.base64().encode(ecSeed.decodedSeed().bytes().toByteArray()))
      .isEqualTo("m3HSJL1i83hdltRq0+o9cw==");
    assertThat(ecSeed.isDestroyed()).isFalse();
    ecSeed.destroy();
    assertThat(ecSeed.isDestroyed()).isTrue();
  }

  @Test
  void seedFromBase58EncodedSecretWithNull() {
    assertThrows(NullPointerException.class, () -> {
      Seed.fromBase58EncodedSecret(null);
    });
  }

  @Test
  void seedFromBase58EncodedSecretEd25519() {
    Seed seed = Seed.fromBase58EncodedSecret(Base58EncodedSecret.of("sEdSvUyszZFDFkkxQLm18ry3yeZ2FDM"));
    assertThat(seed.decodedSeed().bytes().hexValue()).isEqualTo("2C74FD17EDAFD80E8447B0D46741EE24");
  }

  @Test
  void seedFromBase58EncodedSecretSecp256k1() {
    Seed seed = Seed.fromBase58EncodedSecret(Base58EncodedSecret.of("sp5fghtJtpUorTwvof1NpDXAzNwf5"));
    assertThat(seed.decodedSeed().bytes().hexValue()).isEqualTo("0102030405060708090A0B0C0D0E0F10");
  }

  /**
   * Verify {@link Seed#deriveKeyPair()} using a seed that would have produced a 32 byte private key prior to fixing
   * #486.
   *
   * @see "https://github.com/XRPLF/xrpl4j/issues/486"
   */
  @Test
  void deriveKeyPairFor32ByteEcSeed() {
    // Without explicit padding added by xrpl4j, but instead due to twos-complement padding, this seed would have
    // (prior to fixing #486) produced a 32 byte private key. We validate here that it produces the correct output
    // for either value() call.
    final Seed ecSeedFor32BytePrivateKey = Seed.secp256k1SeedFromEntropy(
      Entropy.of(BaseEncoding.base16().decode("B7E99C6BB9786494238D2BA84EABE854"))
    );
    final PrivateKey privateKey = ecSeedFor32BytePrivateKey.deriveKeyPair().privateKey();
    assertThat(privateKey.value().hexValue()) // <- Overtly test .value()
      .isEqualTo("007030CBD40D6961E625AD73159A4B463AA42B4E88CC2248AC49E1EDCB50AF2924");
    assertThat(privateKey.valueWithPrefixedBytes().hexValue())
      .isEqualTo("007030CBD40D6961E625AD73159A4B463AA42B4E88CC2248AC49E1EDCB50AF2924");
    assertThat(privateKey.valueWithNaturalBytes().hexValue())
      .isEqualTo("7030CBD40D6961E625AD73159A4B463AA42B4E88CC2248AC49E1EDCB50AF2924");
  }

  /**
   * Verify {@link Seed#deriveKeyPair()} using a seed that would have produced a 33 byte private key prior to fixing
   * #486.
   *
   * @see "https://github.com/XRPLF/xrpl4j/issues/486"
   */
  @Test
  void deriveKeyPairFor33ByteEcSeed() {
    // Without explicit padding added by xrpl4j, but instead due to twos-complement padding, this seed would have
    // (prior to fixing #486) produced a 33 byte private key. We validate here that it produces the correct output
    // for either value() call.
    final Seed ecSeedFor32BytePrivateKey = Seed.secp256k1SeedFromEntropy(
      Entropy.of(BaseEncoding.base16().decode("358C75D5AD5E2FF5E19256978F18F0B9"))
    );
    final PrivateKey privateKey = ecSeedFor32BytePrivateKey.deriveKeyPair().privateKey();
    assertThat(privateKey.value().hexValue()) // <- Overtly test .value()
      .isEqualTo("00FBD60C9C99BA3D4706A449C30E4D61DCC3811E23EF69291F98A886CEC6A8B0B5");
    assertThat(privateKey.valueWithPrefixedBytes().hexValue())
      .isEqualTo("00FBD60C9C99BA3D4706A449C30E4D61DCC3811E23EF69291F98A886CEC6A8B0B5");
    assertThat(privateKey.valueWithNaturalBytes().hexValue())
      .isEqualTo("FBD60C9C99BA3D4706A449C30E4D61DCC3811E23EF69291F98A886CEC6A8B0B5");
  }

  /**
   * Verify that BigInteger construction behaves the same no matter how many zero-byte prefix pads are used.
   */
  @Test
  void verifyBigIntegerConstructionForSecp256k1PrivateKeys() {
    // This seed will generate a secp256k1 SecretKey D value of
    // 53AC3F62A5A6E598C7D1E31AB92587C56823A1BE5C21E53ABE9D9A722E5236 (which is only 31 bytes long).
    Seed ecSeedFor31BytePrivateKey = Seed.fromBase58EncodedSecret(
      Base58EncodedSecret.of("shZrqKhyGi5Gyvrv5AJawNMuR2WaN")
    );

    assertThat(BaseEncoding.base16().decode("53AC3F62A5A6E598C7D1E31AB92587C56823A1BE5C21E53ABE9D9A722E5236").length)
      .isEqualTo(31);
    assertThat(ecSeedFor31BytePrivateKey.deriveKeyPair().privateKey().valueWithNaturalBytes().toByteArray().length)
      .isEqualTo(32);
    assertThat(ecSeedFor31BytePrivateKey.deriveKeyPair().privateKey().valueWithPrefixedBytes().toByteArray().length)
      .isEqualTo(33);

    // Assert that all BigIntegers are equivalent (i.e., that the constructor ignores zero-byte pads)
    BigInteger shortBigInt = new BigInteger(
      BaseEncoding.base16().decode("53AC3F62A5A6E598C7D1E31AB92587C56823A1BE5C21E53ABE9D9A722E5236")
    );
    BigInteger unPaddedBitInt = new BigInteger(
      ecSeedFor31BytePrivateKey.deriveKeyPair().privateKey().valueWithNaturalBytes().toByteArray()
    );
    BigInteger paddedBitInt = new BigInteger(
      ecSeedFor31BytePrivateKey.deriveKeyPair().privateKey().valueWithPrefixedBytes().toByteArray()
    );

    assertThat(shortBigInt).isEqualTo(unPaddedBitInt);
    assertThat(shortBigInt).isEqualTo(paddedBitInt);
    assertThat(paddedBitInt).isEqualTo(unPaddedBitInt);
  }

  /**
   * Verify {@link Seed#deriveKeyPair()} using a seed that would have produced a 31 byte private key (prior to fixing
   * #486) generates expected results.
   *
   * @see "https://github.com/XRPLF/xrpl4j/issues/486"
   */
  @Test
  void deriveKeyPairFor31ByteEcSeed() {
    Seed ecSeedFor31BytePrivateKey = Seed.fromBase58EncodedSecret(
      Base58EncodedSecret.of("shZrqKhyGi5Gyvrv5AJawNMuR2WaN")
    );
    final PrivateKey privateKey = ecSeedFor31BytePrivateKey.deriveKeyPair().privateKey();
    assertThat(privateKey.value().hexValue()) // <- Overtly test .value()
      .isEqualTo("000053AC3F62A5A6E598C7D1E31AB92587C56823A1BE5C21E53ABE9D9A722E5236");
    assertThat(privateKey.valueWithPrefixedBytes().hexValue())
      .isEqualTo("000053AC3F62A5A6E598C7D1E31AB92587C56823A1BE5C21E53ABE9D9A722E5236");
    assertThat(privateKey.valueWithNaturalBytes().hexValue())
      .isEqualTo("0053AC3F62A5A6E598C7D1E31AB92587C56823A1BE5C21E53ABE9D9A722E5236");
  }

  @Test
  void testEquals() {
    assertThat(edSeed.equals(edSeed)).isTrue();
    assertThat(ecSeed.equals(ecSeed)).isTrue();
    assertThat(edSeed).isNotEqualTo(ecSeed);
    assertThat(ecSeed).isNotEqualTo(edSeed);
    assertThat(ecSeed).isNotEqualTo(new Object());
  }

  @Test
  void testHashCode() {
    assertThat(edSeed.hashCode()).isEqualTo(edSeed.hashCode());
    assertThat(ecSeed.hashCode()).isEqualTo(ecSeed.hashCode());
  }

  @Test
  void testToString() {
    assertThat(edSeed.toString()).isEqualTo("Seed{value=[redacted], destroyed=false}");
  }

  ///////////////////
  // Tests for Ed25519KeyService
  ///////////////////

  @Test
  void deriveEd25519KeyPairFromNullSeed() {
    assertThrows(NullPointerException.class,
      () -> Seed.DefaultSeed.Ed25519KeyPairService.deriveKeyPair(null));
  }

  @Test
  public void deriveEd25519KeyPair() {
    Seed seed = Seed.fromBase58EncodedSecret(Base58EncodedSecret.of("sEdSvUyszZFDFkkxQLm18ry3yeZ2FDM"));

    KeyPair keyPair = Seed.DefaultSeed.Ed25519KeyPairService.deriveKeyPair(seed);

    KeyPair expectedKeyPair = KeyPair.builder()
      .privateKey(PrivateKey.fromPrefixedBytes(UnsignedByteArray.of(
        BaseEncoding.base16().decode("ED2F1185B6F5525D7A7D2A22C1D8BAEEBEEFFE597C9010AF916EBB9447BECC5BE6"
        ))))
      .publicKey(
        PublicKey.fromBase16EncodedPublicKey("EDFC76D20CCC92FB18CC280C27EECEFB652749C7B090BA12CF30D4F35BE0009191")
      )
      .build();
    assertThat(keyPair).isEqualTo(expectedKeyPair);
    assertThat(keyPair.publicKey().deriveAddress().value()).isEqualTo("rpsAiz1JjunVeGk5QipvZt8QxY3hRcmKRR");
  }

  @Test
  public void deriveEd25519KeyPairFromWrongSeedType() {
    Seed seed = Seed.fromBase58EncodedSecret(Base58EncodedSecret.of("sp5fghtJtpUorTwvof1NpDXAzNwf5"));
    assertThrows(DecodeException.class, () -> Seed.DefaultSeed.Ed25519KeyPairService.deriveKeyPair(seed));
  }

  ///////////////////
  // Tests for Secp256k1KeyService
  ///////////////////

  @Test
  void deriveSecp256k1KeyPairFromNullSeed() {
    assertThrows(NullPointerException.class,
      () -> Seed.DefaultSeed.Secp256k1KeyPairService.deriveKeyPair(null));
  }

  @Test
  public void deriveSecp256k1KeyPair() {
    Seed seed = Seed.fromBase58EncodedSecret(Base58EncodedSecret.of("sp5fghtJtpUorTwvof1NpDXAzNwf5"));
    KeyPair keyPair = Seed.DefaultSeed.Secp256k1KeyPairService.deriveKeyPair(seed);
    KeyPair expectedKeyPair = KeyPair.builder()
      .privateKey(PrivateKey.fromPrefixedBytes(UnsignedByteArray.of(
        BaseEncoding.base16().decode("00D78B9735C3F26501C7337B8A5727FD53A6EFDBC6AA55984F098488561F985E23"
        ))))
      .publicKey(
        PublicKey.fromBase16EncodedPublicKey("030D58EB48B4420B1F7B9DF55087E0E29FEF0E8468F9A6825B01CA2C361042D435")
      )
      .build();
    assertThat(keyPair).isEqualTo(expectedKeyPair);
    assertThat(keyPair.publicKey().deriveAddress().value()).isEqualTo("rU6K7V3Po4snVhBBaU29sesqs2qTQJWDw1");
  }

  @Test
  public void generateSeedFromEd25519Seed() {
    Entropy entropy = Entropy.of(BaseEncoding.base16().decode("0102030405060708090A0B0C0D0E0F10"));
    Seed seed = Seed.ed25519SeedFromEntropy(entropy);
    assertThat(seed.deriveKeyPair().publicKey()).isEqualTo(
      PublicKey.fromBase16EncodedPublicKey("ED01FA53FA5A7E77798F882ECE20B1ABC00BB358A9E55A202D0D0676BD0CE37A63"));
    assertThat(seed.deriveKeyPair().privateKey()).isEqualTo(PrivateKey.fromPrefixedBytes(
      UnsignedByteArray.fromHex("EDB4C4E046826BD26190D09715FC31F4E6A728204EADD112905B08B14B7F15C4F3")
    ));
    assertThat(seed.deriveKeyPair().publicKey().deriveAddress().value()).isEqualTo(
      "rLUEXYuLiQptky37CqLcm9USQpPiz5rkpD");
  }

  @Test
  public void generateWalletFromSecp256k1Seed() {
    Entropy entropy = Entropy.of(BaseEncoding.base16().decode("CC4E55BC556DD561CBE990E3D4EF7069"));
    Seed seed = Seed.secp256k1SeedFromEntropy(entropy);
    assertThat(seed.deriveKeyPair().publicKey().base16Value()).isEqualTo(
      "02FD0E8479CE8182ABD35157BB0FA17A469AF27DCB12B5DDED697C61809116A33B"
    );
    assertThat(seed.deriveKeyPair().privateKey().valueWithPrefixedBytes().hexValue()).isEqualTo(
      "0027690792130FC12883E83AE85946B018B3BEDE6EEDCDA3452787A94FC0A17438"
    );
    assertThat(seed.deriveKeyPair().publicKey().deriveAddress().value()).isEqualTo(
      "rByLcEZ7iwTBAK8FfjtpFuT7fCzt4kF4r2"
    );
  }
}
