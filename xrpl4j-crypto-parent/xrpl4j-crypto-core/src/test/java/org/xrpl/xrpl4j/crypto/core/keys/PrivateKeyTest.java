package org.xrpl.xrpl4j.crypto.core.keys;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.BaseEncoding;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.Base58;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.addresses.VersionType;

/**
 * Unit tests for {@link PrivateKey}.
 */
public class PrivateKeyTest {

  private Ed25519KeyPairService ed25519KeyPairService;
  private Secp256k1KeyPairService secp256k1KeyPairService;

  @BeforeEach
  public void setUp() {
    this.ed25519KeyPairService = Ed25519KeyPairService.getInstance();
    this.secp256k1KeyPairService = Secp256k1KeyPairService.getInstance();
  }

  @Test
  public void fromBase58EncodedStringEd25519() {
    final String base58EncodedPrivateKey = "pDcQTi2uFBAzQ7cY2mYQtk9QuQBoLU6rJypEf8EYPQoouh";
    PrivateKey privateKey = PrivateKey.of(UnsignedByteArray.of(Base58.decode(base58EncodedPrivateKey)));
    assertThat(Base58.encode(privateKey.value().toByteArray())).isEqualTo(base58EncodedPrivateKey);
    assertThat(privateKey.versionType()).isEqualTo(VersionType.ED25519);
  }

  @Test
  public void fromBase58EncodedStringSecp256k1() {
    final String base58EncodedPrivateKey = "rEjDwJp2Pm3NrUtcf8v17jWopvqPJxyi5RTrDfhcJcWSi";
    PrivateKey privateKey = PrivateKey.of(UnsignedByteArray.of(Base58.decode(base58EncodedPrivateKey)));
    assertThat(Base58.encode(privateKey.value().toByteArray())).isEqualTo(base58EncodedPrivateKey);
    assertThat(privateKey.versionType()).isEqualTo(VersionType.SECP256K1);
  }

  @Test
  public void fromBase16EncodedStringEd25519() {
    final Seed seed = Seed.ed25519SeedFromPassphrase(Passphrase.of("hello"));
    final PrivateKey privateKey = ed25519KeyPairService.deriveKeyPair(seed).privateKey();

    final String expectedBase58EncodedPrivateKey = "pDcQTi2uFBAzQ7cY2mYQtk9QuQBoLU6rJypEf8EYPQoouh";
    final String expectedBase16EncodedPrivateKey = "EDB224AFDCCEC7AA4E245E35452585D4FBBE37519BCA3929578BFC5BBD4640E163";

    assertThat(Base58.encode(privateKey.value().toByteArray())).isEqualTo(expectedBase58EncodedPrivateKey);
    assertThat(BaseEncoding.base16().encode(privateKey.value().toByteArray()))
      .isEqualTo(expectedBase16EncodedPrivateKey);
  }

  @Test
  public void fromBase16EncodedStringSecp256k1() {
    final Seed seed = Seed.secp256k1SeedFromPassphrase(Passphrase.of("hello"));
    final PrivateKey privateKey = secp256k1KeyPairService.deriveKeyPair(seed).privateKey();

    final String expectedBase58EncodedPrivateKey = "rEjDwJp2Pm3NrUtcf8v17jWopvqPJxyi5RTrDfhcJcWSi";
    final String expectedBase16EncodedPrivateKey = "00DAD3C2B4BF921398932C889DE5335F89D90249355FC6FFB73F1256D2957F9F17";

    assertThat(Base58.encode(privateKey.value().toByteArray())).isEqualTo(expectedBase58EncodedPrivateKey);
    assertThat(BaseEncoding.base16().encode(privateKey.value().toByteArray()))
      .isEqualTo(expectedBase16EncodedPrivateKey);
  }

  @Test
  public void versionTypeSecp256k1() {
    final Seed seed = Seed.secp256k1SeedFromPassphrase(Passphrase.of("hello"));
    final PrivateKey privateKey = secp256k1KeyPairService.deriveKeyPair(seed).privateKey();
    assertThat(privateKey.versionType()).isEqualTo(VersionType.SECP256K1);
  }

