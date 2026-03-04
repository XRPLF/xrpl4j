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
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.PlaintextEqualityProofVerifier;

import java.io.InputStream;

/**
 * Test for {@link BcPlaintextEqualityProofVerifier} using test vectors from C implementation.
 */
class BcPlaintextEqualityProofVerifierTest {

  private static JsonNode testVectors;
  private static final ObjectMapper objectMapper = new ObjectMapper();
  private final PlaintextEqualityProofVerifier verifier = new BcPlaintextEqualityProofVerifier();

  @BeforeAll
  static void loadTestVectors() throws Exception {
    InputStream is = BcPlaintextEqualityProofVerifierTest.class.getResourceAsStream(
      "/mpt/port/equality_plaintext_vectors.json"
    );
    testVectors = objectMapper.readTree(is);
  }

  @Test
  void verificationSucceedsForValidProof() {
    for (JsonNode vector : testVectors.get("vectors")) {
      UnsignedLong amount = UnsignedLong.valueOf(vector.get("amount").asLong());
      UnsignedByteArray c1 = UnsignedByteArray.fromHex(vector.get("c1").asText());
      UnsignedByteArray c2 = UnsignedByteArray.fromHex(vector.get("c2").asText());
      UnsignedByteArray pk = UnsignedByteArray.fromHex(vector.get("pk").asText());
      UnsignedByteArray contextId = UnsignedByteArray.fromHex(vector.get("contextId").asText());
      UnsignedByteArray proof = UnsignedByteArray.fromHex(vector.get("expectedProof").asText());

      assertThat(verifier.verifyProof(proof, c1, c2, pk, amount, contextId))
        .as("Verification should succeed for amount=%s", amount)
        .isTrue();
    }
  }

  @Test
  void verificationFailsWhenProofIsModified() {
    JsonNode vector = testVectors.get("vectors").get(0);
    UnsignedLong amount = UnsignedLong.valueOf(vector.get("amount").asLong());
    UnsignedByteArray c1 = UnsignedByteArray.fromHex(vector.get("c1").asText());
    UnsignedByteArray c2 = UnsignedByteArray.fromHex(vector.get("c2").asText());
    UnsignedByteArray pk = UnsignedByteArray.fromHex(vector.get("pk").asText());
    UnsignedByteArray contextId = UnsignedByteArray.fromHex(vector.get("contextId").asText());

    // Flip a bit in the proof (in the s component at offset 66)
    byte[] proofBytes = UnsignedByteArray.fromHex(vector.get("expectedProof").asText()).toByteArray();
    proofBytes[70] ^= 0x01;
    UnsignedByteArray modifiedProof = UnsignedByteArray.of(proofBytes);

    assertThat(verifier.verifyProof(modifiedProof, c1, c2, pk, amount, contextId)).isFalse();
  }

  @Test
  void verificationFailsWhenAmountIsModified() {
    JsonNode vector = testVectors.get("vectors").get(0);
    UnsignedLong wrongAmount = UnsignedLong.valueOf(vector.get("amount").asLong() + 1);
    UnsignedByteArray c1 = UnsignedByteArray.fromHex(vector.get("c1").asText());
    UnsignedByteArray c2 = UnsignedByteArray.fromHex(vector.get("c2").asText());
    UnsignedByteArray pk = UnsignedByteArray.fromHex(vector.get("pk").asText());
    UnsignedByteArray contextId = UnsignedByteArray.fromHex(vector.get("contextId").asText());
    UnsignedByteArray proof = UnsignedByteArray.fromHex(vector.get("expectedProof").asText());

    assertThat(verifier.verifyProof(proof, c1, c2, pk, wrongAmount, contextId)).isFalse();
  }

  @Test
  void verificationFailsWhenContextIdIsModified() {
    JsonNode vector = testVectors.get("vectors").get(0);
    UnsignedLong amount = UnsignedLong.valueOf(vector.get("amount").asLong());
    UnsignedByteArray c1 = UnsignedByteArray.fromHex(vector.get("c1").asText());
    UnsignedByteArray c2 = UnsignedByteArray.fromHex(vector.get("c2").asText());
    UnsignedByteArray pk = UnsignedByteArray.fromHex(vector.get("pk").asText());
    UnsignedByteArray proof = UnsignedByteArray.fromHex(vector.get("expectedProof").asText());

    // Flip a bit in contextId
    byte[] ctxBytes = UnsignedByteArray.fromHex(vector.get("contextId").asText()).toByteArray();
    ctxBytes[0] ^= 0x01;
    UnsignedByteArray modifiedContextId = UnsignedByteArray.of(ctxBytes);

    assertThat(verifier.verifyProof(proof, c1, c2, pk, amount, modifiedContextId)).isFalse();
  }
}
