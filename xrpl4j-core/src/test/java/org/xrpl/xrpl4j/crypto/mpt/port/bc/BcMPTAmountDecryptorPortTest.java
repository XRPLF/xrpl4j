package org.xrpl.xrpl4j.crypto.mpt.port.bc;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.mpt.port.ElGamalCiphertext;
import org.xrpl.xrpl4j.crypto.mpt.port.ElGamalDecryptorPort;

import java.io.InputStream;

/**
 * Test for {@link BcElGamalDecryptorPort} comparing output with C implementation.
 */
public class BcMPTAmountDecryptorPortTest {

  private static JsonNode testVectors;
  private static final ObjectMapper objectMapper = new ObjectMapper();
  private final ElGamalDecryptorPort decryptor = new BcElGamalDecryptorPort();

  // Max amount for search (must be >= largest amount in test vectors)
  private static final UnsignedLong MAX_AMOUNT = UnsignedLong.valueOf(1_000_001);

  @BeforeAll
  static void loadTestVectors() throws Exception {
    InputStream is = BcMPTAmountDecryptorPortTest.class.getResourceAsStream("/mpt/port/elgamal_decrypt_vectors.json");
    testVectors = objectMapper.readTree(is);
  }

  @Test
  void decryptionMatchesCImplementation() {
    for (JsonNode vector : testVectors.get("vectors")) {
      String privkeyHex = vector.get("privkey").asText();
      String c1Hex = vector.get("c1").asText();
      String c2Hex = vector.get("c2").asText();
      String expectedAmountStr = vector.get("amount").asText();

      UnsignedByteArray privkey = UnsignedByteArray.fromHex(privkeyHex);
      ElGamalCiphertext ciphertext = ElGamalCiphertext.of(
        UnsignedByteArray.fromHex(c1Hex),
        UnsignedByteArray.fromHex(c2Hex)
      );
      UnsignedLong expectedAmount = UnsignedLong.valueOf(expectedAmountStr);

      UnsignedLong decryptedAmount = decryptor.decrypt(ciphertext, privkey, UnsignedLong.ZERO, MAX_AMOUNT);

      assertThat(decryptedAmount)
          .as("Decrypted amount for expected %s", expectedAmountStr)
          .isEqualTo(expectedAmount);
    }
  }

  @Test
  void decryptionWithExactMaxAmount() {
    String privkeyHex = "C0B39C0EB591B24024BE7058DC11FF754980F30E149FC5B99B5141476A5FE721";
    String c1Hex = "0200AF0B3513622E4DBFAB9BF96A19387582495CCD84B7A957BB5509DE5573181C";
    String c2Hex = "033D3F1C5554ABA476CAC62698CA30605B8FABAC5A388CD743E8F1CCCCD3DBA634";

    UnsignedByteArray privkey = UnsignedByteArray.fromHex(privkeyHex);
    ElGamalCiphertext ciphertext = ElGamalCiphertext.of(
      UnsignedByteArray.fromHex(c1Hex),
      UnsignedByteArray.fromHex(c2Hex)
    );

    UnsignedLong decryptedAmount = decryptor.decrypt(
      ciphertext, privkey, UnsignedLong.ZERO, UnsignedLong.valueOf(750)
    );

    assertThat(decryptedAmount).isEqualTo(UnsignedLong.valueOf(750));
  }

  @Test
  void decryptionWithMinAmountRange() {
    // Test that decryption works with a minAmount range
    // Hardcoded values from test vector with amount = 750
    String privkeyHex = "C0B39C0EB591B24024BE7058DC11FF754980F30E149FC5B99B5141476A5FE721";
    String c1Hex = "0200AF0B3513622E4DBFAB9BF96A19387582495CCD84B7A957BB5509DE5573181C";
    String c2Hex = "033D3F1C5554ABA476CAC62698CA30605B8FABAC5A388CD743E8F1CCCCD3DBA634";

    UnsignedByteArray privkey = UnsignedByteArray.fromHex(privkeyHex);
    ElGamalCiphertext ciphertext = ElGamalCiphertext.of(
      UnsignedByteArray.fromHex(c1Hex),
      UnsignedByteArray.fromHex(c2Hex)
    );

    // Search in range [700, 800] - should find 750
    UnsignedLong decryptedAmount = decryptor.decrypt(
      ciphertext, privkey, UnsignedLong.valueOf(700), UnsignedLong.valueOf(800)
    );

    assertThat(decryptedAmount).isEqualTo(UnsignedLong.valueOf(750));
  }

