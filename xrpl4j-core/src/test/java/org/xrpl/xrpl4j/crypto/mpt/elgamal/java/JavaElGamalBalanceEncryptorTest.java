package org.xrpl.xrpl4j.crypto.mpt.elgamal.java;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.crypto.keys.bc.BcKeyUtils;
import org.xrpl.xrpl4j.crypto.mpt.RandomnessUtils;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalCiphertext;
import org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalPrivateKey;

import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Unit tests for {@link JavaElGamalBalanceEncryptor}.
 */
public class JavaElGamalBalanceEncryptorTest extends AbstractElGamalTest {

  private JavaElGamalBalanceDecryptor elGamalBalanceDecryptor;
  private Secp256k1Operations secp256k1;

  private JavaElGamalBalanceEncryptor elGamalBalanceEncryptor;

  @BeforeEach
  void setUp() {
    this.secp256k1 = new Secp256k1Operations();
    this.elGamalBalanceEncryptor = new JavaElGamalBalanceEncryptor(secp256k1);
    this.elGamalBalanceDecryptor = new JavaElGamalBalanceDecryptor(secp256k1);
  }

  @Test
  void deleteMe() {

    KeyPair keypair = Seed.secp256k1Seed().deriveKeyPair();
    ECPoint publicKey = BcKeyUtils.toEcPublicKeyParameters(keypair.publicKey()).getQ();
//        ECPoint publicKeyAsEcPoint = toPublicKey(keyPair.publicKey());
    byte[] blindingFactor = RandomnessUtils.generateRandomScalar(new SecureRandom(), secp256k1);

    ElGamalCiphertext ciphertext = elGamalBalanceEncryptor.encrypt(
      UnsignedLong.valueOf(12345), publicKey, blindingFactor
    );
  }

  /**
   * Deterministic test with hardcoded values for comparison with C implementation.
   *
   * <p>This test uses:
   * <ul>
   *   <li>Passphrase: "test_elgamal_key" to generate a deterministic secp256k1 keypair</li>
   *   <li>Blinding factor: 32 bytes of 0x01, 0x02, 0x03, ... 0x20</li>
   *   <li>Amount: 500</li>
   * </ul>
   *
   * <p>Run this test and compare the output with the C code to verify correctness.</p>
   */
  @Test
  void testDeterministicEncryptionForCComparison() {
    // 1. Generate deterministic keypair from passphrase
    Seed seed = Seed.secp256k1SeedFromPassphrase(Passphrase.of("test_elgamal_key"));
    KeyPair keypair = seed.deriveKeyPair();

    // Get private key bytes (32 bytes)
    byte[] privateKeyBytes = keypair.privateKey().naturalBytes().toByteArray();

    // Get public key as EC point
    ECPoint publicKey = BcKeyUtils.toEcPublicKeyParameters(keypair.publicKey()).getQ();

    // 2. Hardcoded blinding factor: 0x01, 0x02, 0x03, ... 0x20 (32 bytes)
    byte[] blindingFactor = new byte[32];
    for (int i = 0; i < 32; i++) {
      blindingFactor[i] = (byte) (i + 1);
    }

    // 3. Amount to encrypt
    UnsignedLong amount = UnsignedLong.valueOf(0);

    // 4. Encrypt
    ElGamalCiphertext ciphertext = elGamalBalanceEncryptor.encrypt(amount, publicKey, blindingFactor);

    // 5. Print all values for C comparison
    System.out.println("\n========== DETERMINISTIC TEST VALUES FOR C COMPARISON ==========");
    System.out.println("Passphrase: \"test_elgamal_key\"");
    System.out.println("Amount: " + amount);
    System.out.println();

    System.out.println("=== Private Key (32 bytes) ===");
    System.out.println("Bytes: " + Arrays.toString(privateKeyBytes));
    System.out.println("Hex: " + BaseEncoding.base16().encode(privateKeyBytes));
    System.out.println("Length: " + privateKeyBytes.length);
    System.out.println();

    System.out.println("=== Public Key ===");
    byte[] pkCompressed = secp256k1.serializeCompressed(publicKey);
    byte[] pkUncompressed = publicKey.getEncoded(false); // false = uncompressed (65 bytes with 04 prefix)
    // Get 64 bytes without prefix by slicing off the first byte
    byte[] pkUncompressedNoPrefix = new byte[64];
    System.arraycopy(pkUncompressed, 1, pkUncompressedNoPrefix, 0, 64);
    System.out.println("Compressed (33 bytes): " + BaseEncoding.base16().encode(pkCompressed));
    System.out.println("Uncompressed (65 bytes): " + BaseEncoding.base16().encode(pkUncompressed));
    System.out.println("Uncompressed no prefix (64 bytes): " + BaseEncoding.base16().encode(pkUncompressedNoPrefix));
    System.out.println();

    System.out.println("=== Blinding Factor (32 bytes) ===");
    System.out.println("Hex: " + BaseEncoding.base16().encode(blindingFactor));
    System.out.println();

    System.out.println("=== Ciphertext ===");
    byte[] c1Bytes = secp256k1.serializeCompressed(ciphertext.c1());
    byte[] c2Bytes = secp256k1.serializeCompressed(ciphertext.c2());
    byte[] fullCiphertext = ciphertext.toBytes();
    System.out.println("C1 (33 bytes compressed): " + BaseEncoding.base16().encode(c1Bytes));
    System.out.println("C2 (33 bytes compressed): " + BaseEncoding.base16().encode(c2Bytes));
    String fullCipherText = BaseEncoding.base16().encode(fullCiphertext);
    System.out.println("Full ciphertext (66 bytes): " + fullCipherText);

    // compare with output from C implementation (mpt-crypto)
    assertThat(fullCipherText)
      .isEqualTo(
        "0284BF7562262BBD6940085748F3BE6AFA52AE317155181ECE31B66351CCFFA4B002917984BCB834D86CDB127A740BE9D65E34F2EA9E659FD86547A1403CC5CA670D");
    System.out.println();

    // 6. Verify decryption works
    ElGamalPrivateKey elGamalPrivateKey = ElGamalPrivateKey.of(keypair.privateKey().naturalBytes());
    long decryptedAmount = elGamalBalanceDecryptor.decrypt(ciphertext, elGamalPrivateKey);
    System.out.println("=== Verification ===");
    System.out.println("Decrypted amount: " + decryptedAmount);
    System.out.println("Decryption matches: " + (decryptedAmount == amount.longValue()));
    System.out.println("================================================================\n");

    // Assertions
    assertThat(decryptedAmount).isEqualTo(amount.longValue());
    assertThat(fullCiphertext).hasSize(66);
  }


