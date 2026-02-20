package org.xrpl.xrpl4j.crypto.mpt.elgamal.java;

import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalBalanceEncryptor;
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
public class JavaElGamalBalanceEncryptor implements ElGamalBalanceEncryptor {

  /**
   * Domain separator used for generating canonical encrypted zeros.
   */
  private static final byte[] DOMAIN_SEPARATOR = "EncZero".getBytes();

  private final Secp256k1Operations secp256k1;

  /**
   * Constructs a new ElGamalEncryption instance.
   */
  public JavaElGamalBalanceEncryptor(final Secp256k1Operations secp256k1) {
    this.secp256k1 = Objects.requireNonNull(secp256k1);
  }

  /**
   * Encrypts an amount using ElGamal encryption.
   *
   * <p>The encryption produces a ciphertext (C1, C2) where:</p>
   * <ul>
   *   <li>C1 = blindingFactor * G</li>
   *   <li>C2 = amount * G + blindingFactor * Q (where Q is the public key)</li>
   * </ul>
   *
   * @param publicKey      The recipient's public key.
   * @param amount         The unsigned amount to encrypt.
   * @param blindingFactor A 32-byte blinding factor (random scalar).
   *
   * @return The {@link ElGamalCiphertext} containing C1 and C2.
   *
   * @throws IllegalArgumentException if the blinding factor is invalid.
   */
  @Override
  public ElGamalCiphertext encrypt(final UnsignedLong amount, final ECPoint publicKey, final byte[] blindingFactor) {
    Objects.requireNonNull(publicKey, "publicKey must not be null");
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(blindingFactor, "blindingFactor must not be null");
    Preconditions.checkArgument(blindingFactor.length == 32, "blindingFactor must be 32 bytes");

    BigInteger k = new BigInteger(1, blindingFactor);
    Preconditions.checkArgument(secp256k1.isValidPrivateKey(k), "blindingFactor is not a valid scalar");

    // C1 = k * G
    ECPoint c1 = secp256k1.multiplyG(k);

    // S = k * Q (shared secret)
    ECPoint sharedSecret = secp256k1.multiply(publicKey, k);

    ECPoint c2;
    if (amount.equals(UnsignedLong.ZERO)) {
      // For amount = 0, C2 = S
      c2 = sharedSecret;
    } else {
      // M = amount * G
      ECPoint m = secp256k1.multiplyG(amount.bigIntegerValue());
      // C2 = M + S
      c2 = secp256k1.add(m, sharedSecret);
    }

    return new ElGamalCiphertext(c1, c2);
  }

  /**
   * Generates a canonical encrypted zero for a given account and MPT issuance.
   *
   * <p>This produces a deterministic encryption of zero that can be used as a starting point
   * for balance tracking.</p>
   *
   * @param publicKey     The public key to encrypt to.
   * @param accountId     The 20-byte account ID.
   * @param mptIssuanceId The 24-byte MPT issuance ID.
   *
   * @return An {@link ElGamalCiphertext} encrypting zero.
   */
  @Override
  public ElGamalCiphertext generateCanonicalEncryptedZero(final ECPoint publicKey, final byte[] accountId,
    final byte[] mptIssuanceId) {
    Objects.requireNonNull(publicKey, "publicKey must not be null");
    Objects.requireNonNull(accountId, "accountId must not be null");
    Objects.requireNonNull(mptIssuanceId, "mptIssuanceId must not be null");
    Preconditions.checkArgument(accountId.length == 20, "accountId must be 20 bytes");
    Preconditions.checkArgument(mptIssuanceId.length == 24, "mptIssuanceId must be 24 bytes");

    // Build hash input: "EncZero" || accountId || mptIssuanceId
    byte[] hashInput = new byte[DOMAIN_SEPARATOR.length + 20 + 24];
    System.arraycopy(DOMAIN_SEPARATOR, 0, hashInput, 0, DOMAIN_SEPARATOR.length);
    System.arraycopy(accountId, 0, hashInput, DOMAIN_SEPARATOR.length, 20);
    System.arraycopy(mptIssuanceId, 0, hashInput, DOMAIN_SEPARATOR.length + 20, 24);

    // Hash to create deterministic scalar
    // TODO: Limit this so that it doesn't produce a runaway while loop
    byte[] deterministicScalar = new byte[32];
    BigInteger scalar;
    do {
      SHA256Digest digest = new SHA256Digest();
      digest.update(hashInput, 0, hashInput.length);
      digest.doFinal(deterministicScalar, 0);
      scalar = new BigInteger(1, deterministicScalar);
    } while (!secp256k1.isValidPrivateKey(scalar));

    // Encrypt amount 0 using the deterministic scalar
    return encrypt(UnsignedLong.ZERO, publicKey, deterministicScalar);
  }

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
  @Override
  public boolean verifyEncryption(ElGamalCiphertext ciphertext, ECPoint publicKey, UnsignedLong amount,
    byte[] blindingFactor) {
    Objects.requireNonNull(ciphertext, "ciphertext must not be null");
    Objects.requireNonNull(publicKey, "publicKey must not be null");
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(blindingFactor, "blindingFactor must not be null");

    if (blindingFactor.length != 32) {
      return false;
    }

    BigInteger k = new BigInteger(1, blindingFactor);
    if (!secp256k1.isValidPrivateKey(k)) {
      return false;
    }

    // Verify C1: k * G == C1
    ECPoint expectedC1 = secp256k1.multiplyG(k);
    if (!secp256k1.pointsEqual(ciphertext.c1(), expectedC1)) {
      return false;
    }

    // Verify C2: amount * G + k * Q == C2
    ECPoint mG = secp256k1.multiplyG(amount.bigIntegerValue());
    ECPoint sharedSecret = secp256k1.multiply(publicKey, k);
    ECPoint expectedC2 = secp256k1.add(mG, sharedSecret);

    return secp256k1.pointsEqual(ciphertext.c2(), expectedC2);
  }
}
