package org.xrpl.xrpl4j.crypto.mpt.bulletproofs;

import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.mpt.context.ConfidentialMPTClawbackContext;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalCiphertext;
import org.xrpl.xrpl4j.crypto.mpt.tmp.EqualityPlaintextProof;

/**
 * Interface for generating and verifying Zero-Knowledge Proofs of Knowledge of Plaintext and Randomness.
 *
 * <p>This implements a Sigma protocol (Chaum-Pedersen style) to prove that an ElGamal ciphertext
 * (C1, C2) encrypts a specific known plaintext m under a public key P, and that the prover knows
 * the randomness r used in the encryption.</p>
 *
 * <p><b>Statement:</b> The prover demonstrates knowledge of r ∈ Z_q such that:
 * <ul>
 *   <li>C1 = r * G</li>
 *   <li>C2 = m * G + r * P</li>
 * </ul>
 * </p>
 *
 * <p><b>Protocol:</b>
 * <ol>
 *   <li><b>Commitment:</b> Prover samples t ← Z_q and computes T1 = t * G, T2 = t * P</li>
 *   <li><b>Challenge:</b> e = H("MPT_POK_PLAINTEXT_PROOF" || C1 || C2 || P || [mG] || T1 || T2 || context)</li>
 *   <li><b>Response:</b> s = t + e * r (mod q)</li>
 *   <li><b>Verification:</b> s * G == T1 + e * C1 and s * P == T2 + e * (C2 - m * G)</li>
 * </ol>
 * </p>
 *
 * <p>This proof is used in:
 * <ul>
 *   <li>ConfidentialMPTConvert - explicit randomness verification</li>
 *   <li>ConfidentialMPTClawback - issuer proves the ciphertext matches a revealed amount using their secret key</li>
 * </ul>
 * </p>
 */
public interface PlaintextEqualityProofVerifier {
  /**
   * Verifies that a ciphertext encrypts a known plaintext value.
   *
   * @param proof      The proof to verify.
   * @param ciphertext The ElGamal ciphertext.
   * @param publicKey  The public key used for encryption.
   * @param amount     The claimed unsigned plaintext amount.
   * @param context    The transaction context hash.
   *
   * @return {@code true} if the proof is valid, {@code false} otherwise.
   *
   * @throws NullPointerException if any required parameter is null.
   */
  boolean verifyProof(
    EqualityPlaintextProof proof,
    ElGamalCiphertext ciphertext,
    PublicKey publicKey,
    UnsignedLong amount,
    ConfidentialMPTClawbackContext context
  );
}
