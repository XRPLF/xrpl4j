package org.xrpl.xrpl4j.crypto.mpt.elgamal.java;

import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.crypto.mpt.BlindingFactor;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalBalanceEncryptor;
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
public class JavaElGamalBalanceEncryptor implements ElGamalBalanceEncryptor {

  /**
   * Domain separator used for generating canonical encrypted zeros.
   */
  private static final byte[] DOMAIN_SEPARATOR = "EncZero".getBytes();

  /**
   * Constructs a new ElGamalEncryption instance.
   */
  public JavaElGamalBalanceEncryptor() {
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
   * @param amount         The unsigned amount to encrypt.
   * @param publicKey      The recipient's ElGamal public key.
   * @param blindingFactor The blinding factor (validated 32-byte scalar).
   *
   * @return The {@link ElGamalCiphertext} containing C1 and C2.
   */
  @Override
  public ElGamalCiphertext encrypt(
    final UnsignedLong amount,
    final ElGamalPublicKey publicKey,
    final BlindingFactor blindingFactor
  ) {
    Objects.requireNonNull(amount, "amount must not be null");
    Objects.requireNonNull(publicKey, "publicKey must not be null");
    Objects.requireNonNull(blindingFactor, "blindingFactor must not be null");

    BigInteger k = new BigInteger(1, blindingFactor.toBytes());
    ECPoint publicKeyPoint = publicKey.asEcPoint();

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

  /**
   * Generates a canonical encrypted zero for a given account and MPT issuance.
   *
   * <p>This produces a deterministic encryption of zero that can be used as a starting point
   * for balance tracking.</p>
   *
   * @param publicKey     The ElGamal public key to encrypt to.
   * @param accountId     The 20-byte account ID.
   * @param mptIssuanceId The 24-byte MPT issuance ID.
   *
   * @return An {@link ElGamalCiphertext} encrypting zero.
   */
  @Override
  public ElGamalCiphertext generateCanonicalEncryptedZero(
    final ElGamalPublicKey publicKey,
    final byte[] accountId,
    final byte[] mptIssuanceId
  ) {
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
    } while (!Secp256k1Operations.isValidPrivateKey(scalar));

    // Encrypt amount 0 using the deterministic scalar
    BlindingFactor blindingFactor = BlindingFactor.fromBytes(deterministicScalar);
    return encrypt(UnsignedLong.ZERO, publicKey, blindingFactor);
  }

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
