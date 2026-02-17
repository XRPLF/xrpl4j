package org.xrpl.xrpl4j.crypto.mpt.bulletproofs;

import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.crypto.mpt.BlindingFactor;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.PedersenCommitment;

/**
 * Interface for generating Pedersen Commitments of the form C = v * G + rho * H.
 *
 * <p>Implementations use a "Nothing-Up-My-Sleeve" (NUMS) construction for the H generator,
 * ensuring that the discrete logarithm of H with respect to G is unknown. This is critical for the binding property of
 * the commitments.</p>
 *
 * <p>The NUMS generators are derived deterministically using SHA-256 hash-to-curve with
 * the domain separation tag "MPT_BULLETPROOF_V1_NUMS".</p>
 *
 * @see <a href="ConfidentialMPT_20260201.pdf">Spec Section 3.3.5</a>
 */
public interface PedersenCommitmentGenerator {

  /**
   * Creates a Pedersen Commitment: C = amount * G + rho * H.
   *
   * <p>Handles the edge case where amount = 0, in which case C = rho * H.</p>
   *
   * @param amount The value to commit to (64-bit unsigned).
   * @param rho    The blinding factor (must be a valid scalar).
   *
   * @return The commitment as a {@link PedersenCommitment}.
   */
  PedersenCommitment generateCommitment(UnsignedLong amount, BlindingFactor rho);
}

