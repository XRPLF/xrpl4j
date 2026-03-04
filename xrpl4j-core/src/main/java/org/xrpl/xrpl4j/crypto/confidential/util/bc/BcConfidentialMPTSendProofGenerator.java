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
import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.codec.addresses.KeyType;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.RangeProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.bc.BcRangeProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.confidential.BlindingFactor;
import org.xrpl.xrpl4j.crypto.confidential.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMPTSendContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMPTSendProof;
import org.xrpl.xrpl4j.crypto.confidential.model.MPTConfidentialParty;
import org.xrpl.xrpl4j.crypto.confidential.model.PedersenProofParams;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.PedersenLinkProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.SamePlaintextProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.bc.BcPedersenLinkProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.bc.BcSamePlaintextProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.util.ConfidentialMPTSendProofGenerator;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * BouncyCastle implementation of {@link ConfidentialMPTSendProofGenerator}.
 *
 * <p>This implementation delegates to port implementations for the actual cryptographic operations.</p>
 */
public class BcConfidentialMPTSendProofGenerator implements ConfidentialMPTSendProofGenerator {

  private final SamePlaintextProofGenerator samePlaintextProofGenerator;
  private final PedersenLinkProofGenerator pedersenLinkProofGenerator;
  private final RangeProofGenerator rangeProofGenerator;

  /**
   * Creates a new instance with default port implementations.
   */
  public BcConfidentialMPTSendProofGenerator() {
    this(
      new BcSamePlaintextProofGenerator(),
      new BcPedersenLinkProofGenerator(),
      new BcRangeProofGenerator()
    );
  }

  /**
   * Creates a new instance with custom port implementations.
   *
   * @param samePlaintextProofGenerator The same-plaintext proof generator port.
   * @param pedersenLinkProofGenerator  The Pedersen link proof generator port.
   * @param rangeProofGenerator         The range proof generator port.
   */
  public BcConfidentialMPTSendProofGenerator(
    final SamePlaintextProofGenerator samePlaintextProofGenerator,
    final PedersenLinkProofGenerator pedersenLinkProofGenerator,
    final RangeProofGenerator rangeProofGenerator
  ) {
    this.samePlaintextProofGenerator = Objects.requireNonNull(samePlaintextProofGenerator);
    this.pedersenLinkProofGenerator = Objects.requireNonNull(pedersenLinkProofGenerator);
    this.rangeProofGenerator = Objects.requireNonNull(rangeProofGenerator);
  }

  @Override
  public ConfidentialMPTSendProof generateProof(
    final KeyPair senderKeyPair,
    final UnsignedLong amount,
    final List<MPTConfidentialParty> recipients,
    final BlindingFactor txBlindingFactor,
    final ConfidentialMPTSendContext context,
    final PedersenProofParams amountParams,
    final PedersenProofParams balanceParams
  ) {
    // Validate inputs
    Objects.requireNonNull(senderKeyPair, "senderKeyPair must not be null");
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(recipients, "recipients must not be null");
    Objects.requireNonNull(txBlindingFactor, "txBlindingFactor must not be null");
    Objects.requireNonNull(context, "context must not be null");
    Objects.requireNonNull(amountParams, "amountParams must not be null");
    Objects.requireNonNull(balanceParams, "balanceParams must not be null");

    Preconditions.checkArgument(
      senderKeyPair.publicKey().keyType() == KeyType.SECP256K1,
      "senderKeyPair must be SECP256K1, but was %s", senderKeyPair.publicKey().keyType()
    );
    Preconditions.checkArgument(!recipients.isEmpty(), "recipients must not be empty");

    int numRecipients = recipients.size();

    // Build lists for same-plaintext proof
    List<UnsignedByteArray> c1List = new java.util.ArrayList<>();
    List<UnsignedByteArray> c2List = new java.util.ArrayList<>();
    List<UnsignedByteArray> pkList = new java.util.ArrayList<>();
    List<UnsignedByteArray> blindingsList = new java.util.ArrayList<>();

    UnsignedByteArray txBlindingBytes = UnsignedByteArray.of(txBlindingFactor.toBytes());
    for (int i = 0; i < numRecipients; i++) {
      MPTConfidentialParty party = recipients.get(i);
      EncryptedAmount ciphertext = party.encryptedAmount();
      c1List.add(ciphertext.c1());
      c2List.add(ciphertext.c2());
      pkList.add(party.publicKey().value());
      blindingsList.add(txBlindingBytes);
    }

    // 1. Generate same-plaintext multi proof
    UnsignedByteArray samePlaintextProof = samePlaintextProofGenerator.generateProof(
      amount, c1List, c2List, pkList, blindingsList, context.value()
    );

    // 2. Generate amount linkage proof (uses the transaction blinding factor)
    MPTConfidentialParty senderParty = recipients.get(0);
    UnsignedByteArray amountLinkageProof = generateAmountLinkageProof(
      senderParty, txBlindingFactor, context, amountParams
    );

    // 3. Generate balance linkage proof
    UnsignedByteArray balanceLinkageProof = generateBalanceLinkageProof(
      senderKeyPair, senderParty, context, balanceParams
    );

    // 4. Generate aggregated bulletproof range proof
    UnsignedByteArray rangeProof = generateRangeProof(amount, amountParams, balanceParams, context);

    // Combine all proofs
    return combineProofs(samePlaintextProof, amountLinkageProof, balanceLinkageProof, rangeProof);
  }

