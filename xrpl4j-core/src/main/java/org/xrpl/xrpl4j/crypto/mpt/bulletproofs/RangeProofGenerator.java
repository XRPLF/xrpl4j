package org.xrpl.xrpl4j.crypto.mpt.bulletproofs;

import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;

/**
 * Interface for generating and verifying zero-knowledge range proofs.
 *
 * <p>Range proofs are used in confidential MPT transfers to prove that:</p>
 * <ul>
 *   <li>A hidden transfer amount is non-negative (≥ 0)</li>
 *   <li>A hidden transfer amount does not exceed the sender's balance</li>
 *   <li>A hidden value is within a valid range [0, 2^64)</li>
 * </ul>
 *
 * <p>The implementation uses Bulletproofs, which provide efficient range proofs
 * with logarithmic proof size.</p>
 */
public interface RangeProofGenerator {

  /**
   * Generates a range proof for a given value.
   *
   * <p>The proof demonstrates that the value is in the range [0, 2^64) without
   * revealing the actual value.</p>
   *
   * @param value          The unsigned value to prove is in range.
   * @param blindingFactor The blinding factor used in the commitment.
   * @param publicKey      The public key (used as the dynamic H generator).
   * @return A {@link RangeProof} that can be verified.
   * @throws IllegalArgumentException if the value is zero.
   */
  RangeProof generateProof(UnsignedLong value, byte[] blindingFactor, ECPoint publicKey);

  /**
   * Verifies a range proof against a commitment.
   *
   * @param proof      The range proof to verify.
   * @param commitment The Pedersen commitment (V = value*G + blinding*H).
   * @param publicKey  The public key used as the dynamic H generator.
   * @return {@code true} if the proof is valid, {@code false} otherwise.
   */
  boolean verify(RangeProof proof, ECPoint commitment, ECPoint publicKey);

  /**
   * Generates a balance sufficiency proof.
   *
   * <p>This proves that (balance - amount) ≥ 0 without revealing either value.</p>
   *
   * @param balance              The current balance.
   * @param amount               The amount to transfer.
   * @param balanceBlindingFactor The blinding factor for the balance commitment.
   * @param amountBlindingFactor  The blinding factor for the amount commitment.
   * @param publicKey            The public key.
   * @return A {@link RangeProof} proving balance ≥ amount.
   * @throws IllegalArgumentException if balance &lt; amount.
   */
  RangeProof generateBalanceSufficiencyProof(
      UnsignedLong balance,
      UnsignedLong amount,
      byte[] balanceBlindingFactor,
      byte[] amountBlindingFactor,
      ECPoint publicKey
  );
}
