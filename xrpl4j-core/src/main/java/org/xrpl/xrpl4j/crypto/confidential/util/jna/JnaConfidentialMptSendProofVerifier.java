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
import org.xrpl.xrpl4j.crypto.confidential.model.Commitment;
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.confidential.model.MptConfidentialParty;
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
  private static final int MIN_PARTICIPANTS = 3;
  private static final int MAX_PARTICIPANTS = 4;

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
    final List<MptConfidentialParty> participants,
    final EncryptedAmount senderSpendingCiphertext,
    final ConfidentialMptSendContext context,
    final Commitment amountCommitment,
    final Commitment balanceCommitment
  ) {
    Objects.requireNonNull(proof, "proof must not be null");
    Objects.requireNonNull(participants, "participants must not be null");
    Objects.requireNonNull(senderSpendingCiphertext, "senderSpendingCiphertext must not be null");
    Objects.requireNonNull(context, "context must not be null");
    Objects.requireNonNull(amountCommitment, "amountCommitment must not be null");
    Objects.requireNonNull(balanceCommitment, "balanceCommitment must not be null");
    Preconditions.checkArgument(
      participants.size() >= MIN_PARTICIPANTS && participants.size() <= MAX_PARTICIPANTS,
      "participants must contain %s or %s entries (sender, destination, issuer, and optional auditor), but was %s",
      MIN_PARTICIPANTS, MAX_PARTICIPANTS, participants.size()
    );

    int numParticipants = participants.size();

    // Build the MptConfidentialParticipant struct array for the native library
    MptCryptoLibrary.MptConfidentialParticipant[] participantArray =
      (MptCryptoLibrary.MptConfidentialParticipant[])
        new MptCryptoLibrary.MptConfidentialParticipant().toArray(numParticipants);
    for (int i = 0; i < numParticipants; i++) {
      MptConfidentialParty party = participants.get(i);
      System.arraycopy(
        party.publicKey().value().toByteArray(), 0, participantArray[i].pubkey, 0, PUBKEY_SIZE
      );
      System.arraycopy(
        party.encryptedAmount().value().toByteArray(), 0,
        participantArray[i].ciphertext, 0, CIPHERTEXT_SIZE
      );
    }

    byte[] proofBytes = proof.value().toByteArray();
    return lib.mpt_verify_send_proof(
      proofBytes,
      participantArray, (byte) numParticipants,
      senderSpendingCiphertext.value().toByteArray(),
      amountCommitment.value().toByteArray(),
      balanceCommitment.value().toByteArray(),
      context.value().toByteArray()
    ) == 0;
  }
}
