package org.xrpl.xrpl4j.crypto.core.keys;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.BaseEncoding;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

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
  void deriveKeyPairFromNull() {
    Assertions.assertThrows(NullPointerException.class, () -> keyPairService.deriveKeyPair(null));
  }

  @Test
  public void deriveKeyPair() {
    Seed seed
      = Seed.ed25519SeedFromEntropy(Entropy.of(BaseEncoding.base16().decode("074AEBCFD2F78FB9803E490DFFB7D1BB")));
    KeyPair expectedKeyPair = KeyPair.builder()
      .privateKey(PrivateKey.of(UnsignedByteArray.of(
        BaseEncoding.base16().decode("ED0B7F52B4463290DFE757C1D346509C4B4E2A428F1A84B9CC36B55FA6CF5CEC9F"
        ))))
      .publicKey(
        PublicKey.fromBase16EncodedPublicKey("EDCFF30C9352EE9D605ACC0305B9281517ED55A3B4C037F2EA6F2D2201E1C36CC5")
      )
      .build();

    KeyPair keyPair = keyPairService.deriveKeyPair(seed);
    assertThat(keyPair).isEqualTo(expectedKeyPair);
  }
}