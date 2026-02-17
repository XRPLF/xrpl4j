package org.xrpl.xrpl4j.crypto.mpt.bulletproofs;

import org.xrpl.xrpl4j.crypto.mpt.BlindingFactor;
import org.xrpl.xrpl4j.crypto.mpt.context.ConfidentialMPTConvertContext;
import org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalPrivateKeyable;
import org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalPublicKey;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.SecretKeyProof;

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
 * @param <P> The type of private key this generator accepts, must extend {@link ElGamalPrivateKeyable}.
 *
 * @see ElGamalPrivateKeyable
 * @see org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalPrivateKey
 * @see org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalPrivateKeyReference
 * @see ConfidentialMPTConvertContext
 * @see SecretKeyProof
 */
public interface SecretKeyProofGenerator<P extends ElGamalPrivateKeyable> {

  /**
   * Generates a Schnorr Proof of Knowledge for the given private key.
   *
   * <p>The public key is derived from the private key as P = sk * G.</p>
   *
   * @param privateKey The private key (scalar).
   * @param context    The context for domain separation (e.g., from {@link ConfidentialMPTConvertContext#generate}).
   *                   Can be null for no context.
   * @param nonce      The random nonce (k) used for the commitment (must be a valid scalar). If null, a random nonce
   *                   will be generated.
   *
   * @return A {@link SecretKeyProof} containing the 65-byte proof.
   *
   * @throws NullPointerException if privateKey is null.
   */
  SecretKeyProof generateProof(P privateKey, ConfidentialMPTConvertContext context, BlindingFactor nonce);

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
  boolean verifyProof(SecretKeyProof proof, ElGamalPublicKey publicKey, ConfidentialMPTConvertContext context);
}

