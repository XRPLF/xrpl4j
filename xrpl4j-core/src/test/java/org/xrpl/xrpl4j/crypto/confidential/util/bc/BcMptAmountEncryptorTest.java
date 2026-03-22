package org.xrpl.xrpl4j.crypto.confidential.util.bc;

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
import static org.xrpl.xrpl4j.crypto.TestConstants.EC_PUBLIC_KEY;
import static org.xrpl.xrpl4j.crypto.TestConstants.ED_PUBLIC_KEY;

import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.confidential.BlindingFactor;
import org.xrpl.xrpl4j.crypto.confidential.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.confidential.elgamal.bc.BcElGamalEncryptor;
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;

/**
 * Unit tests for {@link BcMptAmountEncryptor}.
 */
class BcMptAmountEncryptorTest {

  private static final String VALID_BLINDING_FACTOR_HEX =
      "0A0C2EB9A16B16B80CFD1B1E8573B9C722E7E19A756B97788F4D97A56B3B246A";

  private BcMptAmountEncryptor encryptor;
  private BlindingFactor blindingFactor;

  @BeforeEach
  void setUp() {
    encryptor = new BcMptAmountEncryptor();
    blindingFactor = BlindingFactor.fromHex(VALID_BLINDING_FACTOR_HEX);
  }

  // ==================== Constructor Tests ====================

  @Test
  void constructorWithNullPortEncryptor() {
    assertThrows(NullPointerException.class, () -> new BcMptAmountEncryptor(null));
  }

  @Test
  void constructorWithSharedPortEncryptor() {
    BcElGamalEncryptor sharedPort = new BcElGamalEncryptor();
    BcMptAmountEncryptor encryptor1 = new BcMptAmountEncryptor(sharedPort);
    BcMptAmountEncryptor encryptor2 = new BcMptAmountEncryptor(sharedPort);

    EncryptedAmount result1 = encryptor1.encrypt(UnsignedLong.ONE, EC_PUBLIC_KEY, blindingFactor);
    EncryptedAmount result2 = encryptor2.encrypt(UnsignedLong.ONE, EC_PUBLIC_KEY, blindingFactor);

    assertThat(result1.toHex()).isEqualTo(result2.toHex());
  }

  // ==================== Null Input Tests ====================

  @Test
  void encryptWithNullAmount() {
    NullPointerException exception = assertThrows(
        NullPointerException.class,
        () -> encryptor.encrypt(null, EC_PUBLIC_KEY, blindingFactor)
    );
    assertThat(exception.getMessage()).isEqualTo("amount must not be null");
  }

  @Test
  void encryptWithNullPublicKey() {
    NullPointerException exception = assertThrows(
        NullPointerException.class,
        () -> encryptor.encrypt(UnsignedLong.ONE, null, blindingFactor)
    );
    assertThat(exception.getMessage()).isEqualTo("publicKey must not be null");
  }

  @Test
  void encryptWithNullBlindingFactor() {
    NullPointerException exception = assertThrows(
        NullPointerException.class,
        () -> encryptor.encrypt(UnsignedLong.ONE, EC_PUBLIC_KEY, null)
    );
    assertThat(exception.getMessage()).isEqualTo("blindingFactor must not be null");
  }

  // ==================== Validation Tests ====================

  @Test
  void encryptWithEmptyPublicKey() {
    PublicKey emptyPublicKey = PublicKey.MULTI_SIGN_PUBLIC_KEY;

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> encryptor.encrypt(UnsignedLong.ONE, emptyPublicKey, blindingFactor)
    );
    assertThat(exception.getMessage()).isEqualTo("publicKey must not be empty");
  }

  @Test
  void encryptWithEd25519PublicKey() {
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> encryptor.encrypt(UnsignedLong.ONE, ED_PUBLIC_KEY, blindingFactor)
    );
    assertThat(exception.getMessage()).isEqualTo("publicKey must be a SECP256K1 key, but was ED25519");
  }

  // ==================== Happy Path Test ====================

  @Test
  void encryptWithValidInputs() {
    EncryptedAmount result = encryptor.encrypt(UnsignedLong.valueOf(12345), EC_PUBLIC_KEY, blindingFactor);

    assertThat(result).isNotNull();
    assertThat(result.c1().length()).isEqualTo(Secp256k1Operations.ELGAMAL_CIPHER_SIZE);
    assertThat(result.c2().length()).isEqualTo(Secp256k1Operations.ELGAMAL_CIPHER_SIZE);
    assertThat(result.toBytes().length()).isEqualTo(Secp256k1Operations.ELGAMAL_TOTAL_SIZE);
    assertThat(result.toHex()).hasSize(132); // 66 bytes * 2 hex chars
  }
}

