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
import org.xrpl.xrpl4j.crypto.confidential.BlindingFactor;
import org.xrpl.xrpl4j.crypto.confidential.model.MptConfidentialParty;
import org.xrpl.xrpl4j.crypto.confidential.model.PedersenCommitment;
import org.xrpl.xrpl4j.crypto.confidential.model.PedersenProofParams;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptSendContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptSendProof;
import org.xrpl.xrpl4j.crypto.confidential.util.ConfidentialMptSendProofGenerator;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;

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

  private static final int PUBKEY_SIZE = 33;
  private static final int CIPHERTEXT_SIZE = 66;
  private static final int MAX_PROOF_SIZE = 4096;

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
    final List<MptConfidentialParty> recipients,
    final BlindingFactor txBlindingFactor,
    final ConfidentialMptSendContext context,
    final PedersenCommitment amountCommitment,
    final PedersenProofParams balanceParams
  ) {
    Objects.requireNonNull(senderKeyPair, "senderKeyPair must not be null");
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(recipients, "recipients must not be null");
    Objects.requireNonNull(txBlindingFactor, "txBlindingFactor must not be null");
    Objects.requireNonNull(context, "context must not be null");
    Objects.requireNonNull(amountCommitment, "amountCommitment must not be null");
    Objects.requireNonNull(balanceParams, "balanceParams must not be null");

    Preconditions.checkArgument(
      senderKeyPair.publicKey().keyType() == KeyType.SECP256K1,
      "senderKeyPair must be SECP256K1"
    );
    Preconditions.checkArgument(!recipients.isEmpty(), "recipients must not be empty");

    int numRecipients = recipients.size();

    // Extract sender keys
    UnsignedByteArray naturalBytes = senderKeyPair.privateKey().naturalBytes();
    byte[] privkey = naturalBytes.toByteArray();
    byte[] pubkey = senderKeyPair.publicKey().value().toByteArray();

    // Build the MptConfidentialRecipient struct array for the native library
    MptCryptoLibrary.MptConfidentialRecipient firstRecipient = new MptCryptoLibrary.MptConfidentialRecipient();
    MptCryptoLibrary.MptConfidentialRecipient[] recipientArray =
      (MptCryptoLibrary.MptConfidentialRecipient[]) firstRecipient.toArray(numRecipients);
    for (int i = 0; i < numRecipients; i++) {
      MptConfidentialParty party = recipients.get(i);
      System.arraycopy(party.publicKey().value().toByteArray(), 0, recipientArray[i].pubkey, 0, PUBKEY_SIZE);
      System.arraycopy(
        party.encryptedAmount().toBytes().toByteArray(), 0, recipientArray[i].ciphertext, 0, CIPHERTEXT_SIZE
      );
    }

    // Build the MptPedersenProofParams struct for the balance
    MptCryptoLibrary.MptPedersenProofParams balanceStruct = new MptCryptoLibrary.MptPedersenProofParams();
    System.arraycopy(balanceParams.pedersenCommitment().toByteArray(), 0, balanceStruct.pedersenCommitment, 0, 33);
    balanceStruct.amount = balanceParams.amount().longValue();
    System.arraycopy(balanceParams.encryptedAmount().toBytes().toByteArray(), 0, balanceStruct.encryptedAmount, 0, 66);
    System.arraycopy(balanceParams.blindingFactor().toBytes(), 0, balanceStruct.blindingFactor, 0, 32);

    byte[] outProof = new byte[MAX_PROOF_SIZE];
    long[] outLen = new long[]{MAX_PROOF_SIZE};

    int result = lib.mpt_get_confidential_send_proof(
      privkey, pubkey, amount.longValue(),
      recipientArray[0], numRecipients,
      txBlindingFactor.toBytes(), context.value().toByteArray(),
      amountCommitment.value().toByteArray(), balanceStruct,
      outProof, outLen
    );

    naturalBytes.destroy();

    if (result != 0) {
      throw new IllegalStateException("mpt_get_confidential_send_proof failed with error code: " + result);
    }

    byte[] proof = new byte[(int) outLen[0]];
    System.arraycopy(outProof, 0, proof, 0, (int) outLen[0]);
    return ConfidentialMptSendProof.of(UnsignedByteArray.of(proof));
  }
}
