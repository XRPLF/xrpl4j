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

import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.confidential.BlindingFactor;
import org.xrpl.xrpl4j.crypto.confidential.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.confidential.SecureRandomBlindingFactorGenerator;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.SamePlaintextProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.SamePlaintextProofVerifier;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Test for {@link BcSamePlaintextProofGenerator} and {@link BcSamePlaintextProofVerifier}
 * using the shared-r (equality_shared_r) algorithm.
 */
@SuppressWarnings("checkstyle")
class BcSamePlaintextProofGeneratorTest {

  @Test
  void testGenerateAndVerifyWithRandomNonces() {
    // Test with real random nonces to ensure the proof generation and verification work together
    SamePlaintextProofGenerator generator = new BcSamePlaintextProofGenerator();
    SamePlaintextProofVerifier verifier = new BcSamePlaintextProofVerifier();

    SecureRandomBlindingFactorGenerator blindingGen = new SecureRandomBlindingFactorGenerator();

    UnsignedLong amount = UnsignedLong.valueOf(1000);

    // Generate a shared blinding factor r
    BlindingFactor sharedR = blindingGen.generate();
    UnsignedByteArray sharedRBytes = UnsignedByteArray.of(sharedR.toBytes());
    BigInteger rInt = new BigInteger(1, sharedR.toBytes());

    // Compute shared C1 = r * G
    ECPoint c1Point = Secp256k1Operations.multiplyG(rInt);
    UnsignedByteArray c1 = UnsignedByteArray.of(Secp256k1Operations.serializeCompressed(c1Point));

    // Generate 3 recipient key pairs
    int n = 3;
    List<UnsignedByteArray> pkList = new ArrayList<>();
    List<UnsignedByteArray> c2List = new ArrayList<>();

    BigInteger mInt = BigInteger.valueOf(amount.longValue());
    ECPoint mG = Secp256k1Operations.multiplyG(mInt);

    for (int i = 0; i < n; i++) {
      // Generate a random private key for this recipient
      BlindingFactor sk = blindingGen.generate();
      BigInteger skInt = new BigInteger(1, sk.toBytes());
      ECPoint pk = Secp256k1Operations.multiplyG(skInt);
      pkList.add(UnsignedByteArray.of(Secp256k1Operations.serializeCompressed(pk)));

      // C2_i = m * G + r * Pk_i
      ECPoint rPk = Secp256k1Operations.multiply(pk, rInt);
      ECPoint c2 = Secp256k1Operations.add(mG, rPk);
      c2List.add(UnsignedByteArray.of(Secp256k1Operations.serializeCompressed(c2)));
    }

    UnsignedByteArray contextId = UnsignedByteArray.of(new byte[32]);

    // Generate proof with random nonces
    UnsignedByteArray proof = generator.generateProof(amount, sharedRBytes, c1, c2List, pkList, contextId);

    // Verify the proof size
    assertThat(proof.length()).isEqualTo(SamePlaintextProofGenerator.proofSize(n));

    // Verify the proof
    assertThat(verifier.verifyProof(proof, c1, c2List, pkList, contextId)).isTrue();
  }

  @Test
  void testVerifyFailsWithWrongAmount() {
    SamePlaintextProofGenerator generator = new BcSamePlaintextProofGenerator();
    SamePlaintextProofVerifier verifier = new BcSamePlaintextProofVerifier();

    SecureRandomBlindingFactorGenerator blindingGen = new SecureRandomBlindingFactorGenerator();

    UnsignedLong amount = UnsignedLong.valueOf(1000);
    UnsignedLong wrongAmount = UnsignedLong.valueOf(2000);

    BlindingFactor sharedR = blindingGen.generate();
    UnsignedByteArray sharedRBytes = UnsignedByteArray.of(sharedR.toBytes());
    BigInteger rInt = new BigInteger(1, sharedR.toBytes());

    ECPoint c1Point = Secp256k1Operations.multiplyG(rInt);
    UnsignedByteArray c1 = UnsignedByteArray.of(Secp256k1Operations.serializeCompressed(c1Point));

    int n = 2;
    List<UnsignedByteArray> pkList = new ArrayList<>();
    List<UnsignedByteArray> c2List = new ArrayList<>();

    BigInteger mInt = BigInteger.valueOf(amount.longValue());
    ECPoint mG = Secp256k1Operations.multiplyG(mInt);

    for (int i = 0; i < n; i++) {
      BlindingFactor sk = blindingGen.generate();
      BigInteger skInt = new BigInteger(1, sk.toBytes());
      ECPoint pk = Secp256k1Operations.multiplyG(skInt);
      pkList.add(UnsignedByteArray.of(Secp256k1Operations.serializeCompressed(pk)));

      ECPoint rPk = Secp256k1Operations.multiply(pk, rInt);
      ECPoint c2 = Secp256k1Operations.add(mG, rPk);
      c2List.add(UnsignedByteArray.of(Secp256k1Operations.serializeCompressed(c2)));
    }

    UnsignedByteArray contextId = UnsignedByteArray.of(new byte[32]);

    // Generate proof with wrong amount
    UnsignedByteArray proof = generator.generateProof(wrongAmount, sharedRBytes, c1, c2List, pkList, contextId);

    // Verification should fail because the proof was generated with a different amount
    assertThat(verifier.verifyProof(proof, c1, c2List, pkList, contextId)).isFalse();
  }

  @Test
  void testProofSize() {
    assertThat(SamePlaintextProofGenerator.proofSize(2)).isEqualTo(33 * 3 + 64);
    assertThat(SamePlaintextProofGenerator.proofSize(3)).isEqualTo(33 * 4 + 64);
    assertThat(SamePlaintextProofGenerator.proofSize(5)).isEqualTo(33 * 6 + 64);
  }
}
