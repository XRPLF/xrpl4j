package org.xrpl.xrpl4j.crypto.bc.keys;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.BaseEncoding;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.bc.BcAddressUtils;
import org.xrpl.xrpl4j.crypto.core.keys.Base58EncodedSecret;
import org.xrpl.xrpl4j.crypto.core.keys.Entropy;
import org.xrpl.xrpl4j.crypto.core.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.core.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.core.keys.Seed;

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
  void deriveKeyPairFromNullSeed() {
    Seed nullSeed = null;
    Assertions.assertThrows(NullPointerException.class, () -> keyPairService.deriveKeyPair(nullSeed));
  }

  @Test
  void deriveKeyPairFromNullPrivateKey() {
    PrivateKey nullPrivateKey = null;
    Assertions.assertThrows(NullPointerException.class, () -> keyPairService.deriveKeyPair(nullPrivateKey));
  }

  @Test
  public void deriveSecp256k1KeyPair() {
    Seed seed = Seed.fromBase58EncodedSecret(Base58EncodedSecret.of("sp5fghtJtpUorTwvof1NpDXAzNwf5"));
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
    assertThat(BcAddressUtils.getInstance().deriveAddress(keyPair.publicKey()).value())
      .isEqualTo("rU6K7V3Po4snVhBBaU29sesqs2qTQJWDw1");
  }
}