package org.xrpl.xrpl4j.crypto.mpt.bulletproofs;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

/**
 * Interface for generating and verifying Zero-Knowledge Proofs linking ElGamal ciphertexts
 * and Pedersen commitments.
 *
 * <p>This proves that an ElGamal ciphertext and a Pedersen commitment encode the same
 * underlying plaintext value m, without revealing m or the blinding factors.</p>
 *
 * <p><b>Statement:</b> The prover demonstrates knowledge of scalars (m, r, rho) such that:
 * <ul>
 *   <li>C1 = r * G (ElGamal Ephemeral Key)</li>
 *   <li>C2 = m * G + r * P (ElGamal Masked Value)</li>
 *   <li>PCm = m * G + rho * H (Pedersen Commitment)</li>
 * </ul>
 *
 * <p><b>Protocol (Schnorr-style):</b>
 * <ol>
 *   <li><b>Commitment:</b> Prover samples nonces km, kr, krho and computes:
 *     <ul>
 *       <li>T1 = kr * G</li>
 *       <li>T2 = km * G + kr * P</li>
 *       <li>T3 = km * G + krho * H</li>
 *     </ul>
 *   </li>
 *   <li><b>Challenge:</b> e = H("MPT_ELGAMAL_PEDERSEN_LINK" || C1 || C2 || P || PCm || T1 || T2 || T3 || contextId)</li>
 *   <li><b>Response:</b>
 *     <ul>
 *       <li>sm = km + e * m</li>
 *       <li>sr = kr + e * r</li>
 *       <li>srho = krho + e * rho</li>
 *     </ul>
 *   </li>
 *   <li><b>Verification:</b>
 *     <ul>
 *       <li>sr * G == T1 + e * C1</li>
 *       <li>sm * G + sr * P == T2 + e * C2</li>
 *       <li>sm * G + srho * H == T3 + e * PCm</li>
 *     </ul>
 *   </li>
 * </ol>
 *
 * <p><b>Proof format (serialized):</b> 195 bytes total
 * <ul>
 *   <li>T1 (33 bytes) - Commitment kr * G</li>
 *   <li>T2 (33 bytes) - Commitment km * G + kr * P</li>
 *   <li>T3 (33 bytes) - Commitment km * G + krho * H</li>
 *   <li>sm (32 bytes) - Response for amount</li>
 *   <li>sr (32 bytes) - Response for ElGamal randomness</li>
 *   <li>srho (32 bytes) - Response for Pedersen blinding factor</li>
 * </ul>
 *
 * @see <a href="ConfidentialMPT_20260201.pdf">Spec Section 3.3.5</a>
 */
public interface ElGamalPedersenLinkProofGenerator {

  /**
   * The size of the proof in bytes: 3 * 33 (points) + 3 * 32 (scalars) = 195 bytes.
   */
  int PROOF_SIZE = 195;

  /**
   * Generates a proof linking an ElGamal ciphertext and a Pedersen commitment.
   *
   * @param c1          The ElGamal ephemeral key (r * G).
   * @param c2          The ElGamal masked value (m * G + r * P).
   * @param publicKey   The ElGamal public key P.
   * @param commitment  The Pedersen commitment (m * G + rho * H).
   * @param amount      The plaintext amount m.
   * @param r           The ElGamal randomness (32 bytes).
   * @param rho         The Pedersen blinding factor (32 bytes).
   * @param contextHash The 32-byte context hash for domain separation (can be null).
   * @param nonceKm     The 32-byte nonce for amount commitment.
   * @param nonceKr     The 32-byte nonce for ElGamal randomness commitment.
   * @param nonceKrho   The 32-byte nonce for Pedersen blinding factor commitment.
   *
   * @return The serialized proof as a 195-byte array.
   */
  byte[] generateProof(
    ECPoint c1,
    ECPoint c2,
    ECPoint publicKey,
    ECPoint commitment,
    UnsignedLong amount,
    byte[] r,
    byte[] rho,
    byte[] contextHash,
    byte[] nonceKm,
    byte[] nonceKr,
    byte[] nonceKrho
  );

  /**
   * Verifies a proof linking an ElGamal ciphertext and a Pedersen commitment.
   *
   * @param proof       The serialized proof bytes (195 bytes).
   * @param c1          The ElGamal ephemeral key (r * G).
   * @param c2          The ElGamal masked value (m * G + r * P).
   * @param publicKey   The ElGamal public key P.
   * @param commitment  The Pedersen commitment (m * G + rho * H).
   * @param contextHash The 32-byte context hash for domain separation (can be null).
   *
   * @return {@code true} if the proof is valid, {@code false} otherwise.
   */
  boolean verify(
    byte[] proof,
    ECPoint c1,
    ECPoint c2,
    ECPoint publicKey,
    ECPoint commitment,
    byte[] contextHash
  );

  /**
   * Generates the context hash for ConfidentialMPTSend transactions.
   *
   * <p>The context hash is computed as SHA512Half of:
   * <ul>
   *   <li>txType (2 bytes) - ttCONFIDENTIAL_MPT_SEND</li>
   *   <li>account (20 bytes) - sender account</li>
   *   <li>sequence (4 bytes) - transaction sequence</li>
   *   <li>issuanceId (24 bytes) - MPTokenIssuanceID</li>
   *   <li>destination (20 bytes) - destination account</li>
   *   <li>version (4 bytes) - confidential balance version</li>
   * </ul>
   *
   * @param account     The sender account address.
   * @param sequence    The transaction sequence number.
   * @param issuanceId  The MPTokenIssuanceID.
   * @param destination The destination account address.
   * @param version     The confidential balance version from the MPToken ledger object.
   *
   * @return The 32-byte context hash.
   */
  byte[] generateSendContext(
    Address account,
    UnsignedInteger sequence,
    MpTokenIssuanceId issuanceId,
    Address destination,
    UnsignedInteger version
  );

  /**
   * Generates the context hash for ConfidentialMPTConvertBack transactions.
   *
   * <p>The context hash is computed as SHA512Half of:
   * <ul>
   *   <li>txType (2 bytes) - ttCONFIDENTIAL_MPT_CONVERT_BACK</li>
   *   <li>account (20 bytes) - holder account</li>
   *   <li>sequence (4 bytes) - transaction sequence</li>
   *   <li>issuanceId (32 bytes) - MPTokenIssuanceID</li>
   *   <li>amount (8 bytes) - amount being converted back</li>
   *   <li>version (4 bytes) - confidential balance version</li>
   * </ul>
   *
   * @param account    The holder account address.
   * @param sequence   The transaction sequence number.
   * @param issuanceId The MPTokenIssuanceID.
   * @param amount     The amount being converted back to public balance.
   * @param version    The confidential balance version from the MPToken ledger object.
   *
   * @return The 32-byte context hash.
   */
  byte[] generateConvertBackContext(
    Address account,
    UnsignedInteger sequence,
    MpTokenIssuanceId issuanceId,
    UnsignedLong amount,
    UnsignedInteger version
  );
}