  @Test
  void decryptionFailsWhenAmountExceedsMaxAmount() {
    // Test that decryption fails when maxAmount is less than the actual amount
    // Hardcoded values from test vector with amount = 750
    String privkeyHex = "C0B39C0EB591B24024BE7058DC11FF754980F30E149FC5B99B5141476A5FE721";
    String c1Hex = "0200AF0B3513622E4DBFAB9BF96A19387582495CCD84B7A957BB5509DE5573181C";
    String c2Hex = "033D3F1C5554ABA476CAC62698CA30605B8FABAC5A388CD743E8F1CCCCD3DBA634";

    UnsignedByteArray privkey = UnsignedByteArray.fromHex(privkeyHex);
    ElGamalCiphertext ciphertext = ElGamalCiphertext.of(
      UnsignedByteArray.fromHex(c1Hex),
      UnsignedByteArray.fromHex(c2Hex)
    );

    IllegalStateException exception = assertThrows(
        IllegalStateException.class,
        () -> decryptor.decrypt(ciphertext, privkey, UnsignedLong.ZERO, UnsignedLong.valueOf(749))
    );

    assertThat(exception.getMessage()).contains("amount not found in range");
  }

  @Test
  void decryptionFailsWhenAmountBelowMinAmount() {
    // Test that decryption fails when minAmount is greater than the actual amount
    // Hardcoded values from test vector with amount = 750
    String privkeyHex = "C0B39C0EB591B24024BE7058DC11FF754980F30E149FC5B99B5141476A5FE721";
    String c1Hex = "0200AF0B3513622E4DBFAB9BF96A19387582495CCD84B7A957BB5509DE5573181C";
    String c2Hex = "033D3F1C5554ABA476CAC62698CA30605B8FABAC5A388CD743E8F1CCCCD3DBA634";

    UnsignedByteArray privkey = UnsignedByteArray.fromHex(privkeyHex);
    ElGamalCiphertext ciphertext = ElGamalCiphertext.of(
      UnsignedByteArray.fromHex(c1Hex),
      UnsignedByteArray.fromHex(c2Hex)
    );

    IllegalStateException exception = assertThrows(
        IllegalStateException.class,
        () -> decryptor.decrypt(ciphertext, privkey, UnsignedLong.valueOf(751), UnsignedLong.valueOf(1000))
    );

    assertThat(exception.getMessage()).contains("amount not found in range");
  }

  @Test
  void decryptionWithWrongPrivkeyFails() {
    // Use correct ciphertext but wrong private key
    // Hardcoded values: ciphertext from amount=1 vector, privkey from amount=750 vector
    String c1Hex = "03D24EE6F15541FD35E825D2A9B7B15A67780597D04935F8C435379C3331B96B04";
    String c2Hex = "02D65D0C31858A1E998F72356627B85A41D5EE288C7A373D31791092871A65674E";
    String wrongPrivkeyHex = "C0B39C0EB591B24024BE7058DC11FF754980F30E149FC5B99B5141476A5FE721";

    UnsignedByteArray wrongPrivkey = UnsignedByteArray.fromHex(wrongPrivkeyHex);
    ElGamalCiphertext ciphertext = ElGamalCiphertext.of(
      UnsignedByteArray.fromHex(c1Hex),
      UnsignedByteArray.fromHex(c2Hex)
    );

    // Should fail because wrong key produces wrong M_target
    IllegalStateException exception = assertThrows(
        IllegalStateException.class,
        () -> decryptor.decrypt(ciphertext, wrongPrivkey, UnsignedLong.ZERO, UnsignedLong.valueOf(1000))
    );

    assertThat(exception.getMessage()).contains("amount not found in range");
  }

  @Test
  void decryptionOfZeroAmount() {
    // Specifically test amount = 0 case (special handling in code)
    // Hardcoded values from test vector with amount = 0
    String privkeyHex = "0184DA63C975AB22A84722C26744CADDF516D0244DA6913E456DBCE31B960A91";
    String c1Hex = "025A85B3E987540E69A1861C23D1418E6486FBD5DFEC6A56A0BC3155F59D6B3310";
    String c2Hex = "0203BBFB37F2C5B74590ABAC5207D02C7D5C69342A138320DB1A4FAE01E4AB0F90";

    UnsignedByteArray privkey = UnsignedByteArray.fromHex(privkeyHex);
    ElGamalCiphertext ciphertext = ElGamalCiphertext.of(
      UnsignedByteArray.fromHex(c1Hex),
      UnsignedByteArray.fromHex(c2Hex)
    );

    UnsignedLong decryptedAmount = decryptor.decrypt(ciphertext, privkey, UnsignedLong.ZERO, UnsignedLong.ONE);

    assertThat(decryptedAmount).isEqualTo(UnsignedLong.ZERO);
  }
}

