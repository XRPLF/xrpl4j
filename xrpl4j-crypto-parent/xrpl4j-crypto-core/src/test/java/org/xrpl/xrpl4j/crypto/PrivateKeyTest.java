package org.xrpl.xrpl4j.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.VersionType;
import org.xrpl.xrpl4j.keypairs.DefaultKeyPairService;
import org.xrpl.xrpl4j.keypairs.KeyPairService;

/**
 * Unit tests for {@link PrivateKey}.
 */
public class PrivateKeyTest {

  private KeyPairService keyPairService;

  @BeforeEach
  public void setUp() {
    this.keyPairService = new DefaultKeyPairService();
  }

  @Test
  public void fromBase58EncodedStringEd25519() {
    final String base58EncodedPrivateKey = "pDcQTi2uFBAzQ7cY2mYQtk9QuQBoLU6rJypEf8EYPQoouh";
    assertThat(PrivateKey.fromBase58EncodedPrivateKey(base58EncodedPrivateKey).base58Encoded())
      .isEqualTo(base58EncodedPrivateKey);
  }

  @Test
  public void fromBase58EncodedStringSecp256k1() {
    final String base58EncodedPrivateKey = "rEjDwJp2Pm3NrUtcf8v17jWopvqPJxyi5RTrDfhcJcWSi";
    assertThat(PrivateKey.fromBase58EncodedPrivateKey(base58EncodedPrivateKey).base58Encoded())
      .isEqualTo(base58EncodedPrivateKey);
  }

  @Test
  public void fromBase16EncodedStringEd25519() {
    final Seed seed = Seed.ed25519SeedFromPassphrase("hello".getBytes());
    final String privateKeyString = keyPairService.deriveKeyPair(seed.value()).privateKey();
    final PrivateKey privateKey = PrivateKey.fromBase16EncodedPrivateKey(privateKeyString);

    final String expectedBase58EncodedPrivateKey = "pDcQTi2uFBAzQ7cY2mYQtk9QuQBoLU6rJypEf8EYPQoouh";
    final String expectedBase16EncodedPrivateKey = "EDB224AFDCCEC7AA4E245E35452585D4FBBE37519BCA3929578BFC5BBD4640E163";

    assertThat(privateKey.base58Encoded()).isEqualTo(expectedBase58EncodedPrivateKey);
    assertThat(privateKey.base16Encoded()).isEqualTo(expectedBase16EncodedPrivateKey);
  }

  @Test
  public void fromBase16EncodedStringSecp256k1() {
    final Seed seed = Seed.secp256k1SeedFromPassphrase("hello".getBytes());
    final String privateKeyString = keyPairService.deriveKeyPair(seed.value()).privateKey();
    final PrivateKey publicKey = PrivateKey.fromBase16EncodedPrivateKey(privateKeyString);

    final String expectedBase58EncodedPrivateKey = "rEjDwJp2Pm3NrUtcf8v17jWopvqPJxyi5RTrDfhcJcWSi";
    final String expectedBase16EncodedPrivateKey = "00DAD3C2B4BF921398932C889DE5335F89D90249355FC6FFB73F1256D2957F9F17";

    assertThat(publicKey.base58Encoded()).isEqualTo(expectedBase58EncodedPrivateKey);
    assertThat(publicKey.base16Encoded()).isEqualTo(expectedBase16EncodedPrivateKey);
  }

  @Test
  public void versionTypeSecp256k1() {
    final Seed seed = Seed.secp256k1SeedFromPassphrase("hello".getBytes());
    final String privateKeyString = keyPairService.deriveKeyPair(seed.value()).privateKey();
    final PrivateKey privateKey = PrivateKey.fromBase16EncodedPrivateKey(privateKeyString);
    assertThat(privateKey.versionType()).isEqualTo(VersionType.SECP256K1);
  }

  @Test
  public void versionTypeEd25519() {
    final Seed seed = Seed.ed25519SeedFromPassphrase("hello".getBytes());
    final String privateKeyString = keyPairService.deriveKeyPair(seed.value()).privateKey();
    final PrivateKey privateKey = PrivateKey.fromBase16EncodedPrivateKey(privateKeyString);
    assertThat(privateKey.versionType()).isEqualTo(VersionType.ED25519);
  }

}