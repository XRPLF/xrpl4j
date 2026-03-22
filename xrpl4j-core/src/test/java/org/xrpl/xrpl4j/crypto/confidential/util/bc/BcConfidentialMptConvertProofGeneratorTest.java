package org.xrpl.xrpl4j.crypto.confidential.util.bc;

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

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.confidential.ConfidentialMptContextUtil;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptConvertContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptConvertProof;
import org.xrpl.xrpl4j.crypto.confidential.util.ConfidentialMptConvertProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.util.ConfidentialMptConvertProofVerifier;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

/**
 * Tests for {@link BcConfidentialMptConvertProofGenerator} and {@link BcConfidentialMptConvertProofVerifier}.
 */
class BcConfidentialMptConvertProofGeneratorTest {

  private static ConfidentialMptConvertProofGenerator generator;
  private static ConfidentialMptConvertProofVerifier verifier;
  private static KeyPair keyPair;
  private static ConfidentialMptConvertContext context;

  @BeforeAll
  static void setUp() {
    generator = new BcConfidentialMptConvertProofGenerator();
    verifier = new BcConfidentialMptConvertProofVerifier();

    // Generate secp256k1 key pair
    keyPair = Seed.elGamalSecp256k1SeedFromPassphrase(Passphrase.of("test-passphrase")).deriveKeyPair();

    // Create a test context using the utility
    context = ConfidentialMptContextUtil.generateConvertContext(
      Address.of("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"),
      UnsignedInteger.valueOf(12345),
      MpTokenIssuanceId.of("00000143A58DCB491FD36A15A7D3172E6A9F088A5478BA41")
    );
  }

  @Test
  void generateProofAndVerifySuccessfully() {
    // Generate proof
    ConfidentialMptConvertProof proof = generator.generateProof(keyPair, context);

    // Verify proof structure
    assertThat(proof).isNotNull();
    assertThat(proof.value().length()).isEqualTo(65);

    // Verify proof is valid
    boolean isValid = verifier.verifyProof(proof, keyPair.publicKey(), context);
    assertThat(isValid).isTrue();
  }

  @Test
  void proofBecomesInvalidWhenPublicKeyIsModified() {
    // Generate proof with original key pair
    ConfidentialMptConvertProof proof = generator.generateProof(keyPair, context);

    // Verify proof is valid with original public key
    assertThat(verifier.verifyProof(proof, keyPair.publicKey(), context)).isTrue();

    // Generate a different key pair
    KeyPair differentKeyPair = Seed.elGamalSecp256k1SeedFromPassphrase(
      Passphrase.of("different-passphrase")
    ).deriveKeyPair();

    // Verify proof is invalid with different public key
    boolean isValid = verifier.verifyProof(proof, differentKeyPair.publicKey(), context);
    assertThat(isValid).isFalse();
  }

  @Test
  void proofBecomesInvalidWhenContextIsModified() {
    // Generate proof with original context
    ConfidentialMptConvertProof proof = generator.generateProof(keyPair, context);

    // Verify proof is valid with original context
    assertThat(verifier.verifyProof(proof, keyPair.publicKey(), context)).isTrue();

    // Create a different context (different sequence)
    ConfidentialMptConvertContext differentContext = ConfidentialMptContextUtil.generateConvertContext(
      Address.of("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"),
      UnsignedInteger.valueOf(99999),  // Different sequence
      MpTokenIssuanceId.of("00000143A58DCB491FD36A15A7D3172E6A9F088A5478BA41")
    );

    // Verify proof is invalid with different context
    boolean isValid = verifier.verifyProof(proof, keyPair.publicKey(), differentContext);
    assertThat(isValid).isFalse();
  }
}
