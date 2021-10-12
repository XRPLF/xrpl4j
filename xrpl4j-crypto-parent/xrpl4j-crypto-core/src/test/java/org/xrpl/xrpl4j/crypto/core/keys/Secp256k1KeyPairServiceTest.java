package org.xrpl.xrpl4j.crypto.core.keys;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.BaseEncoding;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.Base58;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

/**
 * Unit tests for {@link Secp256k1KeyPairService}.
 */
class Secp256k1KeyPairServiceTest {

  private Secp256k1KeyPairService keyPairService;

  @BeforeEach
  void setUp() {
    keyPairService = Secp256k1KeyPairService.getInstance();
  }

  @Test
  void generateSeed() {
    Seed seed = keyPairService.generateSeed();
    assertThat(seed).isNotNull();
  }

  @Test
  void generateSeedWithInput() {
    Seed seed = keyPairService.generateSeed(Entropy.of(new byte[16]));
    assertThat(seed.decodedSeed().bytes().toByteArray()).isEqualTo(new byte[16]);
  }

  @Test
  void generateSeedWithNull() {
    Assertions.assertThrows(NullPointerException.class, () -> keyPairService.generateSeed(null));
  }

  @Test
  void deriveKeyPairFromNull() {
    Assertions.assertThrows(NullPointerException.class, () -> keyPairService.deriveKeyPair(null));
  }

  @Test
  public void deriveKeyPair() {
    Seed seed = new Seed(UnsignedByteArray.of(Base58.decode("sp5fghtJtpUorTwvof1NpDXAzNwf5")));
    KeyPair keyPair = keyPairService.deriveKeyPair(seed);
    KeyPair expectedKeyPair = KeyPair.builder()
      .privateKey(PrivateKey.of(UnsignedByteArray.of(
        BaseEncoding.base16().decode("00D78B9735C3F26501C7337B8A5727FD53A6EFDBC6AA55984F098488561F985E23"
        ))))
      .publicKey(
        PublicKey.fromBase16EncodedPublicKey("030D58EB48B4420B1F7B9DF55087E0E29FEF0E8468F9A6825B01CA2C361042D435")
      )
      .build();
    assertThat(keyPair).isEqualTo(expectedKeyPair);
  }

}