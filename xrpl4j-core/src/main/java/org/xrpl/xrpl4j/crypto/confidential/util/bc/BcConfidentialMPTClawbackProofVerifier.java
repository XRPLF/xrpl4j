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

import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMPTClawbackContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMPTClawbackProof;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.PlaintextEqualityProofVerifier;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.bc.BcPlaintextEqualityProofVerifier;
import org.xrpl.xrpl4j.crypto.confidential.util.ConfidentialMPTClawbackProofVerifier;

import java.util.Objects;

/**
 * BouncyCastle implementation of {@link ConfidentialMPTClawbackProofVerifier}.
 *
 * <p>This implementation uses the plaintext equality proof port to verify
 * a Chaum-Pedersen style proof that the issuer knows the plaintext amount.</p>
 *
 * <p>For clawback, rippled verifies with swapped parameters matching the generation.
 * This implementation handles the swapping internally.</p>
 */
public class BcConfidentialMPTClawbackProofVerifier implements ConfidentialMPTClawbackProofVerifier {

  private final PlaintextEqualityProofVerifier proofVerifierPort;

  /**
   * Creates a new instance with default port implementation.
   */
  public BcConfidentialMPTClawbackProofVerifier() {
    this(new BcPlaintextEqualityProofVerifier());
  }

  /**
   * Creates a new instance with custom port implementation.
   *
   * @param proofVerifierPort The plaintext equality proof verifier port.
   */
  public BcConfidentialMPTClawbackProofVerifier(final PlaintextEqualityProofVerifier proofVerifierPort) {
    this.proofVerifierPort = Objects.requireNonNull(proofVerifierPort, "proofVerifierPort must not be null");
  }

  @Override
  public boolean verifyProof(
    final ConfidentialMPTClawbackProof proof,
    final EncryptedAmount issuerEncryptedBalance,
    final PublicKey issuerPublicKey,
    final UnsignedLong amount,
    final ConfidentialMPTClawbackContext context
  ) {
    Objects.requireNonNull(proof, "proof must not be null");
    Objects.requireNonNull(issuerEncryptedBalance, "issuerEncryptedBalance must not be null");
    Objects.requireNonNull(issuerPublicKey, "issuerPublicKey must not be null");
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(context, "context must not be null");

    // Verify the plaintext equality proof with swapped parameters
    // Matching the generation: c1 <- issuer's pk, c2 <- balance.c2, pk <- balance.c1
    return proofVerifierPort.verifyProof(
      proof.value(),                                                       // The proof
      issuerPublicKey.value(),                                             // c1 <- issuer's public key
      issuerEncryptedBalance.c2(),  // c2 <- balance.c2
      issuerEncryptedBalance.c1(),  // pk <- balance.c1
      amount,                                                              // Amount being clawed back
      context.value()                             // Context hash
    );
  }
}

