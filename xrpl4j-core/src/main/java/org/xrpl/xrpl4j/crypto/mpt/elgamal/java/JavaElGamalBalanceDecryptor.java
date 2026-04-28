package org.xrpl.xrpl4j.crypto.mpt.elgamal.java;

import com.google.common.base.Preconditions;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalBalanceDecryptor;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalCiphertext;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Implementation of ElGamal encryption over the secp256k1 elliptic curve for confidential MPT operations
 *
 * <p>This class provides methods for:</p>
 * <ul>
 *   <li>Decrypting ciphertexts (via brute-force for small amounts)</li>
 * </ul>
 */
public class JavaElGamalBalanceDecryptor implements ElGamalBalanceDecryptor {

  /**
   * Maximum amount that can be decrypted via brute-force search.
   */
  public static final long MAX_DECRYPTABLE_AMOUNT = 1_000_000L;

  private final Secp256k1Operations secp256k1;

  /**
   * Constructs a new ElGamalEncryption instance.
   */
  public JavaElGamalBalanceDecryptor(final Secp256k1Operations secp256k1) {
    this.secp256k1 = Objects.requireNonNull(secp256k1);
  }

  /**
   * Decrypts an ElGamal ciphertext to recover the original amount.
   *
   * <p>This uses brute-force search and is only practical for small amounts
   * (up to {@link #MAX_DECRYPTABLE_AMOUNT}).</p>
   *
   * @param ciphertext The ciphertext to decrypt.
   * @param privateKey The 32-byte private key.
   *
   * @return The decrypted amount.
   *
   * @throws IllegalArgumentException if the amount cannot be found within the search range.
   */
  public long decrypt(final ElGamalCiphertext ciphertext, final byte[] privateKey) {
    Objects.requireNonNull(ciphertext, "ciphertext must not be null");
    Objects.requireNonNull(privateKey, "privateKey must not be null");
    Preconditions.checkArgument(privateKey.length == 32, "privateKey must be 32 bytes");

    BigInteger privKeyScalar = new BigInteger(1, privateKey);

    // S = privateKey * C1
    ECPoint sharedSecret = secp256k1.multiply(ciphertext.c1(), privKeyScalar);

    // Check for amount = 0: C2 == S
    if (secp256k1.pointsEqual(ciphertext.c2(), sharedSecret)) {
      return 0;
    }

    // M = C2 - S
    ECPoint negS = secp256k1.negate(sharedSecret);
    ECPoint m = secp256k1.add(ciphertext.c2(), negS);

    // Brute-force search: find i such that i * G == M
    ECPoint gPoint = secp256k1.getG();
    ECPoint currentM = gPoint;

    for (long i = 1; i <= MAX_DECRYPTABLE_AMOUNT; i++) {
      if (secp256k1.pointsEqual(m, currentM)) {
        return i;
      }
      currentM = secp256k1.add(currentM, gPoint);
    }

    throw new IllegalArgumentException("Amount not found within search range (0 to " + MAX_DECRYPTABLE_AMOUNT + ")");
  }
}
