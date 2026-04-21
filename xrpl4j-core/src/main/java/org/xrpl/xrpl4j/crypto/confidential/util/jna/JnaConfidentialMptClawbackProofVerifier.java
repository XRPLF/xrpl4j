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
import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.codec.addresses.KeyType;
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptClawbackContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptClawbackProof;
import org.xrpl.xrpl4j.crypto.confidential.util.ConfidentialMptClawbackProofVerifier;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;

import java.util.Objects;

/**
 * Implementation of {@link ConfidentialMptClawbackProofVerifier} that delegates to the native
 * mpt-crypto C library via the {@link NativeMptCrypto} bridge.
 *
 * <p>Calls {@code mpt_verify_clawback_proof} from the native library to verify
 * a 64-byte compact sigma proof.</p>
 */
public class JnaConfidentialMptClawbackProofVerifier implements ConfidentialMptClawbackProofVerifier {

  private final NativeMptCrypto nativeCrypto;

  /**
   * Constructs a new instance by reflectively loading the native bridge.
   */
  public JnaConfidentialMptClawbackProofVerifier() {
    this(NativeMptCryptoLoader.getInstance());
  }

  /**
   * Constructs a new instance with the specified {@link NativeMptCrypto} bridge.
   *
   * @param nativeCrypto The native bridge to delegate to.
   */
  public JnaConfidentialMptClawbackProofVerifier(final NativeMptCrypto nativeCrypto) {
    this.nativeCrypto = Objects.requireNonNull(nativeCrypto);
  }

  @Override
  public boolean verifyProof(
    final ConfidentialMptClawbackProof proof,
    final EncryptedAmount issuerEncryptedBalance,
    final PublicKey issuerPublicKey,
    final UnsignedLong amount,
    final ConfidentialMptClawbackContext context
  ) {
    Objects.requireNonNull(proof, "proof must not be null");
    Objects.requireNonNull(issuerEncryptedBalance, "issuerEncryptedBalance must not be null");
    Objects.requireNonNull(issuerPublicKey, "issuerPublicKey must not be null");
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(context, "context must not be null");

    Preconditions.checkArgument(
      issuerPublicKey.keyType() == KeyType.SECP256K1,
      "issuerPublicKey must be SECP256K1"
    );

    return nativeCrypto.verifyClawbackProof(
      proof.value().toByteArray(),
      issuerEncryptedBalance.c1().toByteArray(),
      issuerEncryptedBalance.c2().toByteArray(),
      issuerPublicKey.value().toByteArray(),
      amount.longValue(),
      context.value().toByteArray()
    ) == 0;
  }
}
