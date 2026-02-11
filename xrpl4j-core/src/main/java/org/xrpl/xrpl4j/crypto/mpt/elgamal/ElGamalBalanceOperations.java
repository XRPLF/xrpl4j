package org.xrpl.xrpl4j.crypto.mpt.elgamal;

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
public interface ElGamalBalanceOperations {

  /**
   * Adds two ElGamal ciphertexts homomorphically.
   *
   * <p>The result encrypts the sum of the two original amounts.</p>
   *
   * @param aCipherText The first ciphertext.
   * @param bCipherText The second ciphertext.
   *
   * @return A new ciphertext encrypting (amount_a + amount_b).
   */
  ElGamalCiphertext add(ElGamalCiphertext aCipherText, ElGamalCiphertext bCipherText);

  /**
   * Subtracts one ElGamal ciphertext from another homomorphically.
   *
   * <p>The result encrypts the difference of the two original amounts.</p>
   *
   * @param aCipherText The ciphertext to subtract from.
   * @param bCipherText The ciphertext to subtract.
   *
   * @return A new ciphertext encrypting (amount_a - amount_b).
   */
  ElGamalCiphertext subtract(ElGamalCiphertext aCipherText, ElGamalCiphertext bCipherText);
}
