package org.xrpl.xrpl4j.crypto.bc.keys;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.BaseEncoding;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.addresses.exceptions.DecodeException;
import org.xrpl.xrpl4j.crypto.core.keys.Base58EncodedSecret;
import org.xrpl.xrpl4j.crypto.core.keys.Entropy;
import org.xrpl.xrpl4j.crypto.core.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.core.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.core.keys.Seed;

/**
 * Unit tests for {@link Ed25519KeyPairService}.
 */
class Ed25519KeyPairServiceTest {

  private Ed25519KeyPairService keyPairService;

  @BeforeEach
  void setUp() {
    keyPairService = Ed25519KeyPairService.getInstance();
  }

  @Test
  void generateSeed() {
    Seed seed = keyPairService.generateSeed();
    assertThat(seed).isNotNull();
  }

  @Test
  void generateSeedWithInput() {
    Seed seed = keyPairService.generateSeed(Entropy.of(new byte[16]));
    assertThat(seed.decodedSeed().bytes().hexValue()).isEqualTo("00000000000000000000000000000000");
  }

  @Test
  void generateSeedWithNull() {
    Assertions.assertThrows(NullPointerException.class, () -> keyPairService.generateSeed(null));
  }

  @Test
  void deriveKeyPairFromNullSeed() {
    Assertions.assertThrows(NullPointerException.class, () -> keyPairService.deriveKeyPair(null));
  }

  @Test
  public void deriveKeyPair() {
    Seed seed = Seed.fromBase58EncodedSecret(Base58EncodedSecret.of("sEdSvUyszZFDFkkxQLm18ry3yeZ2FDM"));

    KeyPair keyPair = keyPairService.deriveKeyPair(seed);

    KeyPair expectedKeyPair = KeyPair.builder()
      .privateKey(PrivateKey.of(UnsignedByteArray.of(
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
  public void deriveKeyPairFromWrongSeedType() {
    Seed seed = Seed.fromBase58EncodedSecret(Base58EncodedSecret.of("sp5fghtJtpUorTwvof1NpDXAzNwf5"));
    Assertions.assertThrows(DecodeException.class, () -> keyPairService.deriveKeyPair(seed));
  }
}