package org.xrpl.xrpl4j.crypto.mpt.elgamal.java;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.mpt.BlindingFactor;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalCiphertext;
import org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalPrivateKey;
import org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalPublicKey;

/**
 * Unit tests for {@link JavaElGamalBalanceDecryptor}.
 */
public class JavaElGamalBalanceDecryptorTest extends AbstractElGamalTest {

  private JavaElGamalBalanceDecryptor decryptor;
  private JavaElGamalBalanceEncryptor encryptor;

  @BeforeEach
  void setUp() {
    this.decryptor = new JavaElGamalBalanceDecryptor();
    this.encryptor = new JavaElGamalBalanceEncryptor();
  }

  // ==================== Positive Tests ====================

  @Test
  void decryptWithFullRange() {
    ElGamalPublicKey publicKey = toElGamalPublicKey(keyPair.publicKey());
    ElGamalPrivateKey privateKey = ElGamalPrivateKey.of(keyPair.privateKey().naturalBytes());
    BlindingFactor blindingFactor = BlindingFactor.generate();
    UnsignedLong amount = UnsignedLong.valueOf(12345);

    ElGamalCiphertext ciphertext = encryptor.encrypt(amount, publicKey, blindingFactor);
    long decrypted = decryptor.decrypt(ciphertext, privateKey, 0, 1_000_000);

    assertThat(decrypted).isEqualTo(amount.longValue());
  }

  @Test
  void decryptWithCustomRange() {
    ElGamalPublicKey publicKey = toElGamalPublicKey(keyPair.publicKey());
    ElGamalPrivateKey privateKey = ElGamalPrivateKey.of(keyPair.privateKey().naturalBytes());
    BlindingFactor blindingFactor = BlindingFactor.generate();
    UnsignedLong amount = UnsignedLong.valueOf(500);

    ElGamalCiphertext ciphertext = encryptor.encrypt(amount, publicKey, blindingFactor);
    long decrypted = decryptor.decrypt(ciphertext, privateKey, 400, 600);

    assertThat(decrypted).isEqualTo(amount.longValue());
  }

  @Test
  void decryptZeroWithRange() {
    ElGamalPublicKey publicKey = toElGamalPublicKey(keyPair.publicKey());
    ElGamalPrivateKey privateKey = ElGamalPrivateKey.of(keyPair.privateKey().naturalBytes());
    BlindingFactor blindingFactor = BlindingFactor.generate();
    UnsignedLong amount = UnsignedLong.ZERO;

    ElGamalCiphertext ciphertext = encryptor.encrypt(amount, publicKey, blindingFactor);
    long decrypted = decryptor.decrypt(ciphertext, privateKey, 0, 100);

    assertThat(decrypted).isEqualTo(0);
  }

  @Test
  void decryptAtRangeBoundaries() {
    ElGamalPublicKey publicKey = toElGamalPublicKey(keyPair.publicKey());
    ElGamalPrivateKey privateKey = ElGamalPrivateKey.of(keyPair.privateKey().naturalBytes());
    BlindingFactor blindingFactor = BlindingFactor.generate();

    // Test at min boundary
    UnsignedLong minAmount = UnsignedLong.valueOf(100);
    ElGamalCiphertext ciphertext1 = encryptor.encrypt(minAmount, publicKey, blindingFactor);
    assertThat(decryptor.decrypt(ciphertext1, privateKey, 100, 200)).isEqualTo(100);

    // Test at max boundary
    BlindingFactor blindingFactor2 = BlindingFactor.generate();
    UnsignedLong maxAmount = UnsignedLong.valueOf(200);
    ElGamalCiphertext ciphertext2 = encryptor.encrypt(maxAmount, publicKey, blindingFactor2);
    assertThat(decryptor.decrypt(ciphertext2, privateKey, 100, 200)).isEqualTo(200);
  }

  @Test
  void decryptWithSameMinAndMax() {
    ElGamalPublicKey publicKey = toElGamalPublicKey(keyPair.publicKey());
    ElGamalPrivateKey privateKey = ElGamalPrivateKey.of(keyPair.privateKey().naturalBytes());
    BlindingFactor blindingFactor = BlindingFactor.generate();
    UnsignedLong amount = UnsignedLong.valueOf(42);

    ElGamalCiphertext ciphertext = encryptor.encrypt(amount, publicKey, blindingFactor);
    long decrypted = decryptor.decrypt(ciphertext, privateKey, 42, 42);

    assertThat(decrypted).isEqualTo(42);
  }

