package org.xrpl.xrpl4j.crypto.mpt.bulletproofs;

import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.crypto.mpt.BlindingFactor;
import org.xrpl.xrpl4j.crypto.mpt.context.ConfidentialMPTSendContext;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.SamePlaintextMultiProof;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.SamePlaintextParticipant;

import java.util.Optional;

/**
 * Interface for generating and verifying Zero-Knowledge Proofs of Plaintext Equality (1-to-N).
 *
 * <p>This proves that N distinct ElGamal ciphertexts all encrypt the same underlying
 * plaintext amount m, using distinct randomness r_i for each.</p>
 *
 * <p><b>Statement:</b> Given N ciphertexts (R_i, S_i) encrypted under public keys P_i,
 * the prover demonstrates knowledge of scalars m and {r_1, ..., r_N} such that for all i:
 * <ul>
 *   <li>R_i = r_i * G</li>
 *   <li>S_i = m * G + r_i * P_i</li>
 * </ul>
 *
 * <p><b>Protocol (Shared Amount Nonce):</b>
 * <ol>
 *   <li><b>Commitments:</b>
 *     <ul>
 *       <li>T_m = k_m * G (Shared commitment to amount nonce)</li>
 *       <li>For each i: T_{r,G}^{(i)} = k_{r,i} * G</li>
 *       <li>For each i: T_{r,P}^{(i)} = k_{r,i} * P_i</li>
 *     </ul>
 *   </li>
 *   <li><b>Challenge:</b> e = H(... || T_m || {T_{r,G}^{(i)}, T_{r,P}^{(i)}} || ...)</li>
 *   <li><b>Responses:</b>
 *     <ul>
 *       <li>s_m = k_m + e * m (Shared response for amount)</li>
 *       <li>For each i: s_{r,i} = k_{r,i} + e * r_i</li>
 *     </ul>
 *   </li>
 *   <li><b>Verification:</b> For each i:
 *     <ul>
 *       <li>s_{r,i} * G == T_{r,G}^{(i)} + e * R_i</li>
 *       <li>s_m * G + s_{r,i} * P_i == T_m + T_{r,P}^{(i)} + e * S_i</li>
 *     </ul>
 *   </li>
 * </ol>
 *
 * <p><b>Proof format (serialized):</b>
 * <ul>
 *   <li>Tm (33 bytes) - Shared commitment to amount nonce</li>
 *   <li>TrG[0..N-1] (N * 33 bytes) - Commitments k_{r,i} * G</li>
 *   <li>TrP[0..N-1] (N * 33 bytes) - Commitments k_{r,i} * P_i</li>
 *   <li>sm (32 bytes) - Shared response for amount</li>
 *   <li>sr[0..N-1] (N * 32 bytes) - Responses for randomness</li>
 * </ul>
 * Total size: (1 + 2N) * 33 + (1 + N) * 32 bytes</p>
 *
 * @see <a href="ConfidentialMPT_20260106.pdf">Spec Section 3.3.4</a>
 */
public interface SamePlaintextMultiProofGenerator {

  /**
   * Generates a proof that ciphertexts for sender, destination, issuer (and optionally auditor) all encrypt the same
   * plaintext amount.
   *
   * @param amount      The plaintext amount (witness).
   * @param sender      The sender participant (ciphertext, publicKey, blindingFactor, nonceKr).
   * @param destination The destination participant.
   * @param issuer      The issuer participant.
   * @param auditor     Optional auditor participant.
   * @param context     The context hash for domain separation.
   * @param nonceKm     The nonce for the amount commitment (k_m).
   *
   * @return A {@link SamePlaintextMultiProof}.
   */
  SamePlaintextMultiProof generateProof(
    UnsignedLong amount,
    SamePlaintextParticipant sender,
    SamePlaintextParticipant destination,
    SamePlaintextParticipant issuer,
    Optional<SamePlaintextParticipant> auditor,
    ConfidentialMPTSendContext context,
    BlindingFactor nonceKm
  );

  /**
   * Verifies that ciphertexts for sender, destination, issuer (and optionally auditor) all encrypt the same plaintext
   * amount.
   *
   * @param proof       The proof to verify.
   * @param sender      The sender participant (ciphertext and publicKey required).
   * @param destination The destination participant.
   * @param issuer      The issuer participant.
   * @param auditor     Optional auditor participant.
   * @param context     The context hash for domain separation.
   *
   * @return {@code true} if the proof is valid, {@code false} otherwise.
   */
  boolean verify(
    SamePlaintextMultiProof proof,
    SamePlaintextParticipant sender,
    SamePlaintextParticipant destination,
    SamePlaintextParticipant issuer,
    Optional<SamePlaintextParticipant> auditor,
    ConfidentialMPTSendContext context
  );

  /**
   * Computes the proof size for N participants.
   *
   * <p>Format: (1 Tm + 2N Tr) * 33 + (1 sm + N sr) * 32</p>
   *
   * @param n The number of participants.
   *
   * @return The proof size in bytes.
   */
  static int proofSize(int n) {
    return ((1 + 2 * n) * 33) + ((1 + n) * 32);
  }
}

