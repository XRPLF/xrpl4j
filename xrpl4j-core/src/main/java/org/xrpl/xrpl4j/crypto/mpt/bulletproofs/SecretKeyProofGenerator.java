package org.xrpl.xrpl4j.crypto.mpt.bulletproofs;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

/**
 * Interface for generating Schnorr Proof of Knowledge (PoK) for ElGamal secret keys.
 *
 * <p>This proves knowledge of the private key corresponding to an ElGamal public key
 * without revealing the private key itself. The proof is used in confidential MPT transactions
 * to prove ownership of the encryption key.</p>
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
 */
public interface SecretKeyProofGenerator {

  /**
   * The length of the proof in bytes (33 bytes T + 32 bytes s).
   */
  int PROOF_LENGTH = 65;

  /**
   * Generates a Schnorr Proof of Knowledge for the given private key.
   *
   * <p>The public key is derived from the private key as P = sk * G.</p>
   *
   * @param privateKey The 32-byte private key (scalar).
   * @param contextId  A 32-byte context identifier for domain separation. Can be null for no context.
   * @param nonce      The 32-byte random nonce (k) used for the commitment. If null, a random nonce will be generated.
   *
   * @return A 65-byte proof (33 bytes T + 32 bytes s).
   *
   * @throws IllegalArgumentException if privateKey is not 32 bytes, contextId is not 32 bytes (when provided),
   *                                  or nonce is not 32 bytes (when provided).
   */
  byte[] generateProof(byte[] privateKey, byte[] contextId, byte[] nonce);

  /**
   * Verifies a Schnorr Proof of Knowledge.
   *
   * <p>Verification equation: s * G == T + e * P</p>
   *
   * @param proof     The 65-byte proof to verify.
   * @param publicKey The public key point P.
   * @param contextId A 32-byte context identifier. Can be null for no context.
   *
   * @return {@code true} if the proof is valid, {@code false} otherwise.
   */
  boolean verifyProof(byte[] proof, ECPoint publicKey, byte[] contextId);

  /**
   * Generates the context hash for a ConfidentialMPTConvert transaction.
   *
   * <p>This matches rippled's getConvertContextHash function which computes:
   * SHA512Half(txType || account || sequence || issuanceId || amount)</p>
   *
   * @param account    The account address.
   * @param sequence   The transaction sequence number.
   * @param issuanceId The MPTokenIssuanceId (24 bytes as hex string).
   * @param amount     The amount being converted.
   *
   * @return A 32-byte context hash (SHA512Half).
   */
  byte[] generateConvertContext(Address account, UnsignedInteger sequence, MpTokenIssuanceId issuanceId, UnsignedLong amount);
}

