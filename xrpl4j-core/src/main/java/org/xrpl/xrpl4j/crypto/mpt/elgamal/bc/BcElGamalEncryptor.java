package org.xrpl.xrpl4j.crypto.mpt.elgamal.bc;

import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.mpt.BlindingFactor;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalEncryptor;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalCiphertext;

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
public class BcElGamalEncryptor implements ElGamalEncryptor {
  /**
   * Encrypts an amount using ElGamal encryption.
   *
   * <p>The encryption produces a ciphertext (C1, C2) where:</p>
   * <ul>
   *   <li>C1 = blindingFactor * G</li>
   *   <li>C2 = amount * G + blindingFactor * Q (where Q is the public key)</li>
   * </ul>
   *
   * @param amount         The unsigned amount to encrypt.
   * @param publicKey      The recipient's ElGamal public key.
   * @param blindingFactor The blinding factor (validated 32-byte scalar).
   *
   * @return The {@link ElGamalCiphertext} containing C1 and C2.
   */
  @Override
  public ElGamalCiphertext encrypt(
    final UnsignedLong amount,
    final PublicKey publicKey,
    final BlindingFactor blindingFactor
  ) {
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(publicKey, "publicKey must not be null");
    Objects.requireNonNull(blindingFactor, "blindingFactor must not be null");

    BigInteger k = new BigInteger(1, blindingFactor.toBytes());
    ECPoint publicKeyPoint = Secp256k1Operations.toEcPoint(publicKey);

    // C1 = k * G
    ECPoint c1 = Secp256k1Operations.multiplyG(k);

    // S = k * Q (shared secret)
    ECPoint sharedSecret = Secp256k1Operations.multiply(publicKeyPoint, k);

    ECPoint c2;
    if (amount.equals(UnsignedLong.ZERO)) {
      // For amount = 0, C2 = S
      c2 = sharedSecret;
    } else {
      // M = amount * G
      ECPoint m = Secp256k1Operations.multiplyG(amount.bigIntegerValue());
      // C2 = M + S
      c2 = Secp256k1Operations.add(m, sharedSecret);
    }

    return new ElGamalCiphertext(c1, c2);
  }
}
