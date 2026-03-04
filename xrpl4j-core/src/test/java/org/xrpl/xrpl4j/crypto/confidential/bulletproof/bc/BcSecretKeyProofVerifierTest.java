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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.SecretKeyProofVerifier;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.bc.BcSecretKeyProofVerifier;

import java.io.InputStream;

/**
 * Test for {@link BcSecretKeyProofVerifier} using C implementation test vectors.
 */
public class BcSecretKeyProofVerifierTest {

  private static JsonNode testVectors;
  private static final ObjectMapper objectMapper = new ObjectMapper();
  private final SecretKeyProofVerifier verifier = new BcSecretKeyProofVerifier();

  @BeforeAll
  static void loadTestVectors() throws Exception {
    InputStream is = BcSecretKeyProofVerifierTest.class.getResourceAsStream("/mpt/port/pok_sk_vectors.json");
    testVectors = objectMapper.readTree(is);
  }

  @Test
  void verificationSucceedsForValidProof() {
    for (JsonNode vector : testVectors.get("vectors")) {
      String pkHex = vector.get("pk").asText();
      String expectedProofHex = vector.get("expectedProof").asText();

      JsonNode contextIdNode = vector.get("contextId");
      String contextIdHex = contextIdNode.isNull() ? null : contextIdNode.asText();

      UnsignedByteArray pk = UnsignedByteArray.fromHex(pkHex);
      UnsignedByteArray proof = UnsignedByteArray.fromHex(expectedProofHex);
      UnsignedByteArray contextId = contextIdHex != null ? UnsignedByteArray.fromHex(contextIdHex) : null;

      boolean isValid = verifier.verifyProof(proof, pk, contextId);

      assertThat(isValid)
        .as("Proof should verify for pk=%s", pkHex)
        .isTrue();
    }
  }

  @Test
  void verificationFailsWhenPublicKeyIsModified() {
    for (JsonNode vector : testVectors.get("vectors")) {
      String pkHex = vector.get("pk").asText();
      String expectedProofHex = vector.get("expectedProof").asText();

      JsonNode contextIdNode = vector.get("contextId");
      String contextIdHex = contextIdNode.isNull() ? null : contextIdNode.asText();

      UnsignedByteArray pk = UnsignedByteArray.fromHex(pkHex);
      UnsignedByteArray proof = UnsignedByteArray.fromHex(expectedProofHex);
      UnsignedByteArray contextId = contextIdHex != null ? UnsignedByteArray.fromHex(contextIdHex) : null;

      // Modify one bit of the public key
      byte[] modifiedPkBytes = pk.toByteArray();
      modifiedPkBytes[modifiedPkBytes.length - 1] ^= 0x01;
      UnsignedByteArray modifiedPk = UnsignedByteArray.of(modifiedPkBytes);

      try {
        boolean isValid = verifier.verifyProof(proof, modifiedPk, contextId);
        assertThat(isValid)
          .as("Proof should NOT verify with modified pk for pk=%s", pkHex)
          .isFalse();
      } catch (IllegalArgumentException e) {
        // Modified pk may be an invalid point - that's acceptable (verification fails)
      }
    }
  }

  @Test
  void verificationFailsWhenContextIdIsModified() {
    for (JsonNode vector : testVectors.get("vectors")) {
      String pkHex = vector.get("pk").asText();
      String expectedProofHex = vector.get("expectedProof").asText();

      JsonNode contextIdNode = vector.get("contextId");
      String contextIdHex = contextIdNode.isNull() ? null : contextIdNode.asText();

      UnsignedByteArray pk = UnsignedByteArray.fromHex(pkHex);
      UnsignedByteArray proof = UnsignedByteArray.fromHex(expectedProofHex);

      UnsignedByteArray modifiedContextId;
      if (contextIdHex == null) {
        modifiedContextId = UnsignedByteArray.fromHex(
          "6FEA36EAD59A20D9BE215730B8D2995B67112721B67E1E14E3AFA809E83B2858");
      } else {
        // For vectors with contextId, modify one bit
        UnsignedByteArray contextId = UnsignedByteArray.fromHex(contextIdHex);
        byte[] modifiedContextBytes = contextId.toByteArray();
        modifiedContextBytes[modifiedContextBytes.length - 1] ^= 0x01;
        modifiedContextId = UnsignedByteArray.of(modifiedContextBytes);
      }

      boolean isValid = verifier.verifyProof(proof, pk, modifiedContextId);

      assertThat(isValid)
        .as("Proof should NOT verify with modified/added contextId for pk=%s", pkHex)
        .isFalse();
    }
  }

  @Test
  void verificationFailsWhenProofIsModified() {
    for (JsonNode vector : testVectors.get("vectors")) {
      String pkHex = vector.get("pk").asText();
      String expectedProofHex = vector.get("expectedProof").asText();

      JsonNode contextIdNode = vector.get("contextId");
      String contextIdHex = contextIdNode.isNull() ? null : contextIdNode.asText();

      UnsignedByteArray pk = UnsignedByteArray.fromHex(pkHex);
      UnsignedByteArray proof = UnsignedByteArray.fromHex(expectedProofHex);
      UnsignedByteArray contextId = contextIdHex != null ? UnsignedByteArray.fromHex(contextIdHex) : null;

      // Modify one bit of the proof (in the s component, byte 40)
      byte[] modifiedProofBytes = proof.toByteArray();
      modifiedProofBytes[40] ^= 0x01;
      UnsignedByteArray modifiedProof = UnsignedByteArray.of(modifiedProofBytes);

      boolean isValid = verifier.verifyProof(modifiedProof, pk, contextId);

      assertThat(isValid)
        .as("Modified proof should NOT verify for pk=%s", pkHex)
        .isFalse();
    }
  }
}
