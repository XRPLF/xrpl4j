package org.xrpl.xrpl4j.crypto.mpt.elgamal.java;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.mpt.BlindingFactor;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalBalanceEncryptor;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalCiphertext;
import org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalPrivateKey;
import org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalPublicKey;

/**
 * Unit tests for {@link JavaElGamalBalanceOperations}.
 */
public class JavaElGamalBalanceOperationsTest extends AbstractElGamalTest {

  private ElGamalBalanceEncryptor elGamalBalanceEncryptor;
  private JavaElGamalBalanceDecryptor javaElGamalBalanceDecryptor;

  private JavaElGamalBalanceOperations elGamalBalanceOperations;

  @BeforeEach
  void setUp() {
    this.elGamalBalanceEncryptor = new JavaElGamalBalanceEncryptor();
    this.javaElGamalBalanceDecryptor = new JavaElGamalBalanceDecryptor();
    this.elGamalBalanceOperations = new JavaElGamalBalanceOperations();
  }

  @Test
  void testHomomorphicAddition() {
    ElGamalPublicKey publicKey = toElGamalPublicKey(keyPair.publicKey());
    ElGamalPrivateKey privateKey = ElGamalPrivateKey.of(keyPair.privateKey().naturalBytes());
    UnsignedLong amountA = UnsignedLong.valueOf(5000);
    UnsignedLong amountB = UnsignedLong.valueOf(1234);

    BlindingFactor blindingFactorA = BlindingFactor.generate();
    BlindingFactor blindingFactorB = BlindingFactor.generate();

    ElGamalCiphertext ciphertextA = elGamalBalanceEncryptor.encrypt(amountA, publicKey, blindingFactorA);
    ElGamalCiphertext ciphertextB = elGamalBalanceEncryptor.encrypt(amountB, publicKey, blindingFactorB);

    ElGamalCiphertext sumCiphertext = elGamalBalanceOperations.add(ciphertextA, ciphertextB);
    long decryptedSum = javaElGamalBalanceDecryptor.decrypt(sumCiphertext, privateKey);

    assertThat(decryptedSum).isEqualTo(amountA.plus(amountB).longValue());
  }

  @Test
  void testHomomorphicSubtraction() {
    ElGamalPublicKey publicKey = toElGamalPublicKey(keyPair.publicKey());
    ElGamalPrivateKey privateKey = ElGamalPrivateKey.of(keyPair.privateKey().naturalBytes());
    UnsignedLong amountA = UnsignedLong.valueOf(5000);
    UnsignedLong amountB = UnsignedLong.valueOf(1234);

    BlindingFactor blindingFactorA = BlindingFactor.generate();
    BlindingFactor blindingFactorB = BlindingFactor.generate();

    ElGamalCiphertext ciphertextA = elGamalBalanceEncryptor.encrypt(amountA, publicKey, blindingFactorA);
    ElGamalCiphertext ciphertextB = elGamalBalanceEncryptor.encrypt(amountB, publicKey, blindingFactorB);

    ElGamalCiphertext diffCiphertext = elGamalBalanceOperations.subtract(ciphertextA, ciphertextB);
    long decryptedDiff = javaElGamalBalanceDecryptor.decrypt(diffCiphertext, privateKey);

    assertThat(decryptedDiff).isEqualTo(amountA.minus(amountB).longValue());
  }

  @Test
  void testZeroEncryption() {
    ElGamalPublicKey publicKey = toElGamalPublicKey(keyPair.publicKey());
    ElGamalPrivateKey privateKey = ElGamalPrivateKey.of(keyPair.privateKey().naturalBytes());
    BlindingFactor blindingFactor = BlindingFactor.generate();
    UnsignedLong originalAmount = UnsignedLong.ZERO;

    ElGamalCiphertext ciphertext = elGamalBalanceEncryptor.encrypt(
      originalAmount, publicKey, blindingFactor
    );
    long decryptedAmount = javaElGamalBalanceDecryptor.decrypt(ciphertext, privateKey);

    assertThat(decryptedAmount).isEqualTo(originalAmount.longValue());
  }

  @Test
  void testCanonicalEncryptedZero() {
    ElGamalPublicKey publicKey = toElGamalPublicKey(keyPair.publicKey());
    ElGamalPrivateKey privateKey = ElGamalPrivateKey.of(keyPair.privateKey().naturalBytes());

    // Use placeholder byte arrays for IDs (20 bytes for account, 24 for issuance)
    byte[] accountId = new byte[20];
    accountId[0] = 1;
    byte[] issuanceId = new byte[24];
    issuanceId[0] = 2;

    // Generate it once
    ElGamalCiphertext ciphertextA = elGamalBalanceEncryptor.generateCanonicalEncryptedZero(
      publicKey, accountId, issuanceId
    );

    // Generate it a second time with the same inputs
    ElGamalCiphertext ciphertextB = elGamalBalanceEncryptor.generateCanonicalEncryptedZero(
      publicKey, accountId, issuanceId
    );

    // 1. Verify that it decrypts to zero
    long decryptedAmount = javaElGamalBalanceDecryptor.decrypt(ciphertextA, privateKey);
    assertThat(decryptedAmount).isEqualTo(0);

    // 2. Verify that the output is deterministic (both ciphertexts are identical)
    assertThat(ciphertextA).isEqualTo(ciphertextB);
  }

  @Test
  void testMultipleHomomorphicOperations() {
    ElGamalPublicKey publicKey = toElGamalPublicKey(keyPair.publicKey());
    ElGamalPrivateKey privateKey = ElGamalPrivateKey.of(keyPair.privateKey().naturalBytes());

    // Encrypt several amounts
    UnsignedLong[] amounts = {UnsignedLong.valueOf(100), UnsignedLong.valueOf(200), UnsignedLong.valueOf(300),
      UnsignedLong.valueOf(50)};
    ElGamalCiphertext[] ciphertexts = new ElGamalCiphertext[amounts.length];

    for (int i = 0; i < amounts.length; i++) {
      BlindingFactor blindingFactor = BlindingFactor.generate();
      ciphertexts[i] = elGamalBalanceEncryptor.encrypt(amounts[i], publicKey, blindingFactor);
    }

    // Sum all ciphertexts: 100 + 200 + 300 + 50 = 650
    ElGamalCiphertext sum = ciphertexts[0];
    for (int i = 1; i < ciphertexts.length; i++) {
      sum = elGamalBalanceOperations.add(sum, ciphertexts[i]);
    }

    long decryptedSum = javaElGamalBalanceDecryptor.decrypt(sum, privateKey);
    assertThat(decryptedSum).isEqualTo(100 + 200 + 300 + 50);
  }
}
