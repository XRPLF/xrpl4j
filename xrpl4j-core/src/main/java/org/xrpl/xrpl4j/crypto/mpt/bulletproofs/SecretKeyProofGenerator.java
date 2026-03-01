package org.xrpl.xrpl4j.crypto.mpt.bulletproofs;

import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.keys.PrivateKeyable;
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
public interface SecretKeyProofGenerator<PK extends PrivateKeyable> {

  /**
   * Generates a Schnorr Proof of Knowledge for the given private key.
   *
   * <p>The public key is derived from the private key as P = sk * G.</p>
   *
   * @param privateKeyable The private key (scalar).
   * @param context    The context for domain separation (e.g., from {@link ConfidentialMPTConvertContext#generate}).
   *                   Can be null for no context.
   *
   * @return A {@link SecretKeyProof} containing the 65-byte proof.
   *
   * @throws NullPointerException if privateKey is null.
   */
  SecretKeyProof generateProof(PK privateKeyable, ConfidentialMPTConvertContext context);
}

