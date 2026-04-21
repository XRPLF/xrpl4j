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
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.confidential.model.MptConfidentialParty;
import org.xrpl.xrpl4j.crypto.confidential.model.PedersenCommitment;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptSendContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptSendProof;
import org.xrpl.xrpl4j.crypto.confidential.util.ConfidentialMptSendProofVerifier;

import java.util.List;
import java.util.Objects;

/**
 * Implementation of {@link ConfidentialMptSendProofVerifier} that delegates to the native
 * mpt-crypto C library via the {@link NativeMptCrypto} bridge.
 */
public class JnaConfidentialMptSendProofVerifier implements ConfidentialMptSendProofVerifier {

  private static final int PUBKEY_SIZE = 33;
  private static final int CIPHERTEXT_SIZE = 66;

  private final NativeMptCrypto nativeCrypto;

  /**
   * Constructs a new instance by reflectively loading the native bridge.
   */
  public JnaConfidentialMptSendProofVerifier() {
    this(NativeMptCryptoLoader.getInstance());
  }

  /**
   * Constructs a new instance with the specified {@link NativeMptCrypto} bridge.
   *
   * @param nativeCrypto The native bridge to delegate to.
   */
  public JnaConfidentialMptSendProofVerifier(final NativeMptCrypto nativeCrypto) {
    this.nativeCrypto = Objects.requireNonNull(nativeCrypto);
  }

  @Override
  public boolean verifyProof(
    final ConfidentialMptSendProof proof,
    final List<MptConfidentialParty> recipients,
    final EncryptedAmount senderSpendingCiphertext,
    final ConfidentialMptSendContext context,
    final PedersenCommitment amountCommitment,
    final PedersenCommitment balanceCommitment
  ) {
    Objects.requireNonNull(proof, "proof must not be null");
    Objects.requireNonNull(recipients, "recipients must not be null");
    Objects.requireNonNull(context, "context must not be null");
    Objects.requireNonNull(amountCommitment, "amountCommitment must not be null");
    Objects.requireNonNull(balanceCommitment, "balanceCommitment must not be null");
    Preconditions.checkArgument(!recipients.isEmpty(), "recipients must not be empty");

    int numRecipients = recipients.size();
    byte[] recipientPubkeys = new byte[numRecipients * PUBKEY_SIZE];
    byte[] recipientCiphertexts = new byte[numRecipients * CIPHERTEXT_SIZE];
    for (int i = 0; i < numRecipients; i++) {
      MptConfidentialParty party = recipients.get(i);
      System.arraycopy(
        party.publicKey().value().toByteArray(), 0, recipientPubkeys, i * PUBKEY_SIZE, PUBKEY_SIZE
      );
      System.arraycopy(
        party.encryptedAmount().toBytes().toByteArray(), 0,
        recipientCiphertexts, i * CIPHERTEXT_SIZE, CIPHERTEXT_SIZE
      );
    }

    byte[] proofBytes = proof.value().toByteArray();
    return nativeCrypto.verifySendProof(
      recipientPubkeys, recipientCiphertexts, numRecipients,
      senderSpendingCiphertext.toBytes().toByteArray(),
      context.value().toByteArray(),
      amountCommitment.value().toByteArray(),
      balanceCommitment.value().toByteArray(),
      proofBytes
    ) == 0;
  }
}
