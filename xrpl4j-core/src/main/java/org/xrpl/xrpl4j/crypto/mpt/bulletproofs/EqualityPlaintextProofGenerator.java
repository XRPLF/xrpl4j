package org.xrpl.xrpl4j.crypto.mpt.bulletproofs;

import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.crypto.mpt.BlindingFactor;
import org.xrpl.xrpl4j.crypto.mpt.context.ConfidentialMPTClawbackContext;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalCiphertext;
import org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalPublicKey;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.EqualityPlaintextProof;

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
public interface EqualityPlaintextProofGenerator {

  /**
   * The length of the proof in bytes: T1 (33) + T2 (33) + s (32) = 98 bytes.
   */
  int PROOF_LENGTH = 98;

  /**
   * Generates a proof that a ciphertext encrypts a known plaintext value for clawback.
   *
   * <p>For clawback transactions, the issuer proves they know the plaintext encrypted in the
   * holder's IssuerEncryptedBalance ciphertext using their private key.</p>
   *
   * <p>The implementation internally handles the parameter swapping required by rippled's
   * clawback proof generation. Rippled calls the C function with swapped parameters:
   * {@code secp256k1_equality_plaintext_prove(ctx, proof, &pk, &c2, &c1, amount, privateKey, contextHash)}
   * which maps to: c1 ← issuer's pk, c2 ← balance.c2, pk_recipient ← balance.c1.</p>
   *
   * <p>Callers should simply pass the actual IssuerEncryptedBalance ciphertext and issuer's
   * public key - the swapping is done internally.</p>
   *
   * @param ciphertext The IssuerEncryptedBalance ciphertext from the MPToken.
   * @param publicKey  The issuer's ElGamal public key.
   * @param amount     The unsigned plaintext amount to clawback.
   * @param randomness The issuer's ElGamal private key (used as "randomness" in the proof).
   * @param nonceT     The random nonce for the commitment.
   * @param context    The transaction context hash for clawback.
   *
   * @return An {@link EqualityPlaintextProof} (98 bytes: T1 || T2 || s).
   *
   * @throws NullPointerException if any required parameter is null.
   */
  EqualityPlaintextProof generateProof(
    ElGamalCiphertext ciphertext,
    ElGamalPublicKey publicKey,
    UnsignedLong amount,
    BlindingFactor randomness,
    BlindingFactor nonceT,
    ConfidentialMPTClawbackContext context
  );

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
    ElGamalPublicKey publicKey,
    UnsignedLong amount,
    ConfidentialMPTClawbackContext context
  );
}