  @Test
  void testEncryptionSmokeTest() {
    ECPoint publicKeyAsEcPoint = toPublicKey(keyPair.publicKey());
    byte[] blindingFactor = RandomnessUtils.generateRandomScalar(new SecureRandom(), secp256k1);

    ElGamalCiphertext ciphertext = elGamalBalanceEncryptor.encrypt(UnsignedLong.valueOf(12345), publicKeyAsEcPoint,
      blindingFactor);

    assertThat(ciphertext).isNotNull();
    assertThat(ciphertext.c1()).isNotNull();
    assertThat(ciphertext.c2()).isNotNull();
    assertThat(ciphertext.c1().isInfinity()).isFalse();
    assertThat(ciphertext.c2().isInfinity()).isFalse();
  }

  @Test
  void testEncryptionDecryptionRoundTrip() {
    ECPoint publicKeyAsEcPoint = toPublicKey(keyPair.publicKey());
    byte[] blindingFactor = RandomnessUtils.generateRandomScalar(new SecureRandom(), secp256k1);
    UnsignedLong originalAmount = UnsignedLong.valueOf(10001);
    ElGamalPrivateKey privateKey = ElGamalPrivateKey.of(keyPair.privateKey().naturalBytes());

    ElGamalCiphertext ciphertext = elGamalBalanceEncryptor.encrypt(originalAmount, publicKeyAsEcPoint, blindingFactor);
    long decryptedAmount = elGamalBalanceDecryptor.decrypt(ciphertext, privateKey);

    assertThat(decryptedAmount).isEqualTo(originalAmount.longValue());
  }

  @Test
  void testZeroEncryption() {
    ECPoint publicKeyAsEcPoint = toPublicKey(keyPair.publicKey());
    byte[] blindingFactor = RandomnessUtils.generateRandomScalar(new SecureRandom(), secp256k1);
    UnsignedLong originalAmount = UnsignedLong.ZERO;
    ElGamalPrivateKey privateKey = ElGamalPrivateKey.of(keyPair.privateKey().naturalBytes());

    ElGamalCiphertext ciphertext = elGamalBalanceEncryptor.encrypt(
      originalAmount, publicKeyAsEcPoint, blindingFactor
    );
    long decryptedAmount = elGamalBalanceDecryptor.decrypt(ciphertext, privateKey);

    assertThat(decryptedAmount).isEqualTo(originalAmount.longValue());
  }

  @Test
  void testCanonicalEncryptedZero() {
    ECPoint publicKeyAsEcPoint = toPublicKey(keyPair.publicKey());
    ElGamalPrivateKey privateKey = ElGamalPrivateKey.of(keyPair.privateKey().naturalBytes());

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
    long decryptedAmount = elGamalBalanceDecryptor.decrypt(ciphertextA, privateKey);
    assertThat(decryptedAmount).isEqualTo(0);

    // 2. Verify that the output is deterministic (both ciphertexts are identical)
    assertThat(ciphertextA).isEqualTo(ciphertextB);
  }

