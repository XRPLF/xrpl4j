package org.xrpl.xrpl4j.crypto.confidential.util.bc;

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
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.confidential.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.PedersenLinkProofVerifier;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.RangeProofVerifier;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.SamePlaintextProofVerifier;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.bc.BcPedersenLinkProofVerifier;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.bc.BcRangeProofVerifier;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.bc.BcSamePlaintextProofVerifier;
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.confidential.model.MptConfidentialParty;
import org.xrpl.xrpl4j.crypto.confidential.model.PedersenCommitment;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMptSendContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMptSendProof;
import org.xrpl.xrpl4j.crypto.confidential.util.ConfidentialMptSendProofVerifier;

import java.util.List;
import java.util.Objects;

/**
 * BouncyCastle implementation of {@link ConfidentialMptSendProofVerifier}.
 *
 * <p>This implementation delegates to port implementations for the actual cryptographic operations.</p>
 */
public class BcConfidentialMptSendProofVerifier implements ConfidentialMptSendProofVerifier {

  private final SamePlaintextProofVerifier samePlaintextProofVerifier;
  private final PedersenLinkProofVerifier pedersenLinkProofVerifier;
  private final RangeProofVerifier rangeProofVerifier;

  /**
   * Creates a new instance with default port implementations.
   */
  public BcConfidentialMptSendProofVerifier() {
    this(
      new BcSamePlaintextProofVerifier(),
      new BcPedersenLinkProofVerifier(),
      new BcRangeProofVerifier()
    );
  }

  /**
   * Creates a new instance with custom port implementations.
   *
   * @param samePlaintextProofVerifier The same-plaintext proof verifier port.
   * @param pedersenLinkProofVerifier  The Pedersen link proof verifier port.
   * @param rangeProofVerifier         The range proof verifier port.
   */
  public BcConfidentialMptSendProofVerifier(
    final SamePlaintextProofVerifier samePlaintextProofVerifier,
    final PedersenLinkProofVerifier pedersenLinkProofVerifier,
    final RangeProofVerifier rangeProofVerifier
  ) {
    this.samePlaintextProofVerifier = Objects.requireNonNull(samePlaintextProofVerifier);
    this.pedersenLinkProofVerifier = Objects.requireNonNull(pedersenLinkProofVerifier);
    this.rangeProofVerifier = Objects.requireNonNull(rangeProofVerifier);
  }

  @Override
  public boolean verifyProof(
    final ConfidentialMptSendProof proof,
    final List<MptConfidentialParty> recipients,
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

    try {
      int numRecipients = recipients.size();
      byte[] proofBytes = proof.value().toByteArray();

      // Calculate expected sizes
      int samePlaintextSize = Secp256k1Operations.samePlaintextMultiProofSize(numRecipients);
      int linkageSize = Secp256k1Operations.PEDERSEN_LINK_SIZE;
      int rangeProofSize = ConfidentialMptSendProof.DOUBLE_BULLETPROOF_SIZE;
      int expectedSize = samePlaintextSize + (2 * linkageSize) + rangeProofSize;

      if (proofBytes.length != expectedSize) {
        return false;
      }

      // Extract proof components
      int offset = 0;
      byte[] samePlaintextProof = new byte[samePlaintextSize];
      System.arraycopy(proofBytes, offset, samePlaintextProof, 0, samePlaintextSize);
      offset += samePlaintextSize;

      byte[] amountLinkageProof = new byte[linkageSize];
      System.arraycopy(proofBytes, offset, amountLinkageProof, 0, linkageSize);
      offset += linkageSize;

      byte[] balanceLinkageProof = new byte[linkageSize];
      System.arraycopy(proofBytes, offset, balanceLinkageProof, 0, linkageSize);
      offset += linkageSize;

      byte[] rangeProof = new byte[rangeProofSize];
      System.arraycopy(proofBytes, offset, rangeProof, 0, rangeProofSize);

      // 1. Verify same-plaintext proof
      if (!verifySamePlaintextProof(samePlaintextProof, recipients, context)) {
        return false;
      }

      // 2. Verify amount linkage proof
      if (!verifyAmountLinkageProof(amountLinkageProof, recipients.get(0), amountCommitment, context)) {
        return false;
      }

      // 3. Verify balance linkage proof
      if (!verifyBalanceLinkageProof(balanceLinkageProof, recipients.get(0), balanceCommitment, context)) {
        return false;
      }

      // 4. Verify range proof
      return verifyRangeProof(rangeProof, amountCommitment, balanceCommitment, context);

    } catch (Exception e) {
      return false;
    }
  }

