package org.xrpl.xrpl4j.crypto.mpt.elgamal;

import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.mpt.BlindingFactor;

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
public interface ElGamalEncryptionVerifier {
  /**
   * Verifies that a ciphertext is a valid encryption of the given amount.
   *
   * <p>This requires knowledge of the blinding factor used during encryption.</p>
   *
   * @param ciphertext     The ciphertext to verify.
   * @param publicKey      The ElGamal public key used for encryption.
   * @param amount         The claimed unsigned amount.
   * @param blindingFactor The blinding factor used during encryption.
   *
   * @return {@code true} if the ciphertext is valid, {@code false} otherwise.
   *
   * @throws NullPointerException if any parameter is null.
   */
  boolean verifyEncryption(
    ElGamalCiphertext ciphertext,
    PublicKey publicKey,
    UnsignedLong amount,
    BlindingFactor blindingFactor
  );
}
