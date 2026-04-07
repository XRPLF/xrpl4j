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
 * C library via the {@link NativeMptCrypto} bridge.
 *
 * <p>Calls {@code secp256k1_mpt_pok_sk_verify} from the native library to verify a 65-byte
 * Schnorr Proof of Knowledge.</p>
 */
public class JnaConfidentialMptConvertProofVerifier implements ConfidentialMptConvertProofVerifier {

  private static final int PUBKEY_SIZE = 33;
  private static final int CONTEXT_HASH_SIZE = 32;
  private static final int PROOF_SIZE = 65;

  private final NativeMptCrypto nativeCrypto;

  /**
   * Constructs a new instance by reflectively loading the native bridge from {@code xrpl4j-mpt-crypto}.
   *
   * @throws IllegalStateException if {@code xrpl4j-mpt-crypto} is not on the classpath.
   */
  public JnaConfidentialMptConvertProofVerifier() {
    this(NativeMptCryptoLoader.getInstance());
  }

  /**
   * Constructs a new instance with the specified {@link NativeMptCrypto} bridge.
   *
   * @param nativeCrypto The native bridge to delegate to.
   */
  public JnaConfidentialMptConvertProofVerifier(final NativeMptCrypto nativeCrypto) {
    this.nativeCrypto = Objects.requireNonNull(nativeCrypto);
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

    return nativeCrypto.verifyConvertProof(proofBytes, pubkeyBytes, ctxHash) == 1;
  }
}
