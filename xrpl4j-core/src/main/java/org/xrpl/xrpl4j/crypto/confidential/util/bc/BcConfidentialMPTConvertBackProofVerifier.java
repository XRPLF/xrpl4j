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

import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.confidential.model.EncryptedAmount;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.confidential.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.confidential.model.context.ConfidentialMPTConvertBackContext;
import org.xrpl.xrpl4j.crypto.confidential.model.proof.ConfidentialMPTConvertBackProof;
import org.xrpl.xrpl4j.crypto.confidential.model.PedersenCommitment;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.PedersenLinkProofVerifier;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.RangeProofVerifier;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.bc.BcPedersenLinkProofVerifier;
import org.xrpl.xrpl4j.crypto.confidential.bulletproof.bc.BcRangeProofVerifier;
import org.xrpl.xrpl4j.crypto.confidential.util.ConfidentialMPTConvertBackProofVerifier;

import java.util.Arrays;
import java.util.Objects;

/**
 * BouncyCastle implementation of {@link ConfidentialMPTConvertBackProofVerifier}.
 */
public class BcConfidentialMPTConvertBackProofVerifier implements ConfidentialMPTConvertBackProofVerifier {

  private static final int SINGLE_BULLETPROOF_SIZE = 688;

  private final PedersenLinkProofVerifier pedersenLinkProofVerifier;
  private final RangeProofVerifier rangeProofVerifier;

  /**
   * Creates a new instance with default port implementations.
   */
  public BcConfidentialMPTConvertBackProofVerifier() {
    this(new BcPedersenLinkProofVerifier(), new BcRangeProofVerifier());
  }

  /**
   * Creates a new instance with custom port implementations.
   *
   * @param pedersenLinkProofVerifier The Pedersen link proof verifier port.
   * @param rangeProofVerifier        The range proof verifier port.
   */
  public BcConfidentialMPTConvertBackProofVerifier(
    final PedersenLinkProofVerifier pedersenLinkProofVerifier,
    final RangeProofVerifier rangeProofVerifier
  ) {
    this.pedersenLinkProofVerifier = Objects.requireNonNull(pedersenLinkProofVerifier);
    this.rangeProofVerifier = Objects.requireNonNull(rangeProofVerifier);
  }

  @Override
  public boolean verifyProof(
    final ConfidentialMPTConvertBackProof proof,
    final PublicKey senderPublicKey,
    final EncryptedAmount encryptedBalance,
    final PedersenCommitment balanceCommitment,
    final UnsignedLong amount,
    final ConfidentialMPTConvertBackContext context
  ) {
    Objects.requireNonNull(proof, "proof must not be null");
    Objects.requireNonNull(senderPublicKey, "senderPublicKey must not be null");
    Objects.requireNonNull(encryptedBalance, "encryptedBalance must not be null");
    Objects.requireNonNull(balanceCommitment, "balanceCommitment must not be null");
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(context, "context must not be null");

    byte[] proofBytes = proof.value().toByteArray();

    // Extract balance linkage proof (first 195 bytes)
    byte[] balanceLinkageProof = Arrays.copyOfRange(proofBytes, 0, Secp256k1Operations.PEDERSEN_LINK_SIZE);

    // Extract range proof (remaining 688 bytes)
    byte[] rangeProof = Arrays.copyOfRange(
      proofBytes, Secp256k1Operations.PEDERSEN_LINK_SIZE, proofBytes.length
    );

    // 1. Verify balance linkage proof
    if (!verifyBalanceLinkageProof(balanceLinkageProof, senderPublicKey, encryptedBalance, balanceCommitment, context)) {
      return false;
    }

    // 2. Verify range proof
    return verifyRangeProof(rangeProof, balanceCommitment, amount, context);
  }

  private boolean verifyBalanceLinkageProof(
    final byte[] proof,
    final PublicKey senderPublicKey,
    final EncryptedAmount encryptedBalance,
    final PedersenCommitment balanceCommitment,
    final ConfidentialMPTConvertBackContext context
  ) {
    // Balance linkage: pk, c2, c1, pcm (matching C code order - note swapped c1/c2)
    return pedersenLinkProofVerifier.verifyProof(
      UnsignedByteArray.of(proof),
      senderPublicKey.value(),
      encryptedBalance.c2(),
      encryptedBalance.c1(),
      balanceCommitment.value(),
      context.value()
    );
  }

  private boolean verifyRangeProof(
    final byte[] proof,
    final PedersenCommitment balanceCommitment,
    final UnsignedLong amount,
    final ConfidentialMPTConvertBackContext context
  ) {
    // Compute remaining balance commitment: C_rem = C_bal - amount * G
    // This is because the range proof proves the remaining balance (balance - amount) is in range
    org.bouncycastle.math.ec.ECPoint balPoint = Secp256k1Operations.deserialize(balanceCommitment.value().toByteArray());
    org.bouncycastle.math.ec.ECPoint amountTimesG = Secp256k1Operations.getG().multiply(
      new java.math.BigInteger(1, com.google.common.primitives.Longs.toByteArray(amount.longValue()))
    );
    org.bouncycastle.math.ec.ECPoint remPoint = balPoint.add(amountTimesG.negate());

    UnsignedByteArray[] commitmentVec = new UnsignedByteArray[] {
      UnsignedByteArray.of(Secp256k1Operations.serializeCompressed(remPoint))
    };

    // Generator vectors for single value (n = 64 bits * 1 value = 64)
    int n = 64;
    UnsignedByteArray[] gVec = new UnsignedByteArray[n];
    UnsignedByteArray[] hVec = new UnsignedByteArray[n];

    org.bouncycastle.math.ec.ECPoint[] gPoints = Secp256k1Operations.getGeneratorVector("G", n);
    org.bouncycastle.math.ec.ECPoint[] hPoints = Secp256k1Operations.getGeneratorVector("H", n);

    for (int i = 0; i < n; i++) {
      gVec[i] = UnsignedByteArray.of(Secp256k1Operations.serializeCompressed(gPoints[i]));
      hVec[i] = UnsignedByteArray.of(Secp256k1Operations.serializeCompressed(hPoints[i]));
    }

    UnsignedByteArray pkBase = UnsignedByteArray.of(
      Secp256k1Operations.serializeCompressed(Secp256k1Operations.getH())
    );

    return rangeProofVerifier.verifyProof(
      gVec, hVec, UnsignedByteArray.of(proof), commitmentVec, pkBase, context.value()
    );
  }
}

