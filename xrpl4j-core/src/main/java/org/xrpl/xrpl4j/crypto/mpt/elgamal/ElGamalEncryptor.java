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
public interface ElGamalEncryptor {

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
   * @param publicKey      The recipient's ElGamal public key.
   * @param blindingFactor The blinding factor (validated 32-byte scalar).
   *
   * @return The {@link ElGamalCiphertext} containing C1 and C2.
   *
   * @throws NullPointerException if any parameter is null.
   */
  ElGamalCiphertext encrypt(UnsignedLong amount, PublicKey publicKey, BlindingFactor blindingFactor);
}
