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
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.bc.BcRangeProofGenerator;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.confidential.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMPTConvertBackContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMPTConvertBackProof;
import org.xrpl.xrpl4j.crypto.confidential.model.PedersenProofParams;
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.PedersenLinkProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.RangeProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.bc.BcPedersenLinkProofGenerator;
import org.xrpl.xrpl4j.crypto.confidential.util.ConfidentialMPTConvertBackProofGenerator;

import java.util.Arrays;
import java.util.Objects;

/**
 * BouncyCastle implementation of {@link ConfidentialMPTConvertBackProofGenerator}.
 *
 * <p>This implementation mirrors the C function {@code mpt_get_convert_back_proof} from mpt_utility.h.</p>
 */
public class BcConfidentialMPTConvertBackProofGenerator implements ConfidentialMPTConvertBackProofGenerator {

  private final PedersenLinkProofGenerator pedersenLinkProofGenerator;
  private final RangeProofGenerator rangeProofGenerator;

  /**
   * Creates a new instance with default port implementations.
   */
  public BcConfidentialMPTConvertBackProofGenerator() {
    this(new BcPedersenLinkProofGenerator(), new BcRangeProofGenerator());
  }

  /**
   * Creates a new instance with custom port implementations.
   *
   * @param pedersenLinkProofGenerator The Pedersen link proof generator port.
   * @param rangeProofGenerator        The range proof generator port.
   */
  public BcConfidentialMPTConvertBackProofGenerator(
    final PedersenLinkProofGenerator pedersenLinkProofGenerator,
    final RangeProofGenerator rangeProofGenerator
  ) {
    this.pedersenLinkProofGenerator = Objects.requireNonNull(pedersenLinkProofGenerator);
    this.rangeProofGenerator = Objects.requireNonNull(rangeProofGenerator);
  }

  @Override
  public ConfidentialMPTConvertBackProof generateProof(
    final KeyPair senderKeyPair,
    final UnsignedLong amount,
    final ConfidentialMPTConvertBackContext context,
    final PedersenProofParams balanceParams
  ) {
    // Validate inputs
    Objects.requireNonNull(senderKeyPair, "senderKeyPair must not be null");
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(context, "context must not be null");
    Objects.requireNonNull(balanceParams, "balanceParams must not be null");

    Preconditions.checkArgument(
      senderKeyPair.publicKey().keyType() == KeyType.SECP256K1,
      "senderKeyPair must be SECP256K1, but was %s", senderKeyPair.publicKey().keyType()
    );

    // 1. Generate balance linkage proof
    UnsignedByteArray balanceLinkageProof = generateBalanceLinkageProof(
      senderKeyPair, context, balanceParams
    );

    // 2. Generate single bulletproof range proof for remaining balance
    UnsignedByteArray rangeProof = generateRangeProof(amount, balanceParams, context);

    // 3. Combine proofs
    return combineProofs(balanceLinkageProof, rangeProof);
  }

  private UnsignedByteArray generateBalanceLinkageProof(
    final KeyPair senderKeyPair,
    final ConfidentialMPTConvertBackContext context,
    final PedersenProofParams balanceParams
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
      senderKeyPair.publicKey().value(),
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
    final UnsignedLong amount,
    final PedersenProofParams balanceParams,
    final ConfidentialMPTConvertBackContext context
  ) {
    // Remaining balance = current balance - amount
    UnsignedLong remainingBalance = UnsignedLong.valueOf(
      balanceParams.amount().longValue() - amount.longValue()
    );

    // Single value range proof uses the same blinding factor as the balance commitment
    UnsignedLong[] values = new UnsignedLong[] { remainingBalance };
    byte[] blindingsFlat = balanceParams.blindingFactor().toBytes();

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

  private ConfidentialMPTConvertBackProof combineProofs(
    final UnsignedByteArray balanceLinkageProof,
    final UnsignedByteArray rangeProof
  ) {
    int totalSize = balanceLinkageProof.length() + rangeProof.length();
    byte[] combined = new byte[totalSize];

    System.arraycopy(balanceLinkageProof.toByteArray(), 0, combined, 0, balanceLinkageProof.length());
    System.arraycopy(rangeProof.toByteArray(), 0, combined, balanceLinkageProof.length(), rangeProof.length());

    return ConfidentialMPTConvertBackProof.of(UnsignedByteArray.of(combined));
  }
}

