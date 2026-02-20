package org.xrpl.xrpl4j.crypto.mpt.elgamal;

import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;

/**
 * Interface for encrypting and decrypting confidential balances using ElGamal encryption.
 *
 * <p>This interface provides the core cryptographic operations needed for confidential MPT transfers:</p>
 * <ul>
 *   <li>Encrypting amounts under a public key</li>
 *   <li>Decrypting ciphertexts using a private key</li>
 *   <li>Homomorphic addition and subtraction of encrypted balances</li>
 *   <li>Generating canonical encrypted zeros for inbox reset</li>
 *   <li>Verifying encryption correctness</li>
 * </ul>
 *
 * <p>The encryption scheme uses EC-ElGamal over secp256k1, which provides additive homomorphism
 * allowing encrypted balances to be updated without decryption.</p>
 */
public interface ElGamalBalanceEncryptor {

  /**
   * Maximum amount that can be decrypted via brute-force search.
   */
  UnsignedLong MAX_DECRYPTABLE_AMOUNT = UnsignedLong.valueOf(1_000_000L);

  /**
   * Encrypts an amount using ElGamal encryption.
   *
   * <p>The encryption produces a ciphertext (C1, C2) where:</p>
   * <ul>
   *   <li>C1 = blindingFactor * G</li>
   *   <li>C2 = amount * G + blindingFactor * publicKey</li>
   * </ul>
   *
   * @param amount         The unsigned amount to encrypt.
   * @param publicKey      The recipient's public key.
   * @param blindingFactor A 32-byte random blinding factor.
   *
   * @return The {@link ElGamalCiphertext} containing C1 and C2.
   *
   * @throws IllegalArgumentException if the blinding factor is invalid.
   */
  // TODO: Consider a PublicKey instead of an ECPoint? Depends on the JNI impl.
  ElGamalCiphertext encrypt(UnsignedLong amount, ECPoint publicKey, byte[] blindingFactor);

  /**
   * Generates a canonical encrypted zero for a given account and MPT issuance.
   *
   * <p>This produces a deterministic encryption of zero that can be used for inbox reset
   * after a merge operation. The deterministic nature ensures all validators compute the same ciphertext.</p>
   *
   * @param publicKey     The public key to encrypt to.
   * @param accountId     The 20-byte account ID.
   * @param mptIssuanceId The 24-byte MPT issuance ID.
   *
   * @return An {@link ElGamalCiphertext} encrypting zero.
   */
  ElGamalCiphertext generateCanonicalEncryptedZero(ECPoint publicKey, byte[] accountId, byte[] mptIssuanceId);

  /**
   * Verifies that a ciphertext is a valid encryption of the given amount.
   *
   * <p>This requires knowledge of the blinding factor used during encryption.</p>
   *
   * @param ciphertext     The ciphertext to verify.
   * @param publicKey      The public key used for encryption.
   * @param amount         The claimed unsigned amount.
   * @param blindingFactor The blinding factor used during encryption.
   *
   * @return {@code true} if the ciphertext is valid, {@code false} otherwise.
   */
  boolean verifyEncryption(ElGamalCiphertext ciphertext, ECPoint publicKey, UnsignedLong amount, byte[] blindingFactor);
}
