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
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.PedersenLinkProofVerifier;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.bc.BcPedersenLinkProofVerifier;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests for {@link BcPedersenLinkProofVerifier}.
 */
class BcPedersenLinkProofVerifierTest {

  private static List<TestVector> testVectors;
  private static PedersenLinkProofVerifier verifier;

  @BeforeAll
  static void loadTestVectors() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    InputStream is = BcPedersenLinkProofVerifierTest.class
      .getResourceAsStream("/mpt/port/pedersen_link_vectors.json");
    JsonNode root = mapper.readTree(is);
    JsonNode vectors = root.get("vectors");

    testVectors = new ArrayList<>();
    for (JsonNode v : vectors) {
      testVectors.add(new TestVector(
        UnsignedLong.valueOf(v.get("amount").asText()),
        v.get("c1").asText(),
        v.get("c2").asText(),
        v.get("pk").asText(),
        v.get("pcm").asText(),
        v.get("contextId").asText(),
        v.get("expectedProof").asText()
      ));
    }

    verifier = new BcPedersenLinkProofVerifier();
  }

  @Test
  void verifyCGeneratedProofs() {
    for (int i = 0; i < testVectors.size(); i++) {
      TestVector tv = testVectors.get(i);

      boolean result = verifier.verifyProof(
        UnsignedByteArray.of(BaseEncoding.base16().decode(tv.expectedProof)),
        UnsignedByteArray.of(BaseEncoding.base16().decode(tv.c1)),
        UnsignedByteArray.of(BaseEncoding.base16().decode(tv.c2)),
        UnsignedByteArray.of(BaseEncoding.base16().decode(tv.pk)),
        UnsignedByteArray.of(BaseEncoding.base16().decode(tv.pcm)),
        UnsignedByteArray.of(BaseEncoding.base16().decode(tv.contextId))
      );

      assertThat(result)
        .as("Verification should pass for test vector %d (amount=%s)", i, tv.amount)
        .isTrue();
    }
  }

  @Test
  void changingOneBitInProofFailsVerification() {
    for (int i = 0; i < testVectors.size(); i++) {
      TestVector tv = testVectors.get(i);

      // Flip one bit in the proof
      byte[] modifiedProof = BaseEncoding.base16().decode(tv.expectedProof);
      modifiedProof[50] ^= 0x01; // Flip a bit in the middle of the proof

      boolean result = verifier.verifyProof(
        UnsignedByteArray.of(modifiedProof),
        UnsignedByteArray.of(BaseEncoding.base16().decode(tv.c1)),
        UnsignedByteArray.of(BaseEncoding.base16().decode(tv.c2)),
        UnsignedByteArray.of(BaseEncoding.base16().decode(tv.pk)),
        UnsignedByteArray.of(BaseEncoding.base16().decode(tv.pcm)),
        UnsignedByteArray.of(BaseEncoding.base16().decode(tv.contextId))
      );

      assertThat(result)
        .as("Verification should fail for modified proof in test vector %d", i)
        .isFalse();
    }
  }

  @Test
  void changingOneBitInC1FailsVerification() {
    for (int i = 0; i < testVectors.size(); i++) {
      TestVector tv = testVectors.get(i);

      // Flip one bit in c1
      byte[] modifiedC1 = BaseEncoding.base16().decode(tv.c1);
      modifiedC1[10] ^= 0x01;

      boolean result = verifier.verifyProof(
        UnsignedByteArray.of(BaseEncoding.base16().decode(tv.expectedProof)),
        UnsignedByteArray.of(modifiedC1),
        UnsignedByteArray.of(BaseEncoding.base16().decode(tv.c2)),
        UnsignedByteArray.of(BaseEncoding.base16().decode(tv.pk)),
        UnsignedByteArray.of(BaseEncoding.base16().decode(tv.pcm)),
        UnsignedByteArray.of(BaseEncoding.base16().decode(tv.contextId))
      );

      assertThat(result)
        .as("Verification should fail for modified c1 in test vector %d", i)
        .isFalse();
    }
  }

  private static class TestVector {
    final UnsignedLong amount;
    final String c1;
    final String c2;
    final String pk;
    final String pcm;
    final String contextId;
    final String expectedProof;

    TestVector(UnsignedLong amount, String c1, String c2, String pk,
               String pcm, String contextId, String expectedProof) {
      this.amount = amount;
      this.c1 = c1;
      this.c2 = c2;
      this.pk = pk;
      this.pcm = pcm;
      this.contextId = contextId;
      this.expectedProof = expectedProof;
    }
  }
}