  // ==================== Negative Tests ====================

  @Test
  void decryptWithNullCiphertextThrows() {
    ElGamalPrivateKey privateKey = ElGamalPrivateKey.of(keyPair.privateKey().naturalBytes());

    assertThatThrownBy(() -> decryptor.decrypt(null, privateKey, 0, 100))
      .isInstanceOf(NullPointerException.class)
      .hasMessage("ciphertext must not be null");
  }

  @Test
  void decryptWithNullPrivateKeyThrows() {
    ElGamalPublicKey publicKey = toElGamalPublicKey(keyPair.publicKey());
    BlindingFactor blindingFactor = BlindingFactor.generate();
    ElGamalCiphertext ciphertext = encryptor.encrypt(UnsignedLong.ONE, publicKey, blindingFactor);

    assertThatThrownBy(() -> decryptor.decrypt(ciphertext, null, 0, 100))
      .isInstanceOf(NullPointerException.class)
      .hasMessage("privateKey must not be null");
  }

  @Test
  void decryptWithNegativeMinAmountThrows() {
    ElGamalPublicKey publicKey = toElGamalPublicKey(keyPair.publicKey());
    ElGamalPrivateKey privateKey = ElGamalPrivateKey.of(keyPair.privateKey().naturalBytes());
    BlindingFactor blindingFactor = BlindingFactor.generate();
    ElGamalCiphertext ciphertext = encryptor.encrypt(UnsignedLong.ONE, publicKey, blindingFactor);

    assertThatThrownBy(() -> decryptor.decrypt(ciphertext, privateKey, -1, 100))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("minAmount must be non-negative");
  }

  @Test
  void decryptWithMinGreaterThanMaxThrows() {
    ElGamalPublicKey publicKey = toElGamalPublicKey(keyPair.publicKey());
    ElGamalPrivateKey privateKey = ElGamalPrivateKey.of(keyPair.privateKey().naturalBytes());
    BlindingFactor blindingFactor = BlindingFactor.generate();
    ElGamalCiphertext ciphertext = encryptor.encrypt(UnsignedLong.ONE, publicKey, blindingFactor);

    assertThatThrownBy(() -> decryptor.decrypt(ciphertext, privateKey, 200, 100))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("minAmount (200) must be less than or equal to maxAmount (100)");
  }

  @Test
  void decryptAmountOutsideRangeThrows() {
    ElGamalPublicKey publicKey = toElGamalPublicKey(keyPair.publicKey());
    ElGamalPrivateKey privateKey = ElGamalPrivateKey.of(keyPair.privateKey().naturalBytes());
    BlindingFactor blindingFactor = BlindingFactor.generate();
    UnsignedLong amount = UnsignedLong.valueOf(500);

    ElGamalCiphertext ciphertext = encryptor.encrypt(amount, publicKey, blindingFactor);

    // Amount is 500, but we search in range [100, 200]
    assertThatThrownBy(() -> decryptor.decrypt(ciphertext, privateKey, 100, 200))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Amount not found within search range (100 to 200)");
  }

  @Test
  void decryptZeroWhenZeroNotInRangeThrows() {
    ElGamalPublicKey publicKey = toElGamalPublicKey(keyPair.publicKey());
    ElGamalPrivateKey privateKey = ElGamalPrivateKey.of(keyPair.privateKey().naturalBytes());
    BlindingFactor blindingFactor = BlindingFactor.generate();
    UnsignedLong amount = UnsignedLong.ZERO;

    ElGamalCiphertext ciphertext = encryptor.encrypt(amount, publicKey, blindingFactor);

    // Amount is 0, but we search in range [1, 100]
    assertThatThrownBy(() -> decryptor.decrypt(ciphertext, privateKey, 1, 100))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Amount not found within search range (1 to 100)");
  }

  @Test
  void decryptWithWrongPrivateKeyThrows() {
    ElGamalPublicKey publicKey = toElGamalPublicKey(keyPair.publicKey());
    BlindingFactor blindingFactor = BlindingFactor.generate();
    UnsignedLong amount = UnsignedLong.valueOf(100);

    ElGamalCiphertext ciphertext = encryptor.encrypt(amount, publicKey, blindingFactor);

    // Use a different key pair for decryption
    ElGamalPrivateKey wrongPrivateKey = ElGamalPrivateKey.of(randomKeyPair().privateKey().naturalBytes());

    assertThatThrownBy(() -> decryptor.decrypt(ciphertext, wrongPrivateKey, 0, 200))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Amount not found within search range");
  }
}

