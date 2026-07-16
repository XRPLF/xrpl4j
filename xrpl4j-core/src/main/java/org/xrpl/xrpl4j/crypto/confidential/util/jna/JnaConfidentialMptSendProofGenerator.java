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
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.confidential.model.BlindingFactor;
import org.xrpl.xrpl4j.crypto.confidential.model.Commitment;
import org.xrpl.xrpl4j.crypto.confidential.model.MptConfidentialParty;
import org.xrpl.xrpl4j.crypto.confidential.model.PedersenProofParams;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptSendContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptSendProof;
import org.xrpl.xrpl4j.crypto.confidential.util.ConfidentialMptSendProofGenerator;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Implementation of {@link ConfidentialMptSendProofGenerator} that delegates to the native mpt-crypto
 * C library via {@link MptCryptoLibrary}.
 *
 * <p>Calls {@code mpt_get_confidential_send_proof} from the native library to generate a compact
 * AND-composed sigma proof (192 bytes) + aggregated Bulletproof (754 bytes) = 946 bytes total.</p>
 */
public class JnaConfidentialMptSendProofGenerator implements ConfidentialMptSendProofGenerator {

  private static final int PUBLIC_KEY_SIZE = 33;
  private static final int CIPHERTEXT_SIZE = 66;
  private static final int SEND_PROOF_SIZE = 946;
  private static final int MIN_PARTICIPANTS = 3;
  private static final int MAX_PARTICIPANTS = 4;

  private final MptCryptoLibrary lib;

  /**
   * Constructs a new instance using the default {@link MptCryptoLibrary} singleton.
   *
   * @throws UnsatisfiedLinkError if the native mpt-crypto library cannot be loaded.
   */
  public JnaConfidentialMptSendProofGenerator() {
    this(MptCryptoLibrary.getInstance());
  }

  /**
   * Constructs a new instance with the specified {@link MptCryptoLibrary}.
   *
   * @param lib The native library to delegate to.
   */
  public JnaConfidentialMptSendProofGenerator(final MptCryptoLibrary lib) {
    this.lib = Objects.requireNonNull(lib);
  }

  @Override
  public ConfidentialMptSendProof generateProof(
    final KeyPair senderKeyPair,
    final UnsignedLong amount,
    final List<MptConfidentialParty> participants,
    final BlindingFactor txBlindingFactor,
    final ConfidentialMptSendContext context,
    final Commitment amountCommitment,
    final PedersenProofParams balanceParams
  ) {
    Objects.requireNonNull(senderKeyPair, "senderKeyPair must not be null");
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(participants, "participants must not be null");
    Objects.requireNonNull(txBlindingFactor, "txBlindingFactor must not be null");
    Objects.requireNonNull(context, "context must not be null");
    Objects.requireNonNull(amountCommitment, "amountCommitment must not be null");
    Objects.requireNonNull(balanceParams, "balanceParams must not be null");

    Preconditions.checkArgument(
      senderKeyPair.publicKey().keyType() == KeyType.SECP256K1,
      "senderKeyPair must be SECP256K1"
    );
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
      System.arraycopy(party.publicKey().value().toByteArray(), 0, participantArray[i].publicKey, 0, PUBLIC_KEY_SIZE);
      System.arraycopy(
        party.encryptedAmount().value().toByteArray(), 0,
        participantArray[i].ciphertext, 0, CIPHERTEXT_SIZE
      );
    }

    // Build the MptPedersenProofParams struct for the balance
    MptCryptoLibrary.MptPedersenProofParams balanceStruct = new MptCryptoLibrary.MptPedersenProofParams();
    System.arraycopy(balanceParams.pedersenCommitment().toByteArray(), 0, balanceStruct.pedersenCommitment, 0, 33);
    balanceStruct.amount = balanceParams.amount().longValue();
    System.arraycopy(
      balanceParams.encryptedAmount().value().toByteArray(), 0,
      balanceStruct.encryptedAmount, 0, 66
    );
    System.arraycopy(
      balanceParams.blindingFactor().value().toByteArray(), 0,
      balanceStruct.blindingFactor, 0, 32
    );

    byte[] outProof = new byte[SEND_PROOF_SIZE];
    long[] outLen = new long[]{SEND_PROOF_SIZE};

    // Extract sender keys just before use; zero the private key copy when done
    byte[] privateKeyBytes = senderKeyPair.privateKey().naturalBytes().toByteArray();
    byte[] publicKeyBytes = senderKeyPair.publicKey().value().toByteArray();

    int result;
    try {
      result = lib.mpt_get_confidential_send_proof(
        privateKeyBytes, publicKeyBytes, amount.longValue(),
        participantArray, numParticipants,
        txBlindingFactor.value().toByteArray(), context.value().toByteArray(),
        amountCommitment.value().toByteArray(), balanceStruct,
        outProof, outLen
      );
    } finally {
      Arrays.fill(privateKeyBytes, (byte) 0);
    }

    if (result != 0) {
      throw new IllegalStateException("mpt_get_confidential_send_proof failed with error code: " + result);
    }
    Preconditions.checkState(
      outLen[0] == SEND_PROOF_SIZE,
      "mpt_get_confidential_send_proof wrote %s bytes, but expected %s bytes",
      outLen[0], SEND_PROOF_SIZE
    );

    return ConfidentialMptSendProof.of(UnsignedByteArray.of(outProof));
  }
}
