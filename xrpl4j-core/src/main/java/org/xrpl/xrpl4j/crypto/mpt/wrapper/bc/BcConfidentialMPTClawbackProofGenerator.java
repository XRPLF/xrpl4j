package org.xrpl.xrpl4j.crypto.mpt.wrapper.bc;

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
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.mpt.context.ConfidentialMPTClawbackContext;
import org.xrpl.xrpl4j.crypto.mpt.models.ConfidentialMPTClawbackProof;
import org.xrpl.xrpl4j.crypto.mpt.port.ElGamalCiphertext;
import org.xrpl.xrpl4j.crypto.mpt.port.PlaintextEqualityProofGeneratorPort;
import org.xrpl.xrpl4j.crypto.mpt.port.bc.BcPlaintextEqualityProofGeneratorPort;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.ConfidentialMPTClawbackProofGenerator;

import java.util.Objects;

/**
 * BouncyCastle implementation of {@link ConfidentialMPTClawbackProofGenerator}.
 *
 * <p>This implementation uses the plaintext equality proof port to generate
 * a Chaum-Pedersen style proof that the issuer knows the plaintext amount.</p>
 *
 * <p>For clawback, rippled calls the C function with swapped parameters:
 * {@code secp256k1_equality_plaintext_prove(ctx, proof, &pk, &c2, &c1, amount, privateKey, contextHash)}
 * which maps to: c1 ← issuer's pk, c2 ← balance.c2, pk_recipient ← balance.c1.
 * This implementation handles the swapping internally.</p>
 */
public class BcConfidentialMPTClawbackProofGenerator implements ConfidentialMPTClawbackProofGenerator {

  private final PlaintextEqualityProofGeneratorPort proofGeneratorPort;

  /**
   * Creates a new instance with default port implementation.
   */
  public BcConfidentialMPTClawbackProofGenerator() {
    this(new BcPlaintextEqualityProofGeneratorPort());
  }

  /**
   * Creates a new instance with custom port implementation.
   *
   * @param proofGeneratorPort The plaintext equality proof generator port.
   */
  public BcConfidentialMPTClawbackProofGenerator(final PlaintextEqualityProofGeneratorPort proofGeneratorPort) {
    this.proofGeneratorPort = Objects.requireNonNull(proofGeneratorPort, "proofGeneratorPort must not be null");
  }

  @Override
  public ConfidentialMPTClawbackProof generateProof(
    final ElGamalCiphertext issuerEncryptedBalance,
    final PublicKey issuerPublicKey,
    final UnsignedLong amount,
    final PrivateKey issuerPrivateKey,
    final ConfidentialMPTClawbackContext context
  ) {
    Objects.requireNonNull(issuerEncryptedBalance, "issuerEncryptedBalance must not be null");
    Objects.requireNonNull(issuerPublicKey, "issuerPublicKey must not be null");
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(issuerPrivateKey, "issuerPrivateKey must not be null");
    Objects.requireNonNull(context, "context must not be null");

    // Generate the plaintext equality proof with swapped parameters
    // Rippled calls: secp256k1_equality_plaintext_prove(ctx, proof, &pk, &c2, &c1, ...)
    // So we swap: c1 <- issuer's pk, c2 <- balance.c2, pk_recipient <- balance.c1
    UnsignedByteArray proof = proofGeneratorPort.generateProof(
      issuerPublicKey.value(),                              // c1 <- issuer's public key
      issuerEncryptedBalance.c2(),  // c2 <- balance.c2
      issuerEncryptedBalance.c1(),  // pk <- balance.c1
      amount,                                               // Amount being clawed back
      issuerPrivateKey.naturalBytes(),                      // r <- issuer's private key
      context.value()               // Context hash
    );

    return ConfidentialMPTClawbackProof.of(proof);
  }
}

