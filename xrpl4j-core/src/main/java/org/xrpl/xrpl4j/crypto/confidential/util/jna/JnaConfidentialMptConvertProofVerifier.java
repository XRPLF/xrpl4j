package org.xrpl.xrpl4j.crypto.confidential.util.jna;

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

import com.google.common.base.Preconditions;
import org.xrpl.xrpl4j.codec.addresses.KeyType;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptConvertContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptConvertProof;
import org.xrpl.xrpl4j.crypto.confidential.util.ConfidentialMptConvertProofVerifier;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;

import java.util.Objects;

/**
 * Implementation of {@link ConfidentialMptConvertProofVerifier} that delegates to the native mpt-crypto
 * C library via {@link MptCryptoLibrary}.
 *
 * <p>Calls {@code mpt_verify_convert_proof} from the native library to verify a 64-byte
 * compact Schnorr proof.</p>
 */
public class JnaConfidentialMptConvertProofVerifier implements ConfidentialMptConvertProofVerifier {

  private static final int PUBKEY_SIZE = 33;
  private static final int CONTEXT_HASH_SIZE = 32;
  private static final int PROOF_SIZE = 64;

  private final MptCryptoLibrary lib;

  /**
   * Constructs a new instance using the default {@link MptCryptoLibrary} singleton.
   *
   * @throws UnsatisfiedLinkError if the native mpt-crypto library cannot be loaded.
   */
  public JnaConfidentialMptConvertProofVerifier() {
    this(MptCryptoLibrary.getInstance());
  }

  /**
   * Constructs a new instance with the specified {@link MptCryptoLibrary}.
   *
   * @param lib The native library to delegate to.
   */
  public JnaConfidentialMptConvertProofVerifier(final MptCryptoLibrary lib) {
    this.lib = Objects.requireNonNull(lib);
  }

  @Override
  public boolean verifyProof(
    final ConfidentialMptConvertProof proof,
    final PublicKey publicKey,
    final ConfidentialMptConvertContext context
  ) {
    Objects.requireNonNull(proof, "proof must not be null");
    Objects.requireNonNull(publicKey, "publicKey must not be null");
    Objects.requireNonNull(context, "context must not be null");

    Preconditions.checkArgument(
      publicKey.keyType() == KeyType.SECP256K1,
      "publicKey must be a SECP256K1 key, but was %s",
      publicKey.keyType()
    );

    byte[] proofBytes = proof.value().toByteArray();
    Preconditions.checkArgument(
      proofBytes.length == PROOF_SIZE,
      "proof must be %s bytes, but was %s bytes",
      PROOF_SIZE, proofBytes.length
    );

    byte[] pubkeyBytes = publicKey.value().toByteArray();
    Preconditions.checkArgument(
      pubkeyBytes.length == PUBKEY_SIZE,
      "publicKey must be %s bytes, but was %s bytes",
      PUBKEY_SIZE, pubkeyBytes.length
    );

    byte[] ctxHash = context.value().toByteArray();
    Preconditions.checkArgument(
      ctxHash.length == CONTEXT_HASH_SIZE,
      "context hash must be %s bytes, but was %s bytes",
      CONTEXT_HASH_SIZE, ctxHash.length
    );

    return lib.mpt_verify_convert_proof(proofBytes, pubkeyBytes, ctxHash) == 0;
  }
}
