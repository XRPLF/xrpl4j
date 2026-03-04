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
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.RangeProofGenerator;

import java.io.InputStream;

/**
 * Test for {@link BcRangeProofGenerator} comparing output with C implementation.
 */
public class BcRangeProofGeneratorTest {

  private static JsonNode testVectors;
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeAll
  static void loadTestVectors() throws Exception {
    InputStream is = BcRangeProofGeneratorTest.class.getResourceAsStream("/mpt/port/rangeproof_vectors.json");
    testVectors = objectMapper.readTree(is);
  }

  @Test
  void proofGenerationMatchesCImplementation() {
    for (JsonNode vector : testVectors.get("vectors")) {
      int m = vector.get("m").asInt();
      String nonceHex = vector.get("nonce").asText();
      String contextIdHex = vector.get("contextId").asText();
      String expectedProofHex = vector.get("expectedProof").asText();

      // Parse values
      JsonNode valuesNode = vector.get("values");
      UnsignedLong[] values = new UnsignedLong[m];
      for (int i = 0; i < m; i++) {
        values[i] = UnsignedLong.valueOf(valuesNode.get(i).asLong());
      }

      // Parse blindings into flat array
      JsonNode blindingsNode = vector.get("blindings");
      byte[] blindingsFlat = new byte[m * 32];
      for (int i = 0; i < m; i++) {
        byte[] b = UnsignedByteArray.fromHex(blindingsNode.get(i).asText()).toByteArray();
        System.arraycopy(b, 0, blindingsFlat, i * 32, 32);
      }

      // Mock BlindingFactorGenerator to return fixed nonce
      BlindingFactor fixedNonce = BlindingFactor.fromHex(nonceHex);
      BlindingFactorGenerator mockGenerator = () -> fixedNonce;

      // Get pkBase from test vector
      UnsignedByteArray pkBase = UnsignedByteArray.fromHex(vector.get("pkBase").asText());

      UnsignedByteArray contextId = UnsignedByteArray.fromHex(contextIdHex);

      RangeProofGenerator generator = new BcRangeProofGenerator(mockGenerator);
      UnsignedByteArray proof = generator.generateProof(
        values,
        UnsignedByteArray.of(blindingsFlat),
        pkBase,
        contextId
      );

      assertThat(proof.hexValue())
        .as("Proof for m=%d, values=%s", m, valuesNode)
        .isEqualToIgnoringCase(expectedProofHex);
    }
  }

  @Test
  void modifiedBlindingProducesDifferentProof() {
    for (JsonNode vector : testVectors.get("vectors")) {
      int m = vector.get("m").asInt();
      String nonceHex = vector.get("nonce").asText();
      String contextIdHex = vector.get("contextId").asText();
      String expectedProofHex = vector.get("expectedProof").asText();

      // Parse values
      JsonNode valuesNode = vector.get("values");
      UnsignedLong[] values = new UnsignedLong[m];
      for (int i = 0; i < m; i++) {
        values[i] = UnsignedLong.valueOf(valuesNode.get(i).asLong());
      }

      // Parse blindings into flat array and modify one bit
      JsonNode blindingsNode = vector.get("blindings");
      byte[] blindingsFlat = new byte[m * 32];
      for (int i = 0; i < m; i++) {
        byte[] b = UnsignedByteArray.fromHex(blindingsNode.get(i).asText()).toByteArray();
        System.arraycopy(b, 0, blindingsFlat, i * 32, 32);
      }
      blindingsFlat[blindingsFlat.length - 1] ^= 0x01;

      BlindingFactor fixedNonce = BlindingFactor.fromHex(nonceHex);
      BlindingFactorGenerator mockGenerator = () -> fixedNonce;

      UnsignedByteArray pkBase = UnsignedByteArray.fromHex(vector.get("pkBase").asText());

      UnsignedByteArray contextId = UnsignedByteArray.fromHex(contextIdHex);

      RangeProofGenerator generator = new BcRangeProofGenerator(mockGenerator);

      try {
        UnsignedByteArray modifiedProof = generator.generateProof(
          values,
          UnsignedByteArray.of(blindingsFlat),
          pkBase,
          contextId
        );

        assertThat(modifiedProof.hexValue())
          .as("Modified blinding should produce different proof for m=%d, values=%s", m, valuesNode)
          .isNotEqualToIgnoringCase(expectedProofHex);
      } catch (IllegalStateException | IllegalArgumentException e) {
        // Modified blinding may be invalid - that's acceptable
      }
    }
  }
}