  private boolean verifySamePlaintextProof(
    byte[] proof,
    List<MptConfidentialParty> recipients,
    ConfidentialMptSendContext context
  ) {
    int recipientCount = recipients.size();
    // All recipients share the same C1 since they use the same blinding factor r
    UnsignedByteArray c1 = recipients.get(0).encryptedAmount().c1();
    List<UnsignedByteArray> c2List = new java.util.ArrayList<>();
    List<UnsignedByteArray> pkList = new java.util.ArrayList<>();

    for (int i = 0; i < recipientCount; i++) {
      MptConfidentialParty party = recipients.get(i);
      EncryptedAmount ciphertext = party.encryptedAmount();
      c2List.add(ciphertext.c2());
      pkList.add(party.publicKey().value());
    }

    return samePlaintextProofVerifier.verifyProof(
      UnsignedByteArray.of(proof), c1, c2List, pkList, context.value()
    );
  }

  private boolean verifyAmountLinkageProof(
    byte[] proof,
    MptConfidentialParty firstRecipient,
    PedersenCommitment amountCommitment,
    ConfidentialMptSendContext context
  ) {
    EncryptedAmount ciphertext = firstRecipient.encryptedAmount();
    UnsignedByteArray pcm = amountCommitment.value();

    // Amount linkage: c1, c2, pk, pcm (matching C code order)
    return pedersenLinkProofVerifier.verifyProof(
      UnsignedByteArray.of(proof),
      ciphertext.c1(),
      ciphertext.c2(),
      firstRecipient.publicKey().value(),
      pcm,
      context.value()
    );
  }

  private boolean verifyBalanceLinkageProof(
    byte[] proof,
    MptConfidentialParty firstRecipient,
    PedersenCommitment balanceCommitment,
    ConfidentialMptSendContext context
  ) {
    EncryptedAmount ciphertext = firstRecipient.encryptedAmount();
    UnsignedByteArray pcm = balanceCommitment.value();

    // Balance linkage: pk, c2, c1, pcm (matching C code order - note swapped c1/c2)
    return pedersenLinkProofVerifier.verifyProof(
      UnsignedByteArray.of(proof),
      firstRecipient.publicKey().value(),
      ciphertext.c2(),
      ciphertext.c1(),
      pcm,
      context.value()
    );
  }

  private boolean verifyRangeProof(
    byte[] proof,
    PedersenCommitment amountCommitment,
    PedersenCommitment balanceCommitment,
    ConfidentialMptSendContext context
  ) {
    // For verification, we need the commitments
    // The range proof verifies that the committed values are in range [0, 2^64-1]
    // Commitments: [amount_commitment, remaining_balance_commitment]
    // remaining_balance_commitment = balance_commitment - amount_commitment

    // Compute remaining balance commitment: C_rem = C_bal - C_amt
    ECPoint amtPoint =
      Secp256k1Operations.deserialize(amountCommitment.value().toByteArray());
    ECPoint balPoint =
      Secp256k1Operations.deserialize(balanceCommitment.value().toByteArray());
    ECPoint remPoint = balPoint.add(amtPoint.negate());

    UnsignedByteArray[] commitmentVec = new UnsignedByteArray[] {
      amountCommitment.value(),
      UnsignedByteArray.of(Secp256k1Operations.serializeCompressed(remPoint))
    };

    // Generator vectors
    int vectorLength = 128; // 64 bits * 2 values
    UnsignedByteArray[] generatorVecG = new UnsignedByteArray[vectorLength];
    UnsignedByteArray[] generatorVecH = new UnsignedByteArray[vectorLength];

    ECPoint[] genPointsG = Secp256k1Operations.getGeneratorVector("G", vectorLength);
    ECPoint[] genPointsH = Secp256k1Operations.getGeneratorVector("H", vectorLength);

    for (int i = 0; i < vectorLength; i++) {
      generatorVecG[i] = UnsignedByteArray.of(Secp256k1Operations.serializeCompressed(genPointsG[i]));
      generatorVecH[i] = UnsignedByteArray.of(Secp256k1Operations.serializeCompressed(genPointsH[i]));
    }

    UnsignedByteArray pkBase = UnsignedByteArray.of(
      Secp256k1Operations.serializeCompressed(Secp256k1Operations.getH())
    );

    return rangeProofVerifier.verifyProof(
      generatorVecG, generatorVecH, UnsignedByteArray.of(proof), commitmentVec, pkBase, context.value()
    );
  }
}

