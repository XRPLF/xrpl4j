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
import org.xrpl.xrpl4j.crypto.confidential.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.RangeProofVerifier;

import java.io.InputStream;

/**
 * Test for {@link BcRangeProofVerifier} using C implementation test vectors.
 */
@SuppressWarnings("checkstyle")
public class BcRangeProofVerifierTest {

  private static final int VALUE_BITS = 64;
  private static JsonNode testVectors;
  private static final ObjectMapper objectMapper = new ObjectMapper();
  private final RangeProofVerifier verifier = new BcRangeProofVerifier();

  @BeforeAll
  static void loadTestVectors() throws Exception {
    InputStream is = BcRangeProofVerifierTest.class.getResourceAsStream("/mpt/port/rangeproof_vectors.json");
    testVectors = objectMapper.readTree(is);
  }

  private UnsignedByteArray[] getGeneratorVector(String label, int n) {
    org.bouncycastle.math.ec.ECPoint[] points = Secp256k1Operations.getGeneratorVector(label, n);
    UnsignedByteArray[] vec = new UnsignedByteArray[n];
    for (int i = 0; i < n; i++) {
      vec[i] = UnsignedByteArray.of(Secp256k1Operations.serializeCompressed(points[i]));
    }
    return vec;
  }

  @Test
  void verificationSucceedsForValidProof() {
    for (JsonNode vector : testVectors.get("vectors")) {
      int m = vector.get("m").asInt();
      String proofHex = vector.get("expectedProof").asText();
      String contextIdHex = vector.get("contextId").asText();

      int n = VALUE_BITS * m;

      // Parse commitments
      JsonNode commitmentsNode = vector.get("commitments");
      UnsignedByteArray[] commitmentVec = new UnsignedByteArray[m];
      for (int i = 0; i < m; i++) {
        commitmentVec[i] = UnsignedByteArray.fromHex(commitmentsNode.get(i).asText());
      }

      // Get generator vectors
      UnsignedByteArray[] gVec = getGeneratorVector("G", n);
      UnsignedByteArray[] hVec = getGeneratorVector("H", n);

      UnsignedByteArray proof = UnsignedByteArray.fromHex(proofHex);
      UnsignedByteArray pkBase = UnsignedByteArray.fromHex(vector.get("pkBase").asText());
      UnsignedByteArray contextId = UnsignedByteArray.fromHex(contextIdHex);

      boolean isValid = verifier.verifyProof(gVec, hVec, proof, commitmentVec, pkBase, contextId);

      assertThat(isValid)
        .as("Proof should verify for m=%d, values=%s", m, vector.get("values"))
        .isTrue();
    }
  }

  @Test
  void verificationFailsWhenProofIsModified() {
    for (JsonNode vector : testVectors.get("vectors")) {
      int m = vector.get("m").asInt();
      String proofHex = vector.get("expectedProof").asText();
      String contextIdHex = vector.get("contextId").asText();

      int n = VALUE_BITS * m;

      JsonNode commitmentsNode = vector.get("commitments");
      UnsignedByteArray[] commitmentVec = new UnsignedByteArray[m];
      for (int i = 0; i < m; i++) {
        commitmentVec[i] = UnsignedByteArray.fromHex(commitmentsNode.get(i).asText());
      }

      UnsignedByteArray[] gVec = getGeneratorVector("G", n);
      UnsignedByteArray[] hVec = getGeneratorVector("H", n);

      // Modify one bit of the proof (in a scalar area, byte 150)
      byte[] modifiedProofBytes = UnsignedByteArray.fromHex(proofHex).toByteArray();
      modifiedProofBytes[150] ^= 0x01;
      UnsignedByteArray modifiedProof = UnsignedByteArray.of(modifiedProofBytes);

      UnsignedByteArray pkBase = UnsignedByteArray.fromHex(vector.get("pkBase").asText());
      UnsignedByteArray contextId = UnsignedByteArray.fromHex(contextIdHex);

      try {
        boolean isValid = verifier.verifyProof(gVec, hVec, modifiedProof, commitmentVec, pkBase, contextId);

        assertThat(isValid)
          .as("Modified proof should NOT verify for m=%d, values=%s", m, vector.get("values"))
          .isFalse();
      } catch (IllegalArgumentException e) {
        // Modified proof may cause deserialization failure - that's acceptable (verification fails)
      }
    }
  }

  @Test
  void verificationFailsWhenCommitmentIsModified() {
    for (JsonNode vector : testVectors.get("vectors")) {
      int m = vector.get("m").asInt();
      String proofHex = vector.get("expectedProof").asText();
      String contextIdHex = vector.get("contextId").asText();

      int n = VALUE_BITS * m;

      JsonNode commitmentsNode = vector.get("commitments");
      UnsignedByteArray[] commitmentVec = new UnsignedByteArray[m];
      for (int i = 0; i < m; i++) {
        commitmentVec[i] = UnsignedByteArray.fromHex(commitmentsNode.get(i).asText());
      }

      // Modify one bit of the first commitment
      byte[] modifiedBytes = commitmentVec[0].toByteArray();
      modifiedBytes[modifiedBytes.length - 1] ^= 0x01;
      commitmentVec[0] = UnsignedByteArray.of(modifiedBytes);

      UnsignedByteArray[] gVec = getGeneratorVector("G", n);
      UnsignedByteArray[] hVec = getGeneratorVector("H", n);

      UnsignedByteArray proof = UnsignedByteArray.fromHex(proofHex);
      UnsignedByteArray pkBase = UnsignedByteArray.fromHex(vector.get("pkBase").asText());
      UnsignedByteArray contextId = UnsignedByteArray.fromHex(contextIdHex);

      try {
        boolean isValid = verifier.verifyProof(gVec, hVec, proof, commitmentVec, pkBase, contextId);

        assertThat(isValid)
          .as("Proof should NOT verify with modified commitment for m=%d, values=%s", m, vector.get("values"))
          .isFalse();
      } catch (IllegalArgumentException e) {
        // Modified commitment may be an invalid point - that's acceptable
      }
    }
  }

  @Test
  void verificationFailsWhenContextIdIsModified() {
    for (JsonNode vector : testVectors.get("vectors")) {
      int m = vector.get("m").asInt();
      String proofHex = vector.get("expectedProof").asText();
      String contextIdHex = vector.get("contextId").asText();

      int n = VALUE_BITS * m;

      JsonNode commitmentsNode = vector.get("commitments");
      UnsignedByteArray[] commitmentVec = new UnsignedByteArray[m];
      for (int i = 0; i < m; i++) {
        commitmentVec[i] = UnsignedByteArray.fromHex(commitmentsNode.get(i).asText());
      }

      UnsignedByteArray[] gVec = getGeneratorVector("G", n);
      UnsignedByteArray[] hVec = getGeneratorVector("H", n);

      UnsignedByteArray proof = UnsignedByteArray.fromHex(proofHex);
      UnsignedByteArray pkBase = UnsignedByteArray.fromHex(vector.get("pkBase").asText());

      // Modify one bit of contextId
      byte[] modifiedContextBytes = UnsignedByteArray.fromHex(contextIdHex).toByteArray();
      modifiedContextBytes[modifiedContextBytes.length - 1] ^= 0x01;
      UnsignedByteArray modifiedContextId = UnsignedByteArray.of(modifiedContextBytes);

      boolean isValid = verifier.verifyProof(gVec, hVec, proof, commitmentVec, pkBase, modifiedContextId);

      assertThat(isValid)
        .as("Proof should NOT verify with modified contextId for m=%d, values=%s", m, vector.get("values"))
        .isFalse();
    }
  }
}
