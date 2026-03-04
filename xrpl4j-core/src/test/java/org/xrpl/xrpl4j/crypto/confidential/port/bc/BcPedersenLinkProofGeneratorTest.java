package org.xrpl.xrpl4j.crypto.confidential.port.bc;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.confidential.BlindingFactor;
import org.xrpl.xrpl4j.crypto.confidential.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.PedersenLinkProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.bc.BcPedersenLinkProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.bc.BcPedersenLinkProofVerifier;
import org.xrpl.xrpl4j.crypto.confidential.commitment.bc.BcPedersenCommitment;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests for {@link BcPedersenLinkProofGenerator}.
 */
class BcPedersenLinkProofGeneratorTest {

  private static List<TestVector> testVectors;

  @BeforeAll
  static void loadTestVectors() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    InputStream is = BcPedersenLinkProofGeneratorTest.class
      .getResourceAsStream("/mpt/port/pedersen_link_vectors.json");
    JsonNode root = mapper.readTree(is);
    JsonNode vectors = root.get("vectors");

    testVectors = new ArrayList<>();
    for (JsonNode v : vectors) {
      testVectors.add(new TestVector(
        UnsignedLong.valueOf(v.get("amount").asText()),
        v.get("nonce").asText(),
        v.get("c1").asText(),
        v.get("c2").asText(),
        v.get("pk").asText(),
        v.get("pcm").asText(),
        v.get("r").asText(),
        v.get("rho").asText(),
        v.get("contextId").asText(),
        v.get("expectedProof").asText()
      ));
    }
  }

  @Test
  void proofMatchesCImplementation() {
    for (int i = 0; i < testVectors.size(); i++) {
      TestVector tv = testVectors.get(i);

      // Create generator with mocked nonce that returns the same nonce for km, kr, krho
      UnsignedByteArray nonce = UnsignedByteArray.of(BaseEncoding.base16().decode(tv.nonce));
      BcPedersenLinkProofGenerator generator = new BcPedersenLinkProofGenerator(
        () -> BlindingFactor.of(nonce)
      );

      UnsignedByteArray proof = generator.generateProof(
        UnsignedByteArray.of(BaseEncoding.base16().decode(tv.c1)),
        UnsignedByteArray.of(BaseEncoding.base16().decode(tv.c2)),
        UnsignedByteArray.of(BaseEncoding.base16().decode(tv.pk)),
        UnsignedByteArray.of(BaseEncoding.base16().decode(tv.pcm)),
        tv.amount,
        UnsignedByteArray.of(BaseEncoding.base16().decode(tv.r)),
        UnsignedByteArray.of(BaseEncoding.base16().decode(tv.rho)),
        UnsignedByteArray.of(BaseEncoding.base16().decode(tv.contextId))
      );

      assertThat(proof.length())
        .as("Proof size for test vector %d", i)
        .isEqualTo(PedersenLinkProofGenerator.PROOF_SIZE);

      String actualHex = BaseEncoding.base16().encode(proof.toByteArray());
      assertThat(actualHex)
        .as("Proof for test vector %d (amount=%s)", i, tv.amount)
        .isEqualTo(tv.expectedProof);
    }
  }

  @Test
  void changingOneBitInInputProducesDifferentProof() {
    for (int i = 0; i < testVectors.size(); i++) {
      TestVector tv = testVectors.get(i);

      UnsignedByteArray nonce = UnsignedByteArray.of(BaseEncoding.base16().decode(tv.nonce));
      BcPedersenLinkProofGenerator generator = new BcPedersenLinkProofGenerator(
        () -> BlindingFactor.of(nonce)
      );

      // Generate original proof
      UnsignedByteArray originalProof = generator.generateProof(
        UnsignedByteArray.of(BaseEncoding.base16().decode(tv.c1)),
        UnsignedByteArray.of(BaseEncoding.base16().decode(tv.c2)),
        UnsignedByteArray.of(BaseEncoding.base16().decode(tv.pk)),
        UnsignedByteArray.of(BaseEncoding.base16().decode(tv.pcm)),
        tv.amount,
        UnsignedByteArray.of(BaseEncoding.base16().decode(tv.r)),
        UnsignedByteArray.of(BaseEncoding.base16().decode(tv.rho)),
        UnsignedByteArray.of(BaseEncoding.base16().decode(tv.contextId))
      );

      // Flip one bit in r and generate new proof
      byte[] modifiedR = BaseEncoding.base16().decode(tv.r);
      modifiedR[0] ^= 0x01;

      UnsignedByteArray modifiedProof = generator.generateProof(
        UnsignedByteArray.of(BaseEncoding.base16().decode(tv.c1)),
        UnsignedByteArray.of(BaseEncoding.base16().decode(tv.c2)),
        UnsignedByteArray.of(BaseEncoding.base16().decode(tv.pk)),
        UnsignedByteArray.of(BaseEncoding.base16().decode(tv.pcm)),
        tv.amount,
        UnsignedByteArray.of(modifiedR),
        UnsignedByteArray.of(BaseEncoding.base16().decode(tv.rho)),
        UnsignedByteArray.of(BaseEncoding.base16().decode(tv.contextId))
      );

      assertThat(BaseEncoding.base16().encode(modifiedProof.toByteArray()))
        .as("Modified proof should differ for test vector %d", i)
        .isNotEqualTo(BaseEncoding.base16().encode(originalProof.toByteArray()));
    }
  }

  @Test
  void zeroAmountProofGenerationAndVerification() {
    // Use first test vector's inputs but with zero amount
    TestVector tv = testVectors.get(0);

    UnsignedByteArray nonce = UnsignedByteArray.of(BaseEncoding.base16().decode(tv.nonce));
    BcPedersenLinkProofGenerator generator = new BcPedersenLinkProofGenerator(
      () -> BlindingFactor.of(nonce)
    );

    // Generate inputs for zero amount
    UnsignedLong zeroAmount = UnsignedLong.ZERO;
    byte[] r = BaseEncoding.base16().decode(tv.r);
    byte[] rho = BaseEncoding.base16().decode(tv.rho);
    byte[] sk = BaseEncoding.base16().decode(tv.nonce); // Use nonce as sk for simplicity

    // Generate pk from sk
    BigInteger skInt = new BigInteger(1, sk);
    byte[] pk = Secp256k1Operations.serializeCompressed(Secp256k1Operations.multiplyG(skInt));

    // Generate c1 = r*G
    BigInteger rInt = new BigInteger(1, r);
    byte[] c1 = Secp256k1Operations.serializeCompressed(Secp256k1Operations.multiplyG(rInt));

    // Generate c2 = 0*G + r*Pk = r*Pk (for zero amount)
    byte[] c2 = Secp256k1Operations.serializeCompressed(
      Secp256k1Operations.multiply(Secp256k1Operations.deserialize(pk), rInt)
    );

    // Generate Pedersen commitment for zero amount
    BcPedersenCommitment commitmentPort = new BcPedersenCommitment();
    UnsignedByteArray pcm = commitmentPort.generateCommitment(zeroAmount, UnsignedByteArray.of(rho));

    // Generate proof - this should work for zero amount
    UnsignedByteArray proof = generator.generateProof(
      UnsignedByteArray.of(c1),
      UnsignedByteArray.of(c2),
      UnsignedByteArray.of(pk),
      pcm,
      zeroAmount,
      UnsignedByteArray.of(r),
      UnsignedByteArray.of(rho),
      UnsignedByteArray.of(BaseEncoding.base16().decode(tv.contextId))
    );

    assertThat(proof.length())
      .as("Proof size for zero amount")
      .isEqualTo(PedersenLinkProofGenerator.PROOF_SIZE);

    // Verify the proof
    BcPedersenLinkProofVerifier verifier = new BcPedersenLinkProofVerifier();
    boolean result = verifier.verifyProof(
      proof,
      UnsignedByteArray.of(c1),
      UnsignedByteArray.of(c2),
      UnsignedByteArray.of(pk),
      pcm,
      UnsignedByteArray.of(BaseEncoding.base16().decode(tv.contextId))
    );

    assertThat(result)
      .as("Zero amount proof should verify")
      .isTrue();
  }

  private static class TestVector {
    final UnsignedLong amount;
    final String nonce;
    final String c1;
    final String c2;
    final String pk;
    final String pcm;
    final String r;
    final String rho;
    final String contextId;
    final String expectedProof;

    TestVector(UnsignedLong amount, String nonce, String c1, String c2, String pk,
               String pcm, String r, String rho, String contextId, String expectedProof) {
      this.amount = amount;
      this.nonce = nonce;
      this.c1 = c1;
      this.c2 = c2;
      this.pk = pk;
      this.pcm = pcm;
      this.r = r;
      this.rho = rho;
      this.contextId = contextId;
      this.expectedProof = expectedProof;
    }
  }
}

