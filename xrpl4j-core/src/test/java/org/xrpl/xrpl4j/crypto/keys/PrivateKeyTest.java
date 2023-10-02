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
import static org.xrpl.xrpl4j.crypto.TestConstants.EC_PRIVATE_KEY_HEX;
import static org.xrpl.xrpl4j.crypto.TestConstants.EC_PRIVATE_KEY_WITH_PREFIX_HEX;
import static org.xrpl.xrpl4j.crypto.TestConstants.ED_PRIVATE_KEY_HEX;
import static org.xrpl.xrpl4j.crypto.TestConstants.ED_PRIVATE_KEY_WITH_PREFIX_HEX;

import com.google.common.io.BaseEncoding;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.KeyType;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.TestConstants;

/**
 * Unit tests for {@link PrivateKey}.
 */
@SuppressWarnings("deprecation")
class PrivateKeyTest {

  ////////////////////
  // of
  ////////////////////

  @Test
  void testOfWithNull() {
    assertThrows(NullPointerException.class, () -> PrivateKey.of(null));
  }

  @Test
  void testEcOf() {
    UnsignedByteArray thirtyThreeBytes = UnsignedByteArray.of(
      BaseEncoding.base16().decode(EC_PRIVATE_KEY_WITH_PREFIX_HEX));
    assertThat(PrivateKey.of(thirtyThreeBytes)).isEqualTo(TestConstants.getEcPrivateKey());

    UnsignedByteArray thirtyTwoBytes = UnsignedByteArray.of(thirtyThreeBytes.slice(1, 33).toByteArray());
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class, () -> PrivateKey.of(thirtyTwoBytes)
    );
    assertThat(exception.getMessage()).isEqualTo(
      "The `fromPrefixedBytes` function requires input length of 33 bytes, but 32 were supplied."
    );
  }

  @Test
  void testEdOf() {
    UnsignedByteArray thirtyThreeBytes = UnsignedByteArray.of(
      BaseEncoding.base16().decode(ED_PRIVATE_KEY_WITH_PREFIX_HEX));
    assertThat(PrivateKey.of(thirtyThreeBytes)).isEqualTo(TestConstants.getEdPrivateKey());

    UnsignedByteArray thirtyTwoBytes = UnsignedByteArray.of(thirtyThreeBytes.slice(1, 33).toByteArray());
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class, () -> PrivateKey.of(thirtyTwoBytes)
    );
    assertThat(exception.getMessage()).isEqualTo(
      "The `fromPrefixedBytes` function requires input length of 33 bytes, but 32 were supplied."
    );
  }

  @Test
  void testOfWithLessWithEmpty() {
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class, () -> PrivateKey.of(UnsignedByteArray.empty())
    );
    assertThat(exception.getMessage())
      .isEqualTo("The `fromPrefixedBytes` function requires input length of 33 bytes, but 0 were supplied.");
  }

  @Test
  void testEdOfWithLessThan32Bytes() {
    UnsignedByteArray twoBytes = UnsignedByteArray.of(new byte[] {(byte) 0xED, (byte) 0xFF});
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class, () -> PrivateKey.of(twoBytes)
    );
    assertThat(exception.getMessage()).isEqualTo(
      "The `fromPrefixedBytes` function requires input length of 33 bytes, but 2 were supplied."
    );
  }

  @Test
  void testEcOfWithLessThan32Bytes() {
    UnsignedByteArray twoBytes = UnsignedByteArray.of(new byte[] {(byte) 0x00, (byte) 0xFF});
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class, () -> PrivateKey.of(twoBytes)
    );
    assertThat(exception.getMessage()).isEqualTo(
      "The `fromPrefixedBytes` function requires input length of 33 bytes, but 2 were supplied."
    );
  }

  ///////////////////
  // fromNaturalBytes
  ///////////////////

  @Test
  void testFromNaturalBytesWithNull() {
    assertThrows(NullPointerException.class, () -> PrivateKey.fromNaturalBytes(null, KeyType.ED25519));
    assertThrows(NullPointerException.class, () -> PrivateKey.fromNaturalBytes(UnsignedByteArray.empty(), null));
  }

  @Test
  void testEcFromNaturalBytesWithEmpty() {
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class, () -> PrivateKey.fromNaturalBytes(UnsignedByteArray.empty(), KeyType.SECP256K1)
    );
    assertThat(exception.getMessage())
      .isEqualTo("Byte values passed to this constructor must be 32 bytes long, with no prefix.");
  }

  @Test
  void testEdFromNaturalBytesWithEmpty() {
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class, () -> PrivateKey.fromNaturalBytes(UnsignedByteArray.empty(), KeyType.ED25519)
    );
    assertThat(exception.getMessage())
      .isEqualTo("Byte values passed to this constructor must be 32 bytes long, with no prefix.");
  }

  @Test
  void testEcFromNaturalBytes() {
    UnsignedByteArray thirtyThreeBytes = UnsignedByteArray.of(
      BaseEncoding.base16().decode(EC_PRIVATE_KEY_WITH_PREFIX_HEX)
    );
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class, () -> PrivateKey.fromNaturalBytes(thirtyThreeBytes, KeyType.SECP256K1)
    );
    assertThat(exception.getMessage())
      .isEqualTo("Byte values passed to this constructor must be 32 bytes long, with no prefix.");

    UnsignedByteArray thirtyTwoBytes = UnsignedByteArray.of(thirtyThreeBytes.slice(1, 33).toByteArray());
    assertThat(PrivateKey.fromNaturalBytes(thirtyTwoBytes, KeyType.SECP256K1)).isEqualTo(
      TestConstants.getEcPrivateKey()
    );
  }

  @Test
  void testEdFromNaturalBytes() {
    UnsignedByteArray thirtyThreeBytes = UnsignedByteArray.of(
      BaseEncoding.base16().decode(ED_PRIVATE_KEY_WITH_PREFIX_HEX));
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class, () -> PrivateKey.fromNaturalBytes(thirtyThreeBytes, KeyType.ED25519)
    );
    assertThat(exception.getMessage())
      .isEqualTo("Byte values passed to this constructor must be 32 bytes long, with no prefix.");

    UnsignedByteArray thirtyTwoBytes = UnsignedByteArray.of(thirtyThreeBytes.slice(1, 33).toByteArray());
    assertThat(PrivateKey.fromNaturalBytes(thirtyTwoBytes, KeyType.ED25519)).isEqualTo(TestConstants.getEdPrivateKey());
  }

  @Test
  void testEdFromNaturalBytesWithLessThan32Bytes() {
    UnsignedByteArray twoBytes = UnsignedByteArray.of(new byte[] {(byte) 0xED, (byte) 0xFF});
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class, () -> PrivateKey.fromNaturalBytes(twoBytes, KeyType.ED25519)
    );
    assertThat(exception.getMessage())
      .isEqualTo("Byte values passed to this constructor must be 32 bytes long, with no prefix.");
  }

  @Test
  void testEcFromNaturalBytesWithLessThan32Bytes() {
    UnsignedByteArray twoBytes = UnsignedByteArray.of(new byte[] {(byte) 0x00, (byte) 0xFF});
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class, () -> PrivateKey.fromNaturalBytes(twoBytes, KeyType.SECP256K1)
    );
    assertThat(exception.getMessage())
      .isEqualTo("Byte values passed to this constructor must be 32 bytes long, with no prefix.");
  }

  ////////////////////
  // fromPrefixedBytes
  ////////////////////

  @Test
  void testFromPrefixedBytesWithNull() {
    assertThrows(NullPointerException.class, () -> PrivateKey.fromPrefixedBytes(null));
  }

  @Test
  void testEcFromPrefixedBytes() {
    UnsignedByteArray thirtyThreeBytes = UnsignedByteArray.of(
      BaseEncoding.base16().decode(EC_PRIVATE_KEY_WITH_PREFIX_HEX)
    );
    assertThat(PrivateKey.fromPrefixedBytes(thirtyThreeBytes)).isEqualTo(TestConstants.getEcPrivateKey());

    UnsignedByteArray thirtyTwoBytes = UnsignedByteArray.of(thirtyThreeBytes.slice(1, 33).toByteArray());
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class, () -> PrivateKey.fromPrefixedBytes(thirtyTwoBytes)
    );
    assertThat(exception.getMessage()).isEqualTo(
      "The `fromPrefixedBytes` function requires input length of 33 bytes, but 32 were supplied."
    );
  }

  @Test
  void testEdFromPrefixedBytes() {
    UnsignedByteArray thirtyThreeBytes = UnsignedByteArray.of(
      BaseEncoding.base16().decode(ED_PRIVATE_KEY_WITH_PREFIX_HEX)
    );
    assertThat(PrivateKey.fromPrefixedBytes(thirtyThreeBytes)).isEqualTo(TestConstants.getEdPrivateKey());

    UnsignedByteArray thirtyTwoBytes = UnsignedByteArray.of(thirtyThreeBytes.slice(1, 33).toByteArray());
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class, () -> PrivateKey.fromPrefixedBytes(thirtyTwoBytes)
    );
    assertThat(exception.getMessage()).isEqualTo(
      "The `fromPrefixedBytes` function requires input length of 33 bytes, but 32 were supplied."
    );
  }

  @Test
  void testEdFromPrefixedBytesWithLessThan32Bytes() {
    UnsignedByteArray twoBytes = UnsignedByteArray.of(new byte[] {(byte) 0xED, (byte) 0xFF});
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class, () -> PrivateKey.fromPrefixedBytes(twoBytes)
    );
    assertThat(exception.getMessage()).isEqualTo(
      "The `fromPrefixedBytes` function requires input length of 33 bytes, but 2 were supplied."
    );
  }

  @Test
  void testEcFromPrefixedBytesWithLessThan32Bytes() {
    UnsignedByteArray twoBytes = UnsignedByteArray.of(new byte[] {(byte) 0x00, (byte) 0xFF});
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class, () -> PrivateKey.fromPrefixedBytes(twoBytes)
    );
    assertThat(exception.getMessage()).isEqualTo(
      "The `fromPrefixedBytes` function requires input length of 33 bytes, but 2 were supplied."
    );
  }

  @Test
  void testEdFromPrefixedBytesWith0Bytes() {
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class, () -> PrivateKey.fromPrefixedBytes(UnsignedByteArray.empty())
    );
    assertThat(exception.getMessage()).isEqualTo(
      "The `fromPrefixedBytes` function requires input length of 33 bytes, but 0 were supplied."
    );
  }

  @Test
  void testEcFromPrefixedBytesWithLess0Bytes() {
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class, () -> PrivateKey.fromPrefixedBytes(UnsignedByteArray.empty())
    );
    assertThat(exception.getMessage()).isEqualTo(
      "The `fromPrefixedBytes` function requires input length of 33 bytes, but 0 were supplied."
    );
  }

  @Test
  void testFromPrefixedBytesWithInvalidPrefix() {
    final byte[] invalidPrefixBytes = BaseEncoding.base16().decode(
      "20000000000000000000000000000000" + // <-- 16 bytes
        "0000000000000000000000000000000000"); // <-- 17 bytes
    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class, () -> PrivateKey.fromPrefixedBytes(UnsignedByteArray.of(invalidPrefixBytes))
    );
    assertThat(exception.getMessage()).isEqualTo(
      "Constructing a PrivateKey with raw bytes requires a one-byte prefix in front of the 32 natural bytes of a " +
        "private key. Use the prefix `0xED` for ed25519 private keys, or `0x00` for secp256k1 private keys. " +
        "Length was 33 bytes."
    );
  }

  ///////////////////
  // Constants
  ///////////////////

  @Test
  void testConstants() {
    assertThat(PrivateKey.ED2559_PREFIX.asInt()).isEqualTo(0xED);
    assertThat(PrivateKey.ED2559_PREFIX.asByte()).isEqualTo((byte) 0xED);

    assertThat(PrivateKey.SECP256K1_PREFIX.asInt()).isEqualTo(0x00);
    assertThat(PrivateKey.SECP256K1_PREFIX.asByte()).isEqualTo((byte) 0x00);
  }

  ///////////////////
  // value tests ==> [value, valueWithPrefixedBytes, valueWithNaturalBytes]
  ///////////////////

  @Test
  void valueForEd25519() {
    assertThat(TestConstants.getEdPrivateKey().value().hexValue()).isEqualTo(ED_PRIVATE_KEY_WITH_PREFIX_HEX);
    assertThat(TestConstants.getEdPrivateKey().valueWithPrefixedBytes().hexValue()).isEqualTo(
      ED_PRIVATE_KEY_WITH_PREFIX_HEX);
    assertThat(TestConstants.getEdPrivateKey().valueWithNaturalBytes().hexValue()).isEqualTo(ED_PRIVATE_KEY_HEX);
  }

  @Test
  void valueForSecp256k1() {
    assertThat(TestConstants.getEcPrivateKey().value().hexValue()).isEqualTo(EC_PRIVATE_KEY_WITH_PREFIX_HEX);
    assertThat(TestConstants.getEcPrivateKey().valueWithPrefixedBytes().hexValue()).isEqualTo(
      EC_PRIVATE_KEY_WITH_PREFIX_HEX);
    assertThat(TestConstants.getEcPrivateKey().valueWithNaturalBytes().hexValue()).isEqualTo(EC_PRIVATE_KEY_HEX);
  }

  ///////////////////
  // Misc
  ///////////////////

  @Test
  void keyTypeEd25519() {
    assertThat(TestConstants.getEdPrivateKey().keyType()).isEqualTo(KeyType.ED25519);
  }

  @Test
  void keyTypeSecp256k1() {
    assertThat(TestConstants.getEcPrivateKey().keyType()).isEqualTo(KeyType.SECP256K1);
  }

  @Test
  void destroy() {
    PrivateKey privateKey = PrivateKey.of(TestConstants.getEdPrivateKey().value());
    assertThat(privateKey.isDestroyed()).isFalse();
    privateKey.destroy();
    assertThat(privateKey.isDestroyed()).isTrue();
    assertThat(privateKey.value().hexValue()).isEqualTo("");
    privateKey.destroy();
    assertThat(privateKey.isDestroyed()).isTrue();
    assertThat(privateKey.value().hexValue()).isEqualTo("");
    assertThat(privateKey.valueWithNaturalBytes()).isEqualTo(UnsignedByteArray.empty());
    assertThat(privateKey.valueWithPrefixedBytes()).isEqualTo(UnsignedByteArray.empty());
    assertThat(privateKey.value()).isEqualTo(UnsignedByteArray.empty());

    privateKey = PrivateKey.of(TestConstants.getEcPrivateKey().value());
    assertThat(privateKey.isDestroyed()).isFalse();
    privateKey.destroy();
    assertThat(privateKey.isDestroyed()).isTrue();
    assertThat(privateKey.value().hexValue()).isEqualTo("");
    privateKey.destroy();
    assertThat(privateKey.isDestroyed()).isTrue();
    assertThat(privateKey.value().hexValue()).isEqualTo("");
    assertThat(privateKey.valueWithNaturalBytes()).isEqualTo(UnsignedByteArray.empty());
    assertThat(privateKey.valueWithPrefixedBytes()).isEqualTo(UnsignedByteArray.empty());
    assertThat(privateKey.value()).isEqualTo(UnsignedByteArray.empty());
  }

  @Test
  void equals() {
    PrivateKey privateKey = TestConstants.getEdPrivateKey();
    assertThat(privateKey).isEqualTo(privateKey); // <-- To cover reference equality in .equals

    assertThat(TestConstants.getEdPrivateKey()).isEqualTo(TestConstants.getEdPrivateKey());
    assertThat(TestConstants.getEdPrivateKey()).isEqualTo(PrivateKey.of(TestConstants.getEdPrivateKey().value()));
    assertThat(TestConstants.getEdPrivateKey()).isNotEqualTo(TestConstants.getEcPrivateKey());
    assertThat(TestConstants.getEcPrivateKey()).isNotEqualTo(new Object());

    assertThat(TestConstants.getEcPrivateKey()).isEqualTo(TestConstants.getEcPrivateKey());
    assertThat(TestConstants.getEcPrivateKey()).isEqualTo(PrivateKey.of(TestConstants.getEcPrivateKey().value()));
    assertThat(TestConstants.getEcPrivateKey()).isNotEqualTo(TestConstants.getEdPrivateKey());
    assertThat(TestConstants.getEcPrivateKey()).isNotEqualTo(new Object());
  }

  @Test
  void testHashcode() {
    assertThat(TestConstants.getEdPrivateKey().hashCode()).isEqualTo(TestConstants.getEdPrivateKey().hashCode());
    assertThat(TestConstants.getEdPrivateKey().hashCode()).isNotEqualTo(TestConstants.getEcPrivateKey().hashCode());

    assertThat(TestConstants.getEcPrivateKey().hashCode()).isEqualTo(TestConstants.getEcPrivateKey().hashCode());
    assertThat(TestConstants.getEcPrivateKey().hashCode()).isNotEqualTo(TestConstants.getEdPrivateKey().hashCode());
  }

  @Test
  void testToString() {
    assertThat(TestConstants.getEdPrivateKey().toString()).isEqualTo(
      "PrivateKey{" +
        "value=[redacted]," +
        "keyType=ED25519," +
        "destroyed=false" +
        "}"
    );

    assertThat(TestConstants.getEcPrivateKey().toString()).isEqualTo(
      "PrivateKey{" +
        "value=[redacted]," +
        "keyType=SECP256K1," +
        "destroyed=false" +
        "}"
    );
  }

}