  private UnsignedByteArray generateAmountLinkageProof(
    MPTConfidentialParty firstRecipient,
    BlindingFactor txBlindingFactor,
    ConfidentialMPTSendContext context,
    PedersenProofParams amountParams
  ) {
    EncryptedAmount ciphertext = amountParams.encryptedAmount();
    // Get compressed Pedersen commitment (first 33 bytes)
    byte[] pcmBytes = Arrays.copyOfRange(
      amountParams.pedersenCommitment().toByteArray(), 0, Secp256k1Operations.PUBKEY_SIZE
    );
    UnsignedByteArray pcm = UnsignedByteArray.of(pcmBytes);

    // Amount linkage: c1, c2, pk, pcm (matching C code order)
    return pedersenLinkProofGenerator.generateProof(
      ciphertext.c1(),
      ciphertext.c2(),
      firstRecipient.publicKey().value(),
      pcm,
      amountParams.amount(),
      UnsignedByteArray.of(txBlindingFactor.toBytes()),
      UnsignedByteArray.of(amountParams.blindingFactor().toBytes()),
      context.value()
    );
  }

  private UnsignedByteArray generateBalanceLinkageProof(
    KeyPair senderKeyPair,
    MPTConfidentialParty firstRecipient,
    ConfidentialMPTSendContext context,
    PedersenProofParams balanceParams
  ) {
    EncryptedAmount ciphertext = balanceParams.encryptedAmount();
    // Get compressed Pedersen commitment (first 33 bytes)
    byte[] pcmBytes = Arrays.copyOfRange(
      balanceParams.pedersenCommitment().toByteArray(), 0, Secp256k1Operations.PUBKEY_SIZE
    );
    UnsignedByteArray pcm = UnsignedByteArray.of(pcmBytes);

    // Balance linkage: pk, c2, c1, pcm (matching C code order - note swapped c1/c2)
    // Uses private key as blinding factor
    return pedersenLinkProofGenerator.generateProof(
      firstRecipient.publicKey().value(),
      ciphertext.c2(),
      ciphertext.c1(),
      pcm,
      balanceParams.amount(),
      senderKeyPair.privateKey().naturalBytes(),
      UnsignedByteArray.of(balanceParams.blindingFactor().toBytes()),
      context.value()
    );
  }

  private UnsignedByteArray generateRangeProof(
    UnsignedLong amount,
    PedersenProofParams amountParams,
    PedersenProofParams balanceParams,
    ConfidentialMPTSendContext context
  ) {
    // Values: [amount, remaining_balance]
    UnsignedLong remainingBalance = UnsignedLong.valueOf(
      balanceParams.amount().longValue() - amount.longValue()
    );
    UnsignedLong[] values = new UnsignedLong[] { amount, remainingBalance };

    // Blindings: [rho_amount, rho_balance - rho_amount]
    byte[] rhoAmount = amountParams.blindingFactor().toBytes();
    byte[] rhoBalance = balanceParams.blindingFactor().toBytes();
    byte[] negRhoAmount = Secp256k1Operations.scalarNegate(rhoAmount);
    byte[] rhoRem = Secp256k1Operations.scalarAdd(rhoBalance, negRhoAmount);

    byte[] blindingsFlat = new byte[64];
    System.arraycopy(rhoAmount, 0, blindingsFlat, 0, 32);
    System.arraycopy(rhoRem, 0, blindingsFlat, 32, 32);

    // pk_base is H generator
    UnsignedByteArray pkBase = UnsignedByteArray.of(
      Secp256k1Operations.serializeCompressed(Secp256k1Operations.getH())
    );

    return rangeProofGenerator.generateProof(
      values,
      UnsignedByteArray.of(blindingsFlat),
      pkBase,
      context.value()
    );
  }

  private ConfidentialMPTSendProof combineProofs(
    UnsignedByteArray samePlaintextProof,
    UnsignedByteArray amountLinkageProof,
    UnsignedByteArray balanceLinkageProof,
    UnsignedByteArray rangeProof
  ) {
    int totalSize = samePlaintextProof.length() + amountLinkageProof.length()
      + balanceLinkageProof.length() + rangeProof.length();

    byte[] combined = new byte[totalSize];
    int offset = 0;

    System.arraycopy(samePlaintextProof.toByteArray(), 0, combined, offset, samePlaintextProof.length());
    offset += samePlaintextProof.length();

    System.arraycopy(amountLinkageProof.toByteArray(), 0, combined, offset, amountLinkageProof.length());
    offset += amountLinkageProof.length();

    System.arraycopy(balanceLinkageProof.toByteArray(), 0, combined, offset, balanceLinkageProof.length());
    offset += balanceLinkageProof.length();

    System.arraycopy(rangeProof.toByteArray(), 0, combined, offset, rangeProof.length());

    return ConfidentialMPTSendProof.of(UnsignedByteArray.of(combined));
  }
}

