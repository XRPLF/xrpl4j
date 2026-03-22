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
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.SamePlaintextProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.SamePlaintextProofVerifier;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.bc.BcSamePlaintextProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.bc.BcSamePlaintextProofVerifier;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Test for {@link BcSamePlaintextProofGenerator} comparing output with C implementation.
 *
 * <p>The C code was modified to use fixed nonces for km and all kr_i.
 * We mock the BlindingFactorGenerator to return the same fixed nonce for each test vector.</p>
 */
@SuppressWarnings("checkstyle")
class BcSamePlaintextProofGeneratorTest {

  private static JsonNode testVectors;
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeAll
  static void loadTestVectors() throws Exception {
    InputStream is = BcSamePlaintextProofGeneratorTest.class.getResourceAsStream(
      "/mpt/port/same_plaintext_multi_vectors.json"
    );
    testVectors = objectMapper.readTree(is);
  }

  @Test
  void proofGenerationMatchesCImplementation() {
    for (JsonNode vector : testVectors.get("vectors")) {
      UnsignedLong amount = UnsignedLong.valueOf(vector.get("amount").asLong());
      String nonceHex = vector.get("nonce").asText();
      String contextIdHex = vector.get("contextId").asText();
      String expectedProofHex = vector.get("expectedProof").asText();

      // Parse Pk array
      List<UnsignedByteArray> Pk = new ArrayList<>();
      for (JsonNode pkNode : vector.get("Pk")) {
        Pk.add(UnsignedByteArray.fromHex(pkNode.asText()));
      }

      // Parse R array
      List<UnsignedByteArray> R = new ArrayList<>();
      for (JsonNode rNode : vector.get("R")) {
        R.add(UnsignedByteArray.fromHex(rNode.asText()));
      }

      // Parse S array
      List<UnsignedByteArray> S = new ArrayList<>();
      for (JsonNode sNode : vector.get("S")) {
        S.add(UnsignedByteArray.fromHex(sNode.asText()));
      }

      // Parse rArray
      List<UnsignedByteArray> rArray = new ArrayList<>();
      for (JsonNode rArrNode : vector.get("rArray")) {
        rArray.add(UnsignedByteArray.fromHex(rArrNode.asText()));
      }

      UnsignedByteArray contextId = UnsignedByteArray.fromHex(contextIdHex);

      // Create generator with mocked BlindingFactorGenerator that returns the nonce from test vector
      BlindingFactor fixedNonce = BlindingFactor.fromHex(nonceHex);
      BlindingFactorGenerator mockGenerator = () -> fixedNonce;
      SamePlaintextProofGenerator generator = new BcSamePlaintextProofGenerator(mockGenerator);

      // Generate proof
      UnsignedByteArray proof = generator.generateProof(amount, R, S, Pk, rArray, contextId);

      // Assert proof matches C output byte-for-byte
      assertThat(proof.hexValue())
        .as("Proof for nonce=%s", nonceHex)
        .isEqualToIgnoringCase(expectedProofHex);

      // Verify the proof using the verifier
      SamePlaintextProofVerifier verifier = new BcSamePlaintextProofVerifier();
      assertThat(verifier.verifyProof(proof, R, S, Pk, contextId))
        .as("Proof should verify for nonce=%s", nonceHex)
        .isTrue();
    }
  }

  @Test
  void testGenerateAndVerifyWithRandomNonces() {
    // Test with real random nonces to ensure the proof generation and verification work together
    SamePlaintextProofGenerator generator = new BcSamePlaintextProofGenerator();
    SamePlaintextProofVerifier verifier = new BcSamePlaintextProofVerifier();

    // Use inputs from first test vector but with random nonces
    JsonNode vector = testVectors.get("vectors").get(0);
    UnsignedLong amount = UnsignedLong.valueOf(vector.get("amount").asLong());
    String contextIdHex = vector.get("contextId").asText();

    List<UnsignedByteArray> Pk = new ArrayList<>();
    for (JsonNode pkNode : vector.get("Pk")) {
      Pk.add(UnsignedByteArray.fromHex(pkNode.asText()));
    }

    List<UnsignedByteArray> R = new ArrayList<>();
    for (JsonNode rNode : vector.get("R")) {
      R.add(UnsignedByteArray.fromHex(rNode.asText()));
    }

    List<UnsignedByteArray> S = new ArrayList<>();
    for (JsonNode sNode : vector.get("S")) {
      S.add(UnsignedByteArray.fromHex(sNode.asText()));
    }

    List<UnsignedByteArray> rArray = new ArrayList<>();
    for (JsonNode rArrNode : vector.get("rArray")) {
      rArray.add(UnsignedByteArray.fromHex(rArrNode.asText()));
    }

    UnsignedByteArray contextId = UnsignedByteArray.fromHex(contextIdHex);

    // Generate proof with random nonces
    UnsignedByteArray proof = generator.generateProof(amount, R, S, Pk, rArray, contextId);

    // Verify the proof
    assertThat(verifier.verifyProof(proof, R, S, Pk, contextId)).isTrue();
  }
}
