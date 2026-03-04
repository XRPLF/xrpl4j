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

import org.xrpl.xrpl4j.codec.addresses.KeyType;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMPTConvertContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMPTConvertProof;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.SecretKeyProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.bc.BcSecretKeyProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.util.ConfidentialMPTConvertProofGenerator;

import java.util.Objects;

/**
 * BouncyCastle implementation of {@link ConfidentialMPTConvertProofGenerator}.
 *
 * <p>This implementation delegates to {@link BcSecretKeyProofGenerator} for the actual
 * cryptographic operations.</p>
 */
public class BcConfidentialMPTConvertProofGenerator implements ConfidentialMPTConvertProofGenerator {

  private final SecretKeyProofGenerator port;

  /**
   * Creates a new instance with the default port implementation.
   */
  public BcConfidentialMPTConvertProofGenerator() {
    this(new BcSecretKeyProofGenerator());
  }

  /**
   * Creates a new instance with a custom port implementation.
   *
   * @param port The port implementation to use.
   */
  public BcConfidentialMPTConvertProofGenerator(final BcSecretKeyProofGenerator port) {
    this.port = Objects.requireNonNull(port, "port must not be null");
  }

  @Override
  public ConfidentialMPTConvertProof generateProof(
    final KeyPair keyPair,
    final ConfidentialMPTConvertContext context
  ) {
    Objects.requireNonNull(keyPair, "keyPair must not be null");
    Objects.requireNonNull(context, "context must not be null");

    // Validate key type
    if (keyPair.publicKey().keyType() != KeyType.SECP256K1) {
      throw new IllegalArgumentException(
        "keyPair must be SECP256K1, but was " + keyPair.publicKey().keyType()
      );
    }

    // Extract public key bytes (33 bytes compressed)
    UnsignedByteArray pk = keyPair.publicKey().value();
    UnsignedByteArray sk = keyPair.privateKey().naturalBytes();

    // Get context bytes
    UnsignedByteArray contextId = context.value();

    UnsignedByteArray proofBytes = port.generateProof(pk, sk, contextId);
    return ConfidentialMPTConvertProof.of(proofBytes);
  }
}