  @Test
  public void versionTypeEd25519() {
    final Seed seed = Seed.ed25519SeedFromPassphrase(Passphrase.of("hello"));
    final PrivateKey privateKey = ed25519KeyPairService.deriveKeyPair(seed).privateKey();
    assertThat(privateKey.versionType()).isEqualTo(VersionType.ED25519);
  }

  @Test
  void destroy() {
    final Seed seed = Seed.ed25519SeedFromPassphrase(Passphrase.of("hello"));
    PrivateKey privateKey = ed25519KeyPairService.deriveKeyPair(seed).privateKey();
    assertThat(privateKey.isDestroyed()).isFalse();
    privateKey.destroy();
    assertThat(privateKey.isDestroyed()).isTrue();
    assertThat(privateKey.value().hexValue()).isEqualTo("");
    privateKey.destroy();
    assertThat(privateKey.isDestroyed()).isTrue();
    assertThat(privateKey.value().hexValue()).isEqualTo("");

    privateKey = secp256k1KeyPairService.deriveKeyPair(seed).privateKey();
    assertThat(privateKey.isDestroyed()).isFalse();
    privateKey.destroy();
    assertThat(privateKey.isDestroyed()).isTrue();
    assertThat(privateKey.value().hexValue()).isEqualTo("");
    privateKey.destroy();
    assertThat(privateKey.isDestroyed()).isTrue();
    assertThat(privateKey.value().hexValue()).isEqualTo("");
  }

  @Test
  void equals() {
    final Seed seed = Seed.ed25519SeedFromPassphrase(Passphrase.of("hello"));
    PrivateKey privateKey1 = ed25519KeyPairService.deriveKeyPair(seed).privateKey();
    PrivateKey privateKey2 = ed25519KeyPairService.deriveKeyPair(seed).privateKey();
    final Seed seed2 = Seed.ed25519SeedFromPassphrase(Passphrase.of("world"));
    PrivateKey privateKey3 = ed25519KeyPairService.deriveKeyPair(seed2).privateKey();

    assertThat(privateKey1).isEqualTo(privateKey1);
    assertThat(privateKey1).isEqualTo(privateKey2);
    assertThat(privateKey2).isEqualTo(privateKey1);
    assertThat(privateKey1).isNotEqualTo(privateKey3);
    assertThat(privateKey3).isNotEqualTo(privateKey1);
    assertThat(privateKey1).isNotEqualTo(new Object());
  }

  @Test
  void testHashcode() {
    final Seed seed = Seed.ed25519SeedFromPassphrase(Passphrase.of("hello"));
    PrivateKey privateKey1 = ed25519KeyPairService.deriveKeyPair(seed).privateKey();
    PrivateKey privateKey2 = ed25519KeyPairService.deriveKeyPair(seed).privateKey();
    final Seed seed2 = Seed.ed25519SeedFromPassphrase(Passphrase.of("world"));
    PrivateKey privateKey3 = ed25519KeyPairService.deriveKeyPair(seed2).privateKey();

    assertThat(privateKey1.hashCode()).isEqualTo(privateKey1.hashCode());
    assertThat(privateKey1.hashCode()).isEqualTo(privateKey2.hashCode());
    assertThat(privateKey2.hashCode()).isEqualTo(privateKey1.hashCode());
    assertThat(privateKey1.hashCode()).isNotEqualTo(privateKey3.hashCode());
    assertThat(privateKey3.hashCode()).isNotEqualTo(privateKey1.hashCode());
  }

  @Test
  void testToString() {
    final Seed seed = Seed.ed25519SeedFromPassphrase(Passphrase.of("hello"));
    PrivateKey privateKey1 = ed25519KeyPairService.deriveKeyPair(seed).privateKey();
    assertThat(privateKey1.toString()).isEqualTo(
      "PrivateKey{sha256=b0eb137e61d77a24cead03e13a50d892f3845c597dd754cfa1b6a7954dc3b54b, " +
        "value=[redacted], " +
        "destroyed=false}"
    );
  }

}