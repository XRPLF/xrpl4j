package org.xrpl.xrpl4j.crypto.mpt.wrapper.bc;

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

import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.keys.Entropy;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.crypto.mpt.BlindingFactor;
import org.xrpl.xrpl4j.crypto.mpt.SecureRandomBlindingFactorGenerator;
import org.xrpl.xrpl4j.crypto.mpt.port.ElGamalCiphertext;

/**
 * Integration tests for ElGamal encryption and decryption roundtrip.
 *
 * <p>Tests that encryption followed by decryption recovers the original amount,
 * and that tampering with the ciphertext causes decryption to fail.</p>
 */
public class ElGamalEncryptionDecryptionTest {

  private BcElGamalEncryptor encryptor;
  private BcElGamalDecryptor decryptor;

  private KeyPair keyPair;
  private BlindingFactor blindingFactor;
  private SecureRandomBlindingFactorGenerator blindingFactorGenerator;

  @BeforeEach
  void setUp() {


    encryptor = new BcElGamalEncryptor();
    decryptor = new BcElGamalDecryptor();
    blindingFactorGenerator = new SecureRandomBlindingFactorGenerator();

    // Generate a fresh key pair for each test using ElGamal seed (32-byte entropy)
    Seed seed = Seed.elGamalSecp256k1SeedFromEntropy(Entropy.newInstance(32));
    keyPair = seed.deriveKeyPair();

    blindingFactor = blindingFactorGenerator.generate();
  }

  // ==================== Roundtrip Tests ====================

  @ParameterizedTest
  @ValueSource(longs = {0, 1, 100, 750, 12345, 100000})
  void encryptThenDecryptRecoversOriginalAmount(long amount) {
    UnsignedLong originalAmount = UnsignedLong.valueOf(amount);

    ElGamalCiphertext ciphertext = encryptor.encrypt(
      originalAmount,
      keyPair.publicKey(),
      blindingFactor
    );

    UnsignedLong decryptedAmount = decryptor.decrypt(
      ciphertext,
      keyPair.privateKey(),
      UnsignedLong.ZERO,
      UnsignedLong.valueOf(amount + 1) // maxAmount just above actual amount
    );

    assertThat(decryptedAmount).isEqualTo(originalAmount);
  }

  @Test
  void encryptThenDecryptWithDifferentBlindingFactors() {
    UnsignedLong amount = UnsignedLong.valueOf(500);

    // Encrypt with different blinding factors
    BlindingFactor bf1 = blindingFactorGenerator.generate();
    BlindingFactor bf2 = blindingFactorGenerator.generate();

    ElGamalCiphertext ct1 = encryptor.encrypt(amount, keyPair.publicKey(), bf1);
    ElGamalCiphertext ct2 = encryptor.encrypt(amount, keyPair.publicKey(), bf2);

    // Ciphertexts should be different
    assertThat(ct1.toHex()).isNotEqualTo(ct2.toHex());

    // But both should decrypt to the same amount
    UnsignedLong decrypted1 = decryptor.decrypt(ct1, keyPair.privateKey(), UnsignedLong.ZERO, UnsignedLong.valueOf(1000));
    UnsignedLong decrypted2 = decryptor.decrypt(ct2, keyPair.privateKey(), UnsignedLong.ZERO, UnsignedLong.valueOf(1000));

    assertThat(decrypted1).isEqualTo(amount);
    assertThat(decrypted2).isEqualTo(amount);
  }

  // ==================== Tamper Detection Tests ====================

  @Test
  void decryptionFailsWhenC1BitIsFlipped() {
    UnsignedLong amount = UnsignedLong.valueOf(100);

    ElGamalCiphertext ciphertext = encryptor.encrypt(amount, keyPair.publicKey(), blindingFactor);

    // Flip a bit in c1
    byte[] c1Bytes = ciphertext.c1().toByteArray();
    c1Bytes[c1Bytes.length - 1] ^= 0x01; // Flip last bit

    ElGamalCiphertext tamperedCiphertext = ElGamalCiphertext.of(
      UnsignedByteArray.of(c1Bytes),
      ciphertext.c2()
    );

    // Decryption should fail (amount not found or invalid point)
    assertThrows(
      IllegalStateException.class,
      () -> decryptor.decrypt(tamperedCiphertext, keyPair.privateKey(), UnsignedLong.ZERO, UnsignedLong.valueOf(1000))
    );
  }
}

