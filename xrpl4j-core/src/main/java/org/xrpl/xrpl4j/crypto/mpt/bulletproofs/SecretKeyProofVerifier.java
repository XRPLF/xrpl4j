package org.xrpl.xrpl4j.crypto.mpt.bulletproofs;

import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.mpt.context.ConfidentialMPTConvertContext;
import org.xrpl.xrpl4j.crypto.mpt.models.SecretKeyProof;

/**
 * Interface for generating Schnorr Proof of Knowledge (PoK) for ElGamal secret keys.
 *
 * <p>This proves knowledge of the private key corresponding to an ElGamal public key
 * without revealing the private key itself. The proof is used in confidential MPT transactions to prove ownership of
 * the encryption key.</p>
 *
 * <p>The proof format is: T (33 bytes compressed point) || s (32 bytes scalar) = 65 bytes total.</p>
 *
 * <p>Algorithm (Schnorr Identification Scheme with Fiat-Shamir transform):
 * <ol>
 *   <li><b>Commitment:</b> Sample random nonce k and compute T = k * G</li>
 *   <li><b>Challenge (Fiat-Shamir):</b> e = reduce(SHA256("MPT_POK_SK_REGISTER" || P || T || contextId)) mod n</li>
 *   <li><b>Response:</b> s = k + e * sk (mod n)</li>
 *   <li><b>Proof:</b> Return T || s (65 bytes)</li>
 * </ol>
 * </p>
 *
 * <p>Verification checks: s * G == T + e * P</p>
 *
 * @see PrivateKey
 * @see PublicKey
 * @see ConfidentialMPTConvertContext
 * @see SecretKeyProof
 */
public interface SecretKeyProofVerifier {

  /**
   * Verifies a Schnorr Proof of Knowledge.
   *
   * <p>Verification equation: s * G == T + e * P</p>
   *
   * @param proof     The proof to verify.
   * @param publicKey The ElGamal public key.
   * @param context   The context for domain separation. Can be null for no context.
   *
   * @return {@code true} if the proof is valid, {@code false} otherwise.
   *
   * @throws NullPointerException if proof or publicKey is null.
   */
  boolean verifyProof(SecretKeyProof proof, PublicKey publicKey, ConfidentialMPTConvertContext context);
}

