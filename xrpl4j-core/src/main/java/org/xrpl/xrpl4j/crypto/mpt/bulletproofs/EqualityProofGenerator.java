package org.xrpl.xrpl4j.crypto.mpt.bulletproofs;

import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalCiphertext;

/**
 * Interface for generating and verifying zero-knowledge equality proofs.
 *
 * <p>Equality proofs are used in confidential MPT transfers to prove that multiple
 * ciphertexts encrypt the same value under different public keys. This is essential for the multi-ciphertext
 * architecture where:</p>
 * <ul>
 *   <li>The sender's ciphertext encrypts the transfer amount under the sender's key</li>
 *   <li>The receiver's ciphertext encrypts the same amount under the receiver's key</li>
 *   <li>The issuer's ciphertext encrypts the same amount under the issuer's key</li>
 *   <li>Optionally, an auditor's ciphertext encrypts the same amount under the auditor's key</li>
 * </ul>
 *
 * <p>Without equality proofs, a malicious sender could credit a different amount to the
 * receiver than what was debited from their own balance.</p>
 */
public interface EqualityProofGenerator {

  /**
   * Generates a proof that two ciphertexts encrypt the same value.
   *
   * @param ciphertext1     The first ciphertext.
   * @param ciphertext2     The second ciphertext.
   * @param publicKey1      The public key for the first ciphertext.
   * @param publicKey2      The public key for the second ciphertext.
   * @param amount          The unsigned plaintext amount (witness).
   * @param blindingFactor1 The blinding factor for the first ciphertext.
   * @param blindingFactor2 The blinding factor for the second ciphertext.
   *
   * @return An {@link EqualityProof} that can be verified.
   */
  EqualityProof generateProof(
    final ElGamalCiphertext ciphertext1,
    final ElGamalCiphertext ciphertext2,
    final ECPoint publicKey1,
    final ECPoint publicKey2,
    final UnsignedLong amount,
    final byte[] blindingFactor1,
    final byte[] blindingFactor2
  );

  /**
   * Verifies that two ciphertexts encrypt the same value.
   *
   * @param proof       The equality proof to verify.
   * @param ciphertext1 The first ciphertext.
   * @param ciphertext2 The second ciphertext.
   * @param publicKey1  The public key for the first ciphertext.
   * @param publicKey2  The public key for the second ciphertext.
   *
   * @return {@code true} if the proof is valid, {@code false} otherwise.
   */
  boolean verify(
    EqualityProof proof,
    ElGamalCiphertext ciphertext1,
    ElGamalCiphertext ciphertext2,
    ECPoint publicKey1,
    ECPoint publicKey2
  );

  /**
   * Generates a proof that a ciphertext encrypts a known plaintext value.
   *
   * <p>This is used in ConfidentialMPTConvert and ConfidentialMPTConvertBack
   * transactions where the amount is publicly revealed.</p>
   *
   * @param ciphertext     The ciphertext to prove.
   * @param publicKey      The public key used for encryption.
   * @param amount         The unsigned plaintext amount.
   * @param blindingFactor The blinding factor used in encryption.
   *
   * @return An {@link EqualityProof} proving the ciphertext encrypts the given amount.
   */
  EqualityProof generatePlaintextEqualityProof(
    ElGamalCiphertext ciphertext,
    ECPoint publicKey,
    UnsignedLong amount,
    byte[] blindingFactor
  );

  /**
   * Verifies that a ciphertext encrypts a known plaintext value.
   *
   * @param proof      The equality proof to verify.
   * @param ciphertext The ciphertext.
   * @param publicKey  The public key used for encryption.
   * @param amount     The claimed unsigned plaintext amount.
   *
   * @return {@code true} if the proof is valid, {@code false} otherwise.
   */
  boolean verifyPlaintextEquality(
    EqualityProof proof,
    ElGamalCiphertext ciphertext,
    ECPoint publicKey,
    UnsignedLong amount
  );
}
