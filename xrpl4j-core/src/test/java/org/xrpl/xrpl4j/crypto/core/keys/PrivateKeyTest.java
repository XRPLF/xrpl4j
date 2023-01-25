package org.xrpl.xrpl4j.crypto.core.keys;

import static org.assertj.core.api.Assertions.assertThat;
import static org.xrpl.xrpl4j.crypto.core.TestConstants.EC_PRIVATE_KEY;
import static org.xrpl.xrpl4j.crypto.core.TestConstants.ED_PRIVATE_KEY;

import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.Base58;
import org.xrpl.xrpl4j.codec.addresses.VersionType;
import org.xrpl.xrpl4j.crypto.core.TestConstants;

/**
 * Unit tests for {@link PrivateKey}.
 */
public class PrivateKeyTest {

  @Test
  public void valueEd25519() {
    assertThat(Base58.encode(ED_PRIVATE_KEY.value().toByteArray())).isEqualTo(TestConstants.ED_PRIVATE_KEY_B58);
  }

  @Test
  public void valueSecp256k1() {
    assertThat(Base58.encode(EC_PRIVATE_KEY.value().toByteArray())).isEqualTo(TestConstants.EC_PRIVATE_KEY_B58);
  }

  @Test
  public void versionTypeEd25519() {
    assertThat(ED_PRIVATE_KEY.versionType()).isEqualTo(VersionType.ED25519);
  }

  @Test
  public void versionTypeSecp256k1() {
    assertThat(EC_PRIVATE_KEY.versionType()).isEqualTo(VersionType.SECP256K1);
  }

  @Test
  void destroy() {
    PrivateKey privateKey = PrivateKey.of(ED_PRIVATE_KEY.value());
    assertThat(privateKey.isDestroyed()).isFalse();
    privateKey.destroy();
    assertThat(privateKey.isDestroyed()).isTrue();
    assertThat(privateKey.value().hexValue()).isEqualTo("");
    privateKey.destroy();
    assertThat(privateKey.isDestroyed()).isTrue();
    assertThat(privateKey.value().hexValue()).isEqualTo("");

    privateKey = PrivateKey.of(EC_PRIVATE_KEY.value());
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
    assertThat(ED_PRIVATE_KEY).isEqualTo(ED_PRIVATE_KEY);
    assertThat(ED_PRIVATE_KEY).isNotEqualTo(EC_PRIVATE_KEY);
    assertThat(EC_PRIVATE_KEY).isNotEqualTo(new Object());

    assertThat(EC_PRIVATE_KEY).isEqualTo(EC_PRIVATE_KEY);
    assertThat(EC_PRIVATE_KEY).isNotEqualTo(ED_PRIVATE_KEY);
    assertThat(EC_PRIVATE_KEY).isNotEqualTo(new Object());
  }

  @Test
  void testHashcode() {
    assertThat(ED_PRIVATE_KEY.hashCode()).isEqualTo(ED_PRIVATE_KEY.hashCode());
    assertThat(ED_PRIVATE_KEY.hashCode()).isNotEqualTo(EC_PRIVATE_KEY.hashCode());

    assertThat(EC_PRIVATE_KEY.hashCode()).isEqualTo(EC_PRIVATE_KEY.hashCode());
    assertThat(EC_PRIVATE_KEY.hashCode()).isNotEqualTo(ED_PRIVATE_KEY.hashCode());
  }

  @Test
  void testToString() {
    assertThat(ED_PRIVATE_KEY.toString()).isEqualTo(
      "PrivateKey{" +
        "value=[redacted], " +
        "destroyed=false" +
        "}"
    );

    assertThat(EC_PRIVATE_KEY.toString()).isEqualTo(
      "PrivateKey{" +
        "value=[redacted], " +
        "destroyed=false" +
        "}"
    );
  }

}