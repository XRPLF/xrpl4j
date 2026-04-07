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
import org.xrpl.xrpl4j.crypto.confidential.model.PedersenProofParams;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptSendContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptSendProof;
import org.xrpl.xrpl4j.crypto.confidential.util.ConfidentialMptSendProofGenerator;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;

import java.util.List;
import java.util.Objects;

/**
 * Implementation of {@link ConfidentialMptSendProofGenerator} that delegates to the native mpt-crypto
 * C library via the {@link NativeMptCrypto} bridge.
 *
 * <p>Calls {@code mpt_get_confidential_send_proof} from the native library to generate the combined
 * proof (same-plaintext + amount linkage + balance linkage + range proof).</p>
 */
public class JnaConfidentialMptSendProofGenerator implements ConfidentialMptSendProofGenerator {

  private static final int PUBKEY_SIZE = 33;
  private static final int PRIVKEY_SIZE = 32;
  private static final int CIPHERTEXT_SIZE = 66;
  private static final int MAX_PROOF_SIZE = 4096;

  private final NativeMptCrypto nativeCrypto;

  /**
   * Constructs a new instance by reflectively loading the native bridge.
   */
  public JnaConfidentialMptSendProofGenerator() {
    this(NativeMptCryptoLoader.getInstance());
  }

  /**
   * Constructs a new instance with the specified {@link NativeMptCrypto} bridge.
   *
   * @param nativeCrypto The native bridge to delegate to.
   */
  public JnaConfidentialMptSendProofGenerator(final NativeMptCrypto nativeCrypto) {
    this.nativeCrypto = Objects.requireNonNull(nativeCrypto);
  }

  @Override
  public ConfidentialMptSendProof generateProof(
    final KeyPair senderKeyPair,
    final UnsignedLong amount,
    final List<MptConfidentialParty> recipients,
    final BlindingFactor txBlindingFactor,
    final ConfidentialMptSendContext context,
    final PedersenProofParams amountParams,
    final PedersenProofParams balanceParams
  ) {
    Objects.requireNonNull(senderKeyPair, "senderKeyPair must not be null");
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(recipients, "recipients must not be null");
    Objects.requireNonNull(txBlindingFactor, "txBlindingFactor must not be null");
    Objects.requireNonNull(context, "context must not be null");
    Objects.requireNonNull(amountParams, "amountParams must not be null");
    Objects.requireNonNull(balanceParams, "balanceParams must not be null");

    Preconditions.checkArgument(
      senderKeyPair.publicKey().keyType() == KeyType.SECP256K1,
      "senderKeyPair must be SECP256K1"
    );
    Preconditions.checkArgument(!recipients.isEmpty(), "recipients must not be empty");

    int numRecipients = recipients.size();

    // Extract sender private key
    UnsignedByteArray naturalBytes = senderKeyPair.privateKey().naturalBytes();
    byte[] privkey = naturalBytes.toByteArray();

    // Flatten recipient pubkeys and ciphertexts into contiguous arrays
    byte[] recipientPubkeys = new byte[numRecipients * PUBKEY_SIZE];
    byte[] recipientCiphertexts = new byte[numRecipients * CIPHERTEXT_SIZE];
    for (int i = 0; i < numRecipients; i++) {
      MptConfidentialParty party = recipients.get(i);
      System.arraycopy(party.publicKey().value().toByteArray(), 0, recipientPubkeys, i * PUBKEY_SIZE, PUBKEY_SIZE);
      System.arraycopy(
        party.encryptedAmount().toBytes().toByteArray(), 0, recipientCiphertexts, i * CIPHERTEXT_SIZE, CIPHERTEXT_SIZE
      );
    }

    byte[] outProof = new byte[MAX_PROOF_SIZE];
    int[] outLen = new int[]{MAX_PROOF_SIZE};

    int result = nativeCrypto.generateSendProof(
      privkey, amount.longValue(),
      recipientPubkeys, recipientCiphertexts, numRecipients,
      txBlindingFactor.toBytes(), context.value().toByteArray(),
      amountParams.pedersenCommitment().toByteArray(), amountParams.amount().longValue(),
      amountParams.encryptedAmount().toBytes().toByteArray(), amountParams.blindingFactor().toBytes(),
      balanceParams.pedersenCommitment().toByteArray(), balanceParams.amount().longValue(),
      balanceParams.encryptedAmount().toBytes().toByteArray(), balanceParams.blindingFactor().toBytes(),
      outProof, outLen
    );

    naturalBytes.destroy();

    if (result != 0) {
      throw new IllegalStateException("mpt_get_confidential_send_proof failed with error code: " + result);
    }

    byte[] proof = new byte[outLen[0]];
    System.arraycopy(outProof, 0, proof, 0, outLen[0]);
    return ConfidentialMptSendProof.of(UnsignedByteArray.of(proof));
  }
}