  @Test
  void testVerifyEncryptionValidCase() {
    ECPoint publicKeyAsEcPoint = toPublicKey(keyPair.publicKey());
    byte[] blindingFactor = RandomnessUtils.generateRandomScalar(new SecureRandom(), secp256k1);
    UnsignedLong amount = UnsignedLong.valueOf(12345);

    ElGamalCiphertext ciphertext = elGamalBalanceEncryptor.encrypt(amount, publicKeyAsEcPoint, blindingFactor);

    boolean isValid = elGamalBalanceEncryptor.verifyEncryption(ciphertext, publicKeyAsEcPoint, amount, blindingFactor);
    assertThat(isValid).isTrue();
  }

  @Test
  void testVerifyEncryptionInvalidAmount() {
    ECPoint publicKeyAsEcPoint = toPublicKey(keyPair.publicKey());
    byte[] blindingFactor = RandomnessUtils.generateRandomScalar(new SecureRandom(), secp256k1);
    UnsignedLong amount = UnsignedLong.valueOf(12345);
    UnsignedLong badAmount = UnsignedLong.valueOf(54321);

    ElGamalCiphertext ciphertext = elGamalBalanceEncryptor.encrypt(amount, publicKeyAsEcPoint, blindingFactor);

    boolean isValid = elGamalBalanceEncryptor.verifyEncryption(ciphertext, publicKeyAsEcPoint, badAmount,
      blindingFactor);
    assertThat(isValid).isFalse();
  }

  @Test
  void testVerifyEncryptionInvalidBlindingFactor() {
    ECPoint publicKeyAsEcPoint = toPublicKey(keyPair.publicKey());
    byte[] blindingFactor = RandomnessUtils.generateRandomScalar(new SecureRandom(), secp256k1);
    UnsignedLong amount = UnsignedLong.valueOf(12345);

    // Create a bad blinding factor by flipping bits
    byte[] badBlindingFactor = Arrays.copyOf(blindingFactor, blindingFactor.length);
    badBlindingFactor[31] ^= (byte) 0xFF;

    ElGamalCiphertext ciphertext = elGamalBalanceEncryptor.encrypt(amount, publicKeyAsEcPoint, blindingFactor);

    boolean isValid = elGamalBalanceEncryptor.verifyEncryption(ciphertext, publicKeyAsEcPoint, amount,
      badBlindingFactor);
    assertThat(isValid).isFalse();
  }

  @Test
  void testCiphertextSerialization() {
    ECPoint publicKeyAsEcPoint = toPublicKey(keyPair.publicKey());
    byte[] blindingFactor = RandomnessUtils.generateRandomScalar(new SecureRandom(), secp256k1);
    UnsignedLong amount = UnsignedLong.valueOf(42);
    ElGamalPrivateKey privateKey = ElGamalPrivateKey.of(keyPair.privateKey().naturalBytes());

    ElGamalCiphertext original = elGamalBalanceEncryptor.encrypt(amount, publicKeyAsEcPoint, blindingFactor);

    // Serialize and deserialize
    byte[] serialized = original.toBytes();
    assertThat(serialized).hasSize(66); // 33 + 33 bytes

    ElGamalCiphertext deserialized = ElGamalCiphertext.fromBytes(serialized, secp256k1);

    assertThat(deserialized).isEqualTo(original);

    // Verify the deserialized ciphertext still decrypts correctly
    long decryptedAmount = elGamalBalanceDecryptor.decrypt(deserialized, privateKey);
    assertThat(decryptedAmount).isEqualTo(amount.longValue());
  }

  @Test
  void testCanonicalZeroDifferentInputsProduceDifferentOutputs() {
    ECPoint publicKeyAsEcPoint = toPublicKey(keyPair.publicKey());
    ElGamalPrivateKey privateKey = ElGamalPrivateKey.of(keyPair.privateKey().naturalBytes());

    byte[] accountId1 = new byte[20];
    accountId1[0] = 1;
    byte[] accountId2 = new byte[20];
    accountId2[0] = 2;
    byte[] issuanceId = new byte[24];

    ElGamalCiphertext ciphertext1 = elGamalBalanceEncryptor.generateCanonicalEncryptedZero(
      publicKeyAsEcPoint, accountId1, issuanceId
    );
    ElGamalCiphertext ciphertext2 = elGamalBalanceEncryptor.generateCanonicalEncryptedZero(
      publicKeyAsEcPoint, accountId2, issuanceId
    );

    // Different inputs should produce different ciphertexts
    assertThat(ciphertext1).isNotEqualTo(ciphertext2);

    // But both should still decrypt to zero
    assertThat(elGamalBalanceDecryptor.decrypt(ciphertext1, privateKey)).isEqualTo(0);
    assertThat(elGamalBalanceDecryptor.decrypt(ciphertext2, privateKey)).isEqualTo(0);
  }
}
