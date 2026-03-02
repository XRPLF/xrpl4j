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

import org.xrpl.xrpl4j.codec.addresses.KeyType;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.mpt.context.ConfidentialMPTConvertContext;
import org.xrpl.xrpl4j.crypto.mpt.models.ConfidentialMPTConvertProof;
import org.xrpl.xrpl4j.crypto.mpt.port.SecretKeyProofVerifierPort;
import org.xrpl.xrpl4j.crypto.mpt.port.bc.BcSecretKeyProofVerifierPort;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.ConfidentialMPTConvertProofVerifier;

import java.util.Objects;

/**
 * BouncyCastle implementation of {@link ConfidentialMPTConvertProofVerifier}.
 *
 * <p>This implementation delegates to {@link BcSecretKeyProofVerifierPort} for the actual
 * cryptographic operations.</p>
 */
public class BcConfidentialMPTConvertProofVerifier implements ConfidentialMPTConvertProofVerifier {

  private final SecretKeyProofVerifierPort port;

  /**
   * Creates a new instance with the default port implementation.
   */
  public BcConfidentialMPTConvertProofVerifier() {
    this(new BcSecretKeyProofVerifierPort());
  }

  /**
   * Creates a new instance with a custom port implementation.
   *
   * @param port The port implementation to use.
   */
  public BcConfidentialMPTConvertProofVerifier(final BcSecretKeyProofVerifierPort port) {
    this.port = Objects.requireNonNull(port, "port must not be null");
  }

  @Override
  public boolean verifyProof(
    final ConfidentialMPTConvertProof proof,
    final PublicKey publicKey,
    final ConfidentialMPTConvertContext context
  ) {
    Objects.requireNonNull(proof, "proof must not be null");
    Objects.requireNonNull(publicKey, "publicKey must not be null");
    Objects.requireNonNull(context, "context must not be null");

    // Validate key type
    if (publicKey.keyType() != KeyType.SECP256K1) {
      throw new IllegalArgumentException(
        "publicKey must be SECP256K1, but was " + publicKey.keyType()
      );
    }

    // Extract public key bytes (33 bytes compressed)
    UnsignedByteArray pk = publicKey.value();

    // Get context bytes
    UnsignedByteArray contextId = context.value();

    return port.verifyProof(proof.value(), pk, contextId);
  }
}

