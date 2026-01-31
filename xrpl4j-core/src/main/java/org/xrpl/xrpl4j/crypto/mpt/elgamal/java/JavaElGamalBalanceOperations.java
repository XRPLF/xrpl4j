package org.xrpl.xrpl4j.crypto.mpt.elgamal.java;

import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalBalanceOperations;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalCiphertext;

import java.util.Objects;

/**
 * Implementation of ElGamal balance operations over the secp256k1 elliptic curve for confidential MPT operations.
 */
public class JavaElGamalBalanceOperations implements ElGamalBalanceOperations {

  private final Secp256k1Operations secp256k1;

  /**
   * Constructs a new ElGamalEncryption instance.
   */
  public JavaElGamalBalanceOperations(final Secp256k1Operations secp256k1) {
    this.secp256k1 = Objects.requireNonNull(secp256k1);
  }

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
  public ElGamalCiphertext add(ElGamalCiphertext aCipherText, ElGamalCiphertext bCipherText) {
    Objects.requireNonNull(aCipherText, "ciphertext a must not be null");
    Objects.requireNonNull(bCipherText, "ciphertext b must not be null");

    ECPoint sumC1 = secp256k1.add(aCipherText.c1(), bCipherText.c1());
    ECPoint sumC2 = secp256k1.add(aCipherText.c2(), bCipherText.c2());

    return new ElGamalCiphertext(sumC1, sumC2);
  }

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
  public ElGamalCiphertext subtract(ElGamalCiphertext aCipherText, ElGamalCiphertext bCipherText) {
    Objects.requireNonNull(aCipherText, "ciphertext a must not be null");
    Objects.requireNonNull(bCipherText, "ciphertext b must not be null");

    // Negate B's points
    ECPoint negBC1 = secp256k1.negate(bCipherText.c1());
    ECPoint negBC2 = secp256k1.negate(bCipherText.c2());

    // Add A and negated B
    ECPoint diffC1 = secp256k1.add(aCipherText.c1(), negBC1);
    ECPoint diffC2 = secp256k1.add(aCipherText.c2(), negBC2);

    return new ElGamalCiphertext(diffC1, diffC2);
  }
}
