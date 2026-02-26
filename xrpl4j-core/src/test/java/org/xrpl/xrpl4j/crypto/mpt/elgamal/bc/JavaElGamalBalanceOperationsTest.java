package org.xrpl.xrpl4j.crypto.mpt.elgamal.bc;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.mpt.BlindingFactor;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalEncryptor;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalCiphertext;

/**
 * Unit tests for {@link JavaElGamalBalanceOperations}.
 */
public class JavaElGamalBalanceOperationsTest extends AbstractElGamalTest {

  private ElGamalEncryptor elGamalEncryptor;
  private BcElGamalDecryptor javaElGamalBalanceDecryptor;

  private JavaElGamalBalanceOperations elGamalBalanceOperations;

  @BeforeEach
  void setUp() {
    this.elGamalEncryptor = new BcElGamalEncryptor();
    this.javaElGamalBalanceDecryptor = new BcElGamalDecryptor();
    this.elGamalBalanceOperations = new JavaElGamalBalanceOperations();
  }

  @Test
  void testHomomorphicAddition() {
    PublicKey publicKey = keyPair.publicKey();
    PrivateKey privateKey = keyPair.privateKey();
    UnsignedLong amountA = UnsignedLong.valueOf(5000);
    UnsignedLong amountB = UnsignedLong.valueOf(1234);

    BlindingFactor blindingFactorA = BlindingFactor.generate();
    BlindingFactor blindingFactorB = BlindingFactor.generate();

    ElGamalCiphertext ciphertextA = elGamalEncryptor.encrypt(amountA, publicKey, blindingFactorA);
    ElGamalCiphertext ciphertextB = elGamalEncryptor.encrypt(amountB, publicKey, blindingFactorB);

    ElGamalCiphertext sumCiphertext = elGamalBalanceOperations.add(ciphertextA, ciphertextB);
    long decryptedSum = javaElGamalBalanceDecryptor.decrypt(sumCiphertext, privateKey, 0, 1_000_000);

    assertThat(decryptedSum).isEqualTo(amountA.plus(amountB).longValue());
  }

  @Test
  void testHomomorphicSubtraction() {
    PublicKey publicKey = keyPair.publicKey();
    PrivateKey privateKey = keyPair.privateKey();
    UnsignedLong amountA = UnsignedLong.valueOf(5000);
    UnsignedLong amountB = UnsignedLong.valueOf(1234);

    BlindingFactor blindingFactorA = BlindingFactor.generate();
    BlindingFactor blindingFactorB = BlindingFactor.generate();

    ElGamalCiphertext ciphertextA = elGamalEncryptor.encrypt(amountA, publicKey, blindingFactorA);
    ElGamalCiphertext ciphertextB = elGamalEncryptor.encrypt(amountB, publicKey, blindingFactorB);

    ElGamalCiphertext diffCiphertext = elGamalBalanceOperations.subtract(ciphertextA, ciphertextB);
    long decryptedDiff = javaElGamalBalanceDecryptor.decrypt(diffCiphertext, privateKey, 0, 1_000_000);

    assertThat(decryptedDiff).isEqualTo(amountA.minus(amountB).longValue());
  }

  @Test
  void testZeroEncryption() {
    PublicKey publicKey = keyPair.publicKey();
    PrivateKey privateKey = keyPair.privateKey();
    BlindingFactor blindingFactor = BlindingFactor.generate();
    UnsignedLong originalAmount = UnsignedLong.ZERO;

    ElGamalCiphertext ciphertext = elGamalEncryptor.encrypt(
      originalAmount, publicKey, blindingFactor
    );
    long decryptedAmount = javaElGamalBalanceDecryptor.decrypt(ciphertext, privateKey, 0, 1_000_000);

    assertThat(decryptedAmount).isEqualTo(originalAmount.longValue());
  }

  @Test
  void testMultipleHomomorphicOperations() {
    PublicKey publicKey = keyPair.publicKey();
    PrivateKey privateKey = keyPair.privateKey();

    // Encrypt several amounts
    UnsignedLong[] amounts = {UnsignedLong.valueOf(100), UnsignedLong.valueOf(200), UnsignedLong.valueOf(300),
      UnsignedLong.valueOf(50)};
    ElGamalCiphertext[] ciphertexts = new ElGamalCiphertext[amounts.length];

    for (int i = 0; i < amounts.length; i++) {
      BlindingFactor blindingFactor = BlindingFactor.generate();
      ciphertexts[i] = elGamalEncryptor.encrypt(amounts[i], publicKey, blindingFactor);
    }

    // Sum all ciphertexts: 100 + 200 + 300 + 50 = 650
    ElGamalCiphertext sum = ciphertexts[0];
    for (int i = 1; i < ciphertexts.length; i++) {
      sum = elGamalBalanceOperations.add(sum, ciphertexts[i]);
    }

    long decryptedSum = javaElGamalBalanceDecryptor.decrypt(sum, privateKey, 0, 1_000_000);
    assertThat(decryptedSum).isEqualTo(100 + 200 + 300 + 50);
  }
}
