package org.xrpl.xrpl4j.crypto.mpt.bulletproofs;

import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.crypto.mpt.BlindingFactor;
import org.xrpl.xrpl4j.crypto.mpt.context.LinkProofContext;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalCiphertext;
import org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalPrivateKeyable;
import org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalPublicKey;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.ElGamalPedersenLinkProof;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.PedersenCommitment;

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
 * @param <P> The type of private key this generator accepts, must extend {@link ElGamalPrivateKeyable}.
 *
 * @see LinkageProofType
 * @see ElGamalPedersenLinkProof
 * @see <a href="ConfidentialMPT_20260201.pdf">Spec Section 3.3.5</a>
 */
public interface ElGamalPedersenLinkProofGenerator<P extends ElGamalPrivateKeyable> {

  /**
   * Generates a proof linking an ElGamal ciphertext and a Pedersen commitment.
   *
   * <p>The {@link LinkageProofType} determines how the ciphertext and public key parameters
   * are used in the proof:
   * <ul>
   *   <li>{@link LinkageProofType#AMOUNT_COMMITMENT}: For proving a newly encrypted amount.
   *       Uses c1=ciphertext.c1, c2=ciphertext.c2, pk=publicKey, r=elGamalBlindingFactor.</li>
   *   <li>{@link LinkageProofType#BALANCE_COMMITMENT}: For proving an existing encrypted balance.
   *       Uses c1=publicKey, c2=ciphertext.c2, pk=ciphertext.c1, r=privateKey (from elGamalBlindingFactor).</li>
   * </ul>
   *
   * @param proofType              The type of linkage proof to generate.
   * @param ciphertext             The ElGamal ciphertext.
   * @param publicKey              The ElGamal public key.
   * @param commitment             The Pedersen commitment.
   * @param amount                 The plaintext amount m.
   * @param elGamalBlindingFactor  The ElGamal blinding factor (r for amount, private key for balance).
   * @param pedersenBlindingFactor The Pedersen blinding factor (rho).
   * @param nonceKm                Random nonce for amount commitment.
   * @param nonceKr                Random nonce for ElGamal randomness commitment.
   * @param nonceKrho              Random nonce for Pedersen blinding factor commitment.
   * @param context                The context for domain separation.
   *
   * @return An {@link ElGamalPedersenLinkProof} containing the 195-byte proof.
   *
   * @throws NullPointerException if any required parameter is null.
   */
  ElGamalPedersenLinkProof generateProof(
    LinkageProofType proofType,
    ElGamalCiphertext ciphertext,
    ElGamalPublicKey publicKey,
    PedersenCommitment commitment,
    UnsignedLong amount,
    BlindingFactor elGamalBlindingFactor,
    BlindingFactor pedersenBlindingFactor,
    BlindingFactor nonceKm,
    BlindingFactor nonceKr,
    BlindingFactor nonceKrho,
    LinkProofContext context
  );

  /**
   * Verifies a proof linking an ElGamal ciphertext and a Pedersen commitment.
   *
   * <p>The {@link LinkageProofType} determines how the ciphertext and public key parameters
   * are interpreted for verification, matching the ordering used during proof generation.</p>
   *
   * @param proofType  The type of linkage proof being verified.
   * @param proof      The proof to verify.
   * @param ciphertext The ElGamal ciphertext.
   * @param publicKey  The ElGamal public key.
   * @param commitment The Pedersen commitment.
   * @param context    The context for domain separation. Can be null for no context.
   *
   * @return {@code true} if the proof is valid, {@code false} otherwise.
   *
   * @throws NullPointerException if proof, ciphertext, publicKey, or commitment is null.
   */
  boolean verify(
    LinkageProofType proofType,
    ElGamalPedersenLinkProof proof,
    ElGamalCiphertext ciphertext,
    ElGamalPublicKey publicKey,
    PedersenCommitment commitment,
    LinkProofContext context
  );
}

