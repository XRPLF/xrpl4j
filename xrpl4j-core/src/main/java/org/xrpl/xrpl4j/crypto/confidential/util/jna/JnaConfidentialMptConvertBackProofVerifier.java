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
import org.xrpl.xrpl4j.crypto.confidential.model.PedersenCommitment;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptConvertBackContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptConvertBackProof;
import org.xrpl.xrpl4j.crypto.confidential.util.ConfidentialMptConvertBackProofVerifier;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;

import java.util.Objects;

/**
 * Implementation of {@link ConfidentialMptConvertBackProofVerifier} that delegates to the native
 * mpt-crypto C library via the {@link NativeMptCrypto} bridge.
 */
public class JnaConfidentialMptConvertBackProofVerifier implements ConfidentialMptConvertBackProofVerifier {

  private final NativeMptCrypto nativeCrypto;

  /**
   * Constructs a new instance by reflectively loading the native bridge.
   */
  public JnaConfidentialMptConvertBackProofVerifier() {
    this(NativeMptCryptoLoader.getInstance());
  }

  /**
   * Constructs a new instance with the specified {@link NativeMptCrypto} bridge.
   *
   * @param nativeCrypto The native bridge to delegate to.
   */
  public JnaConfidentialMptConvertBackProofVerifier(final NativeMptCrypto nativeCrypto) {
    this.nativeCrypto = Objects.requireNonNull(nativeCrypto);
  }

  @Override
  public boolean verifyProof(
    final ConfidentialMptConvertBackProof proof,
    final PublicKey senderPublicKey,
    final EncryptedAmount encryptedBalance,
    final PedersenCommitment balanceCommitment,
    final UnsignedLong amount,
    final ConfidentialMptConvertBackContext context
  ) {
    Objects.requireNonNull(proof, "proof must not be null");
    Objects.requireNonNull(senderPublicKey, "senderPublicKey must not be null");
    Objects.requireNonNull(encryptedBalance, "encryptedBalance must not be null");
    Objects.requireNonNull(balanceCommitment, "balanceCommitment must not be null");
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(context, "context must not be null");

    Preconditions.checkArgument(
      senderPublicKey.keyType() == KeyType.SECP256K1, "senderPublicKey must be SECP256K1"
    );

    return nativeCrypto.verifyConvertBackProof(
      senderPublicKey.value().toByteArray(),
      context.value().toByteArray(),
      amount.longValue(),
      encryptedBalance.toBytes().toByteArray(),
      balanceCommitment.value().toByteArray(),
      proof.value().toByteArray()
    ) == 0;
  }
}
