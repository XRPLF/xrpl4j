package org.xrpl.xrpl4j.crypto.confidential.bulletproof.bc;

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
import org.xrpl.xrpl4j.crypto.confidential.BlindingFactor;
import org.xrpl.xrpl4j.crypto.confidential.BlindingFactorGenerator;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.PlaintextEqualityProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.PlaintextEqualityProofVerifier;

import java.io.InputStream;

/**
 * Test for {@link BcPlaintextEqualityProofGenerator} comparing output with C implementation.
 *
 * <p>The C code was modified to use fixed nonces for the internal random scalar t.
 * We mock the BlindingFactorGenerator to return the same fixed nonce for each test vector.</p>
 */
@SuppressWarnings("checkstyle")
class BcPlaintextEqualityProofGeneratorTest {

  private static JsonNode testVectors;
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeAll
  static void loadTestVectors() throws Exception {
    InputStream is = BcPlaintextEqualityProofGeneratorTest.class.getResourceAsStream(
      "/mpt/port/equality_plaintext_vectors.json"
    );
    testVectors = objectMapper.readTree(is);
  }

  @Test
  void proofGenerationMatchesCImplementation() {
    for (JsonNode vector : testVectors.get("vectors")) {
      UnsignedLong amount = UnsignedLong.valueOf(vector.get("amount").asLong());
      String nonceHex = vector.get("nonce").asText();
      String c1Hex = vector.get("c1").asText();
      String c2Hex = vector.get("c2").asText();
      String pkHex = vector.get("pk").asText();
      String rHex = vector.get("r").asText();
      String contextIdHex = vector.get("contextId").asText();
      String expectedProofHex = vector.get("expectedProof").asText();

      UnsignedByteArray c1 = UnsignedByteArray.fromHex(c1Hex);
      UnsignedByteArray c2 = UnsignedByteArray.fromHex(c2Hex);
      UnsignedByteArray pk = UnsignedByteArray.fromHex(pkHex);
      UnsignedByteArray r = UnsignedByteArray.fromHex(rHex);
      UnsignedByteArray contextId = UnsignedByteArray.fromHex(contextIdHex);

      // Create generator with mocked BlindingFactorGenerator that returns the nonce from test vector
      BlindingFactor fixedNonce = BlindingFactor.fromHex(nonceHex);
      BlindingFactorGenerator mockGenerator = () -> fixedNonce;
      PlaintextEqualityProofGenerator generator = new BcPlaintextEqualityProofGenerator(mockGenerator);

      // Generate proof
      UnsignedByteArray proof = generator.generateProof(c1, c2, pk, amount, r, contextId);

      // Assert proof matches C output byte-for-byte
      assertThat(proof.hexValue())
        .as("Proof for amount=%s nonce=%s", amount, nonceHex)
        .isEqualToIgnoringCase(expectedProofHex);

      // Verify the proof using the verifier
      PlaintextEqualityProofVerifier verifier = new BcPlaintextEqualityProofVerifier();
      assertThat(verifier.verifyProof(proof, c1, c2, pk, amount, contextId))
        .as("Proof should verify for amount=%s", amount)
        .isTrue();
    }
  }

  @Test
  void modifiedRProducesDifferentProof() {
    JsonNode vector = testVectors.get("vectors").get(0);
    UnsignedLong amount = UnsignedLong.valueOf(vector.get("amount").asLong());
    String nonceHex = vector.get("nonce").asText();
    String expectedProofHex = vector.get("expectedProof").asText();

    UnsignedByteArray c1 = UnsignedByteArray.fromHex(vector.get("c1").asText());
    UnsignedByteArray c2 = UnsignedByteArray.fromHex(vector.get("c2").asText());
    UnsignedByteArray pk = UnsignedByteArray.fromHex(vector.get("pk").asText());
    UnsignedByteArray contextId = UnsignedByteArray.fromHex(vector.get("contextId").asText());

    // Flip a bit in r
    byte[] rBytes = UnsignedByteArray.fromHex(vector.get("r").asText()).toByteArray();
    rBytes[0] ^= 0x01;
    UnsignedByteArray modifiedR = UnsignedByteArray.of(rBytes);

    BlindingFactor fixedNonce = BlindingFactor.fromHex(nonceHex);
    BlindingFactorGenerator mockGenerator = () -> fixedNonce;
    PlaintextEqualityProofGenerator generator = new BcPlaintextEqualityProofGenerator(mockGenerator);

    // Generate proof with modified r — should produce a different proof
    UnsignedByteArray proof = generator.generateProof(c1, c2, pk, amount, modifiedR, contextId);

    assertThat(proof.hexValue())
      .isNotEqualToIgnoringCase(expectedProofHex);
  }

  @Test
  void testGenerateAndVerifyWithRandomNonces() {
    PlaintextEqualityProofGenerator generator = new BcPlaintextEqualityProofGenerator();
    PlaintextEqualityProofVerifier verifier = new BcPlaintextEqualityProofVerifier();

    // Use inputs from first test vector but with random nonces
    JsonNode vector = testVectors.get("vectors").get(0);
    UnsignedLong amount = UnsignedLong.valueOf(vector.get("amount").asLong());

    UnsignedByteArray c1 = UnsignedByteArray.fromHex(vector.get("c1").asText());
    UnsignedByteArray c2 = UnsignedByteArray.fromHex(vector.get("c2").asText());
    UnsignedByteArray pk = UnsignedByteArray.fromHex(vector.get("pk").asText());
    UnsignedByteArray r = UnsignedByteArray.fromHex(vector.get("r").asText());
    UnsignedByteArray contextId = UnsignedByteArray.fromHex(vector.get("contextId").asText());

    // Generate proof with random nonces
    UnsignedByteArray proof = generator.generateProof(c1, c2, pk, amount, r, contextId);

    // Verify the proof
    assertThat(verifier.verifyProof(proof, c1, c2, pk, amount, contextId)).isTrue();
  }
}
