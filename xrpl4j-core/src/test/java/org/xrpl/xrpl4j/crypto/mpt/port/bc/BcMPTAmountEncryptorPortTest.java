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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.mpt.port.ElGamalCiphertext;
import org.xrpl.xrpl4j.crypto.mpt.port.ElGamalEncryptorPort;

import java.io.InputStream;

/**
 * Test for {@link BcElGamalEncryptorPort} comparing output with C implementation.
 */
public class BcMPTAmountEncryptorPortTest {

  private static JsonNode testVectors;
  private static final ObjectMapper objectMapper = new ObjectMapper();
  private final ElGamalEncryptorPort encryptor = new BcElGamalEncryptorPort();

  @BeforeAll
  static void loadTestVectors() throws Exception {
    InputStream is = BcMPTAmountEncryptorPortTest.class.getResourceAsStream("/mpt/port/elgamal_encrypt_vectors.json");
    testVectors = objectMapper.readTree(is);
  }

  @Test
  void encryptionMatchesCImplementation() {
    for (JsonNode vector : testVectors.get("vectors")) {
      String pubkeyQHex = vector.get("pubkeyQ").asText();
      String amountStr = vector.get("amount").asText();
      String blindingFactorHex = vector.get("blindingFactor").asText();
      String expectedCiphertextHex = vector.get("expectedCiphertext").asText();

      UnsignedByteArray pubkeyQ = UnsignedByteArray.fromHex(pubkeyQHex);
      UnsignedLong amount = UnsignedLong.valueOf(amountStr);
      UnsignedByteArray blindingFactor = UnsignedByteArray.fromHex(blindingFactorHex);

      ElGamalCiphertext javaCiphertext = encryptor.encrypt(amount, pubkeyQ, blindingFactor);

      assertThat(javaCiphertext.toBytes().hexValue())
          .as("Ciphertext for amount %s", amountStr)
          .isEqualToIgnoringCase(expectedCiphertextHex);
    }
  }

  @Test
  void modifiedPubkeyProducesDifferentCiphertext() {
    int vectorIndex = 0;
    for (JsonNode vector : testVectors.get("vectors")) {
      String pubkeyQHex = vector.get("pubkeyQ").asText();
      String amountStr = vector.get("amount").asText();
      String blindingFactorHex = vector.get("blindingFactor").asText();

      UnsignedByteArray pubkeyQ = UnsignedByteArray.fromHex(pubkeyQHex);
      UnsignedLong amount = UnsignedLong.valueOf(amountStr);
      UnsignedByteArray blindingFactor = UnsignedByteArray.fromHex(blindingFactorHex);

      ElGamalCiphertext originalCiphertext = encryptor.encrypt(amount, pubkeyQ, blindingFactor);

      // Modify one byte (last byte) of the public key and verify ciphertext differs
      byte[] modifiedBytes = pubkeyQ.toByteArray();
      modifiedBytes[modifiedBytes.length - 1] = (byte) (modifiedBytes[modifiedBytes.length - 1] ^ 0x01);
      UnsignedByteArray modifiedPubkey = UnsignedByteArray.of(modifiedBytes);

      try {
        ElGamalCiphertext modifiedCiphertext = encryptor.encrypt(amount, modifiedPubkey, blindingFactor);
        assertThat(modifiedCiphertext.toBytes().hexValue())
            .as("Vector %d: Ciphertext should differ when pubkey is modified", vectorIndex)
            .isNotEqualToIgnoringCase(originalCiphertext.toBytes().hexValue());
      } catch (Exception e) {
        // Modified pubkey may be invalid - that's acceptable
      }
      vectorIndex++;
    }
  }

  @Test
  void modifiedBlindingFactorProducesDifferentCiphertext() {
    int vectorIndex = 0;
    for (JsonNode vector : testVectors.get("vectors")) {
      String pubkeyQHex = vector.get("pubkeyQ").asText();
      String amountStr = vector.get("amount").asText();
      String blindingFactorHex = vector.get("blindingFactor").asText();

      UnsignedByteArray pubkeyQ = UnsignedByteArray.fromHex(pubkeyQHex);
      UnsignedLong amount = UnsignedLong.valueOf(amountStr);
      UnsignedByteArray blindingFactor = UnsignedByteArray.fromHex(blindingFactorHex);

      ElGamalCiphertext originalCiphertext = encryptor.encrypt(amount, pubkeyQ, blindingFactor);

      // Modify one byte (last byte) of the blinding factor and verify ciphertext differs
      byte[] modifiedBytes = blindingFactor.toByteArray();
      modifiedBytes[modifiedBytes.length - 1] = (byte) (modifiedBytes[modifiedBytes.length - 1] ^ 0x01);
      UnsignedByteArray modifiedBlindingFactor = UnsignedByteArray.of(modifiedBytes);

      try {
        ElGamalCiphertext modifiedCiphertext = encryptor.encrypt(amount, pubkeyQ, modifiedBlindingFactor);
        assertThat(modifiedCiphertext.toBytes().hexValue())
            .as("Vector %d: Ciphertext should differ when blinding factor is modified", vectorIndex)
            .isNotEqualToIgnoringCase(originalCiphertext.toBytes().hexValue());
      } catch (Exception e) {
        // Modified blinding factor may be invalid - that's acceptable
      }
      vectorIndex++;
    }
  }
}

