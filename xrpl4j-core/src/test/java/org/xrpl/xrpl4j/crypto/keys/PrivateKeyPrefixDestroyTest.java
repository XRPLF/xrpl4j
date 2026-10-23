package org.xrpl.xrpl4j.crypto.keys;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.BaseEncoding;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.KeyType;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteTestUtils;
import org.xrpl.xrpl4j.crypto.TestConstants;

/**
 * Regression tests proving that destroying the result of {@link PrivateKey#prefixedBytes()} cannot mutate the shared
 * {@link PrivateKey#ED2559_PREFIX} / {@link PrivateKey#SECP256K1_PREFIX} constants.
 */
class PrivateKeyPrefixDestroyTest {

  /**
   * Safety net: if defensive copying ever regresses, don't let it poison every other test in this JVM fork.
   */
  @AfterEach
  void restorePrefixes() {
    UnsignedByteTestUtils.reset(PrivateKey.ED2559_PREFIX, 0xED);
    UnsignedByteTestUtils.reset(PrivateKey.SECP256K1_PREFIX, 0x00);
  }

  @Test
  void prefixedBytesDoesNotShareEd25519PrefixInstance() {
    UnsignedByteArray exported = TestConstants.getEdPrivateKey().prefixedBytes();
    assertThat(exported.get(0)).isNotSameAs(PrivateKey.ED2559_PREFIX);
    assertThat(exported.get(0)).isEqualTo(PrivateKey.ED2559_PREFIX);
  }

  @Test
  void prefixedBytesDoesNotShareSecp256k1PrefixInstance() {
    UnsignedByteArray exported = TestConstants.getEcPrivateKey().prefixedBytes();
    assertThat(exported.get(0)).isNotSameAs(PrivateKey.SECP256K1_PREFIX);
    assertThat(exported.get(0)).isEqualTo(PrivateKey.SECP256K1_PREFIX);
  }

  @Test
  void destroyingEd25519ExportLeavesSharedPrefixIntact() {
    UnsignedByteArray exported = TestConstants.getEdPrivateKey().prefixedBytes();
    exported.destroy();

    assertThat(PrivateKey.ED2559_PREFIX.asInt()).isEqualTo(0xED);
    assertThat(PrivateKey.ED2559_PREFIX.isDestroyed()).isFalse();

    // A fresh export still carries the correct prefix...
    assertThat(TestConstants.getEdPrivateKey().prefixedBytes().hexValue()).startsWith("ED");

    // ...and fromPrefixedBytes still classifies correctly.
    UnsignedByteArray realEdBytes = UnsignedByteArray.of(
      BaseEncoding.base16().decode(TestConstants.ED_PRIVATE_KEY_WITH_PREFIX_HEX));
    assertThat(PrivateKey.fromPrefixedBytes(realEdBytes).keyType()).isEqualTo(KeyType.ED25519);
    UnsignedByteArray realEcBytes = UnsignedByteArray.of(
      BaseEncoding.base16().decode(TestConstants.EC_PRIVATE_KEY_WITH_PREFIX_HEX));
    assertThat(PrivateKey.fromPrefixedBytes(realEcBytes).keyType()).isEqualTo(KeyType.SECP256K1);
  }

  @Test
  void destroyingSecp256k1ExportLeavesSharedPrefixIntact() {
    UnsignedByteArray exported = TestConstants.getEcPrivateKey().prefixedBytes();
    exported.destroy();

    assertThat(PrivateKey.SECP256K1_PREFIX.asInt()).isEqualTo(0x00);
    assertThat(PrivateKey.SECP256K1_PREFIX.isDestroyed()).isFalse();
  }
}
