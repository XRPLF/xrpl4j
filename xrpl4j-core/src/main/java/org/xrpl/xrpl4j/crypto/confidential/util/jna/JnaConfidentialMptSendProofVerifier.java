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
 * mpt-crypto C library via {@link MptCryptoLibrary}.
 *
 * <p>Calls {@code mpt_verify_send_proof} from the native library to verify a compact
 * AND-composed sigma proof + aggregated Bulletproof.</p>
 */
public class JnaConfidentialMptSendProofVerifier implements ConfidentialMptSendProofVerifier {

  private static final int PUBKEY_SIZE = 33;
  private static final int CIPHERTEXT_SIZE = 66;

  private final MptCryptoLibrary lib;

  /**
   * Constructs a new instance using the default {@link MptCryptoLibrary} singleton.
   *
   * @throws UnsatisfiedLinkError if the native mpt-crypto library cannot be loaded.
   */
  public JnaConfidentialMptSendProofVerifier() {
    this(MptCryptoLibrary.getInstance());
  }

  /**
   * Constructs a new instance with the specified {@link MptCryptoLibrary}.
   *
   * @param lib The native library to delegate to.
   */
  public JnaConfidentialMptSendProofVerifier(final MptCryptoLibrary lib) {
    this.lib = Objects.requireNonNull(lib);
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

    // Build the MptConfidentialRecipient struct array for the native library
    MptCryptoLibrary.MptConfidentialRecipient firstRecipient = new MptCryptoLibrary.MptConfidentialRecipient();
    MptCryptoLibrary.MptConfidentialRecipient[] recipientArray =
      (MptCryptoLibrary.MptConfidentialRecipient[]) firstRecipient.toArray(numRecipients);
    for (int i = 0; i < numRecipients; i++) {
      MptConfidentialParty party = recipients.get(i);
      System.arraycopy(
        party.publicKey().value().toByteArray(), 0, recipientArray[i].pubkey, 0, PUBKEY_SIZE
      );
      System.arraycopy(
        party.encryptedAmount().toBytes().toByteArray(), 0,
        recipientArray[i].ciphertext, 0, CIPHERTEXT_SIZE
      );
    }

    byte[] proofBytes = proof.value().toByteArray();
    return lib.mpt_verify_send_proof(
      proofBytes,
      recipientArray[0], (byte) numRecipients,
      senderSpendingCiphertext.toBytes().toByteArray(),
      amountCommitment.value().toByteArray(),
      balanceCommitment.value().toByteArray(),
      context.value().toByteArray()
    ) == 0;
  }
}
