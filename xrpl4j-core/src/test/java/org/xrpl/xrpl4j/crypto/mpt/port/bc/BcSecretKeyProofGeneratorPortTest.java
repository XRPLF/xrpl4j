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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.mpt.BlindingFactor;
import org.xrpl.xrpl4j.crypto.mpt.BlindingFactorGenerator;
import org.xrpl.xrpl4j.crypto.mpt.port.SecretKeyProofGeneratorPort;

import java.io.InputStream;

/**
 * Test for {@link BcSecretKeyProofGeneratorPort} comparing output with C implementation.
 */
public class BcSecretKeyProofGeneratorPortTest {

  private static JsonNode testVectors;
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeAll
  static void loadTestVectors() throws Exception {
    InputStream is = BcSecretKeyProofGeneratorPortTest.class.getResourceAsStream("/mpt/port/pok_sk_vectors.json");
    testVectors = objectMapper.readTree(is);
  }

  @Test
  void proofGenerationMatchesCImplementation() {
    for (JsonNode vector : testVectors.get("vectors")) {
      String skHex = vector.get("sk").asText();
      String pkHex = vector.get("pk").asText();
      String nonceHex = vector.get("nonce").asText();
      String expectedProofHex = vector.get("expectedProof").asText();

      JsonNode contextIdNode = vector.get("contextId");
      String contextIdHex = contextIdNode.isNull() ? null : contextIdNode.asText();

      UnsignedByteArray sk = UnsignedByteArray.fromHex(skHex);
      UnsignedByteArray pk = UnsignedByteArray.fromHex(pkHex);
      UnsignedByteArray contextId = contextIdHex != null ? UnsignedByteArray.fromHex(contextIdHex) : null;

      // Create a mock BlindingFactorGenerator that returns the nonce from the test vector
      BlindingFactor fixedNonce = BlindingFactor.fromHex(nonceHex);
      BlindingFactorGenerator mockGenerator = () -> fixedNonce;

      SecretKeyProofGeneratorPort generator = new BcSecretKeyProofGeneratorPort(mockGenerator);
      UnsignedByteArray proof = generator.generateProof(pk, sk, contextId);

      assertThat(proof.hexValue())
        .as("Proof for sk=%s", skHex)
        .isEqualToIgnoringCase(expectedProofHex);
    }
  }

  @Test
  void modifiedSecretKeyProducesDifferentProof() {
    for (JsonNode vector : testVectors.get("vectors")) {
      String skHex = vector.get("sk").asText();
      String pkHex = vector.get("pk").asText();
      String nonceHex = vector.get("nonce").asText();
      String expectedProofHex = vector.get("expectedProof").asText();

      JsonNode contextIdNode = vector.get("contextId");
      String contextIdHex = contextIdNode.isNull() ? null : contextIdNode.asText();

      UnsignedByteArray sk = UnsignedByteArray.fromHex(skHex);
      UnsignedByteArray pk = UnsignedByteArray.fromHex(pkHex);
      UnsignedByteArray contextId = contextIdHex != null ? UnsignedByteArray.fromHex(contextIdHex) : null;

      // Modify one bit of the secret key
      byte[] modifiedSkBytes = sk.toByteArray();
      modifiedSkBytes[modifiedSkBytes.length - 1] ^= 0x01;
      UnsignedByteArray modifiedSk = UnsignedByteArray.of(modifiedSkBytes);

      BlindingFactor fixedNonce = BlindingFactor.fromHex(nonceHex);
      BlindingFactorGenerator mockGenerator = () -> fixedNonce;

      SecretKeyProofGeneratorPort generator = new BcSecretKeyProofGeneratorPort(mockGenerator);

      try {
        UnsignedByteArray modifiedProof = generator.generateProof(pk, modifiedSk, contextId);
        assertThat(modifiedProof.hexValue())
          .as("Modified sk should produce different proof for sk=%s", skHex)
          .isNotEqualToIgnoringCase(expectedProofHex);
      } catch (IllegalStateException e) {
        // Modified sk may be invalid - that's acceptable
      }
    }
  }
}

