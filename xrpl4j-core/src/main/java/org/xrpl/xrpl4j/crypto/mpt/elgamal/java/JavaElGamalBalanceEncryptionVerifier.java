package org.xrpl.xrpl4j.crypto.mpt.elgamal.java;

import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.crypto.mpt.BlindingFactor;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalBalanceEncryptionVerifier;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalCiphertext;
import org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalPublicKey;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Implementation of ElGamal encryption over the secp256k1 elliptic curve for confidential MPT operations.
 *
 * <p>This class provides methods for:</p>
 * <ul>
 *   <li>Generating ElGamal key pairs</li>
 *   <li>Encrypting amounts using ElGamal encryption</li>
 *   <li>Generating canonical encrypted zeros</li>
 *   <li>Verifying encryption correctness</li>
 * </ul>
 */
public class JavaElGamalBalanceEncryptionVerifier implements ElGamalBalanceEncryptionVerifier {
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
   */
  @Override
  public boolean verifyEncryption(
    ElGamalCiphertext ciphertext,
    ElGamalPublicKey publicKey,
    UnsignedLong amount,
    BlindingFactor blindingFactor
  ) {
    Objects.requireNonNull(ciphertext, "ciphertext must not be null");
    Objects.requireNonNull(publicKey, "publicKey must not be null");
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(blindingFactor, "blindingFactor must not be null");

    BigInteger k = new BigInteger(1, blindingFactor.toBytes());
    ECPoint publicKeyPoint = publicKey.asEcPoint();

    // Verify C1: k * G == C1
    ECPoint expectedC1 = Secp256k1Operations.multiplyG(k);
    if (!Secp256k1Operations.pointsEqual(ciphertext.c1(), expectedC1)) {
      return false;
    }

    // Verify C2: amount * G + k * Q == C2
    ECPoint mG = Secp256k1Operations.multiplyG(amount.bigIntegerValue());
    ECPoint sharedSecret = Secp256k1Operations.multiply(publicKeyPoint, k);
    ECPoint expectedC2 = Secp256k1Operations.add(mG, sharedSecret);

    return Secp256k1Operations.pointsEqual(ciphertext.c2(), expectedC2);
  }
}
