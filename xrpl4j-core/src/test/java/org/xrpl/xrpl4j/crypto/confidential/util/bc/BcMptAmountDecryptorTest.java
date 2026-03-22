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
import static org.xrpl.xrpl4j.crypto.TestConstants.getEdPrivateKey;

import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.KeyType;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.confidential.elgamal.bc.BcElGamalDecryptor;
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;

/**
 * Tests for {@link BcMptAmountDecryptor} wrapper-specific functionality.
 *
 * <p>Port-level cryptographic correctness is tested in {@link BcElGamalDecryptor}.</p>
 */
public class BcMptAmountDecryptorTest {

  private BcMptAmountDecryptor decryptor;

  // Valid ciphertext from test vectors (amount = 1)
  private static final String VALID_C1_HEX = "03D24EE6F15541FD35E825D2A9B7B15A67780597D04935F8C435379C3331B96B04";
  private static final String VALID_C2_HEX = "02D65D0C31858A1E998F72356627B85A41D5EE288C7A373D31791092871A65674E";
  private static final String VALID_PRIVKEY_HEX = "BB5DF5240C857D04FA119EB837D17FD2046E56BD261E677D7981CB086CAB2729";

  private EncryptedAmount validCiphertext;
  private PrivateKey validPrivateKey;

  @BeforeEach
  void setUp() {
    decryptor = new BcMptAmountDecryptor();
    validCiphertext = EncryptedAmount.of(
      UnsignedByteArray.fromHex(VALID_C1_HEX),
      UnsignedByteArray.fromHex(VALID_C2_HEX)
    );
    validPrivateKey = PrivateKey.fromNaturalBytes(
      UnsignedByteArray.fromHex(VALID_PRIVKEY_HEX),
      KeyType.SECP256K1
    );
  }

  // ==================== Constructor Tests ====================

  @Test
  void constructorWithNullPortDecryptor() {
    assertThrows(NullPointerException.class, () -> new BcMptAmountDecryptor(null));
  }

  @Test
  void constructorWithSharedPortDecryptor() {
    BcElGamalDecryptor sharedPort = new BcElGamalDecryptor();
    BcMptAmountDecryptor decryptor1 = new BcMptAmountDecryptor(sharedPort);
    BcMptAmountDecryptor decryptor2 = new BcMptAmountDecryptor(sharedPort);

    UnsignedLong result1 = decryptor1.decrypt(
      validCiphertext, validPrivateKey, UnsignedLong.ZERO, UnsignedLong.valueOf(10)
    );
    UnsignedLong result2 = decryptor2.decrypt(
      validCiphertext, validPrivateKey, UnsignedLong.ZERO, UnsignedLong.valueOf(10)
    );

    assertThat(result1).isEqualTo(result2);
  }

  // ==================== Null Input Tests ====================

  @Test
  void decryptWithNullCiphertext() {
    assertThrows(
      NullPointerException.class,
      () -> decryptor.decrypt(null, validPrivateKey, UnsignedLong.ZERO, UnsignedLong.ONE)
    );
  }

  @Test
  void decryptWithNullPrivateKey() {
    assertThrows(
      NullPointerException.class,
      () -> decryptor.decrypt(validCiphertext, null, UnsignedLong.ZERO, UnsignedLong.ONE)
    );
  }

  @Test
  void decryptWithNullMinAmount() {
    assertThrows(
      NullPointerException.class,
      () -> decryptor.decrypt(validCiphertext, validPrivateKey, null, UnsignedLong.ONE)
    );
  }

  @Test
  void decryptWithNullMaxAmount() {
    assertThrows(
      NullPointerException.class,
      () -> decryptor.decrypt(validCiphertext, validPrivateKey, UnsignedLong.ZERO, null)
    );
  }

  // ==================== Validation Tests ====================

  @Test
  void decryptWithEd25519PrivateKey() {
    PrivateKey edPrivateKey = getEdPrivateKey();

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> decryptor.decrypt(validCiphertext, edPrivateKey, UnsignedLong.ZERO, UnsignedLong.ONE)
    );

    assertThat(exception.getMessage()).contains("SECP256K1");
  }

  // ==================== Happy Path Test ====================

  @Test
  void decryptWithValidInputs() {
    UnsignedLong result = decryptor.decrypt(
      validCiphertext, validPrivateKey, UnsignedLong.ZERO, UnsignedLong.valueOf(10)
    );

    assertThat(result).isEqualTo(UnsignedLong.ONE);
  }
}

