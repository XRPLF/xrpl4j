package org.xrpl.xrpl4j.crypto.mpt.elgamal.java;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.mpt.KeyPairUtils;
import org.xrpl.xrpl4j.crypto.mpt.RandomnessUtils;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalBalanceDecryptor;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalBalanceEncryptor;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalCiphertext;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalKeyPair;

import java.security.SecureRandom;

/**
 * Unit tests for {@link JavaElGamalBalanceOperations}.
 */
public class JavaElGamalBalanceOperationsTest extends AbstractElGamalTest {

  private ElGamalBalanceEncryptor elGamalBalanceEncryptor;
  private ElGamalBalanceDecryptor javaElGamalBalanceDecryptor;

  private JavaElGamalBalanceOperations elGamalBalanceOperations;

  private Secp256k1Operations secp256k1;

  @BeforeEach
  void setUp() {
    this.secp256k1 = new Secp256k1Operations();
    this.elGamalBalanceEncryptor = new JavaElGamalBalanceEncryptor(secp256k1);
    this.javaElGamalBalanceDecryptor = new JavaElGamalBalanceDecryptor(secp256k1);
    this.elGamalBalanceOperations = new JavaElGamalBalanceOperations(secp256k1);
  }

  @Test
  void testHomomorphicAddition() {
    ECPoint publicKeyAsEcPoint = toPublicKey(keyPair.publicKey());
    UnsignedLong amountA = UnsignedLong.valueOf(5000);
    UnsignedLong amountB = UnsignedLong.valueOf(1234);

    byte[] blindingFactorA = RandomnessUtils.generateRandomScalar(new SecureRandom(), secp256k1);
    byte[] blindingFactorB = RandomnessUtils.generateRandomScalar(new SecureRandom(), secp256k1);

    ElGamalCiphertext ciphertextA = elGamalBalanceEncryptor.encrypt(amountA, publicKeyAsEcPoint, blindingFactorA);
    ElGamalCiphertext ciphertextB = elGamalBalanceEncryptor.encrypt(amountB, publicKeyAsEcPoint, blindingFactorB);

    ElGamalCiphertext sumCiphertext = elGamalBalanceOperations.add(ciphertextA, ciphertextB);
    long decryptedSum = javaElGamalBalanceDecryptor.decrypt(sumCiphertext,
      keyPair.privateKey().naturalBytes().toByteArray()
    );

    assertThat(decryptedSum).isEqualTo(amountA.plus(amountB).longValue());
  }

  @Test
  void testHomomorphicSubtraction() {
    ECPoint publicKeyAsEcPoint = toPublicKey(keyPair.publicKey());
    UnsignedLong amountA = UnsignedLong.valueOf(5000);
    UnsignedLong amountB = UnsignedLong.valueOf(1234);

    byte[] blindingFactorA = RandomnessUtils.generateRandomScalar(new SecureRandom(), secp256k1);
    byte[] blindingFactorB = RandomnessUtils.generateRandomScalar(new SecureRandom(), secp256k1);

    ElGamalCiphertext ciphertextA = elGamalBalanceEncryptor.encrypt(amountA, publicKeyAsEcPoint, blindingFactorA);
    ElGamalCiphertext ciphertextB = elGamalBalanceEncryptor.encrypt(amountB, publicKeyAsEcPoint, blindingFactorB);

    ElGamalCiphertext diffCiphertext = elGamalBalanceOperations.subtract(ciphertextA, ciphertextB);
    long decryptedDiff = javaElGamalBalanceDecryptor.decrypt(
      diffCiphertext, keyPair.privateKey().naturalBytes().toByteArray()
    );

    assertThat(decryptedDiff).isEqualTo(amountA.minus(amountB).longValue());
  }

  @Test
  void testZeroEncryption() {
    ECPoint publicKeyAsEcPoint = toPublicKey(keyPair.publicKey());
    byte[] blindingFactor = RandomnessUtils.generateRandomScalar(new SecureRandom(), secp256k1);
    UnsignedLong originalAmount = UnsignedLong.ZERO;

    ElGamalCiphertext ciphertext = elGamalBalanceEncryptor.encrypt(
      originalAmount, publicKeyAsEcPoint, blindingFactor
    );
    long decryptedAmount = javaElGamalBalanceDecryptor.decrypt(ciphertext,
      keyPair.privateKey().naturalBytes().toByteArray());

    assertThat(decryptedAmount).isEqualTo(originalAmount.longValue());
  }

  @Test
  void testCanonicalEncryptedZero() {
    ECPoint publicKeyAsEcPoint = toPublicKey(keyPair.publicKey());

    // Use placeholder byte arrays for IDs (20 bytes for account, 24 for issuance)
    byte[] accountId = new byte[20];
    accountId[0] = 1;
    byte[] issuanceId = new byte[24];
    issuanceId[0] = 2;

    // Generate it once
    ElGamalCiphertext ciphertextA = elGamalBalanceEncryptor.generateCanonicalEncryptedZero(
      publicKeyAsEcPoint, accountId, issuanceId
    );

    // Generate it a second time with the same inputs
    ElGamalCiphertext ciphertextB = elGamalBalanceEncryptor.generateCanonicalEncryptedZero(
      publicKeyAsEcPoint, accountId, issuanceId
    );

    // 1. Verify that it decrypts to zero
    long decryptedAmount = javaElGamalBalanceDecryptor.decrypt(ciphertextA,
      keyPair.privateKey().naturalBytes().toByteArray()
    );
    assertThat(decryptedAmount).isEqualTo(0);

    // 2. Verify that the output is deterministic (both ciphertexts are identical)
    assertThat(ciphertextA).isEqualTo(ciphertextB);
  }

  @Test
  void testMultipleHomomorphicOperations() {
    ECPoint publicKeyAsEcPoint = toPublicKey(keyPair.publicKey());

    // Encrypt several amounts
    UnsignedLong[] amounts = {UnsignedLong.valueOf(100), UnsignedLong.valueOf(200), UnsignedLong.valueOf(300),
      UnsignedLong.valueOf(50)};
    ElGamalCiphertext[] ciphertexts = new ElGamalCiphertext[amounts.length];

    for (int i = 0; i < amounts.length; i++) {
      byte[] blindingFactor = RandomnessUtils.generateRandomScalar(new SecureRandom(), secp256k1);
      ciphertexts[i] = elGamalBalanceEncryptor.encrypt(amounts[i], publicKeyAsEcPoint, blindingFactor);
    }

    // Sum all ciphertexts: 100 + 200 + 300 + 50 = 650
    ElGamalCiphertext sum = ciphertexts[0];
    for (int i = 1; i < ciphertexts.length; i++) {
      sum = elGamalBalanceOperations.add(sum, ciphertexts[i]);
    }

    long decryptedSum = javaElGamalBalanceDecryptor.decrypt(sum, keyPair.privateKey().naturalBytes().toByteArray());
    assertThat(decryptedSum).isEqualTo(100 + 200 + 300 + 50);
  }
}
