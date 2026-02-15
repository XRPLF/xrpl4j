package org.xrpl.xrpl4j.crypto.mpt.bulletproofs;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalCiphertext;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

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
   * Generates a proof that a ciphertext encrypts a known plaintext value.
   *
   * @param c1           The first component of the ElGamal ciphertext (r * G).
   * @param c2           The second component of the ElGamal ciphertext (m * G + r * P).
   * @param publicKey    The public key used for encryption.
   * @param amount       The unsigned plaintext amount.
   * @param randomness   The 32-byte randomness (r) used in encryption.
   * @param contextId    The 32-byte transaction context hash. Can be null.
   *
   * @return A 98-byte proof (T1 || T2 || s).
   */
  byte[] generateProof(
    ECPoint c1,
    ECPoint c2,
    ECPoint publicKey,
    UnsignedLong amount,
    byte[] randomness,
    byte[] contextId
  );

  /**
   * Verifies that a ciphertext encrypts a known plaintext value.
   *
   * @param proof      The 98-byte proof to verify.
   * @param c1         The first component of the ElGamal ciphertext.
   * @param c2         The second component of the ElGamal ciphertext.
   * @param publicKey  The public key used for encryption.
   * @param amount     The claimed unsigned plaintext amount.
   * @param contextId  The 32-byte transaction context hash. Can be null.
   *
   * @return {@code true} if the proof is valid, {@code false} otherwise.
   */
  boolean verifyProof(
    byte[] proof,
    ECPoint c1,
    ECPoint c2,
    ECPoint publicKey,
    UnsignedLong amount,
    byte[] contextId
  );

  /**
   * Generates the context hash for a ConfidentialMPTClawback transaction.
   *
   * <p>The context is computed as SHA512Half of:
   * txType (2 bytes) || account (20 bytes) || sequence (4 bytes) ||
   * issuanceId (24 bytes) || amount (8 bytes) || holder (20 bytes)</p>
   *
   * @param account     The issuer's account address.
   * @param sequence    The transaction sequence number.
   * @param issuanceId  The MPT issuance ID.
   * @param amount      The amount being clawed back.
   * @param holder      The holder account from which tokens are being clawed back.
   *
   * @return A 32-byte context hash.
   */
  byte[] generateClawbackContext(
    Address account,
    UnsignedInteger sequence,
    MpTokenIssuanceId issuanceId,
    UnsignedLong amount,
    Address holder
  );
}

