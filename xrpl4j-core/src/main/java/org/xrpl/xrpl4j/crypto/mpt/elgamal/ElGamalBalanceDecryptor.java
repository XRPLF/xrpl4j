package org.xrpl.xrpl4j.crypto.mpt.elgamal;

/**
 * Interface for decrypting confidential balances.
 */
public interface ElGamalBalanceDecryptor {

  /**
   * Decrypts an ElGamal ciphertext to recover the original amount.
   *
   * <p>This uses brute-force search and is only practical for small amounts.</p>
   *
   * @param ciphertext The ciphertext to decrypt.
   * @param privateKey The 32-byte private key.
   *
   * @return The decrypted amount.
   *
   * @throws IllegalArgumentException if the amount cannot be found within the search range.
   */
  // TODO: Use PrivateKeyable
  long decrypt(ElGamalCiphertext ciphertext, byte[] privateKey);
}
