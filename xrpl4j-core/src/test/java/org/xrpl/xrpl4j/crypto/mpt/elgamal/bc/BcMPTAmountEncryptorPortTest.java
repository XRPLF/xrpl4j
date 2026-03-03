package org.xrpl.xrpl4j.crypto.mpt.elgamal.bc;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.crypto.mpt.BlindingFactor;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.elgamal.ElGamalCiphertext;

import java.util.Arrays;

/**
 * Unit tests for {@link BcElGamalEncryptor}.
 */
public class BcMPTAmountEncryptorPortTest extends AbstractElGamalTest {

  private BcElGamalDecryptor elGamalBalanceDecryptor;

  private BcElGamalEncryptor elGamalBalanceEncryptor;

  private BcElGamalEncryptionVerifier elGamalBalanceEncryptionVerifier;

  @BeforeEach
  void setUp() {
    this.elGamalBalanceEncryptor = new BcElGamalEncryptor();
    this.elGamalBalanceEncryptionVerifier = new BcElGamalEncryptionVerifier();
    this.elGamalBalanceDecryptor = new BcElGamalDecryptor();
  }

  @Test
  void deleteMe() {
    KeyPair keypair = Seed.elGamalSecp256k1Seed().deriveKeyPair();
    PublicKey publicKey = keypair.publicKey();
    BlindingFactor blindingFactor = BlindingFactor.generate();

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
    Seed seed = Seed.elGamalSecp256k1SeedFromPassphrase(Passphrase.of("test_elgamal_key"));
    KeyPair keypair = seed.deriveKeyPair();

    // Get private key bytes (32 bytes)
    byte[] privateKeyBytes = keypair.privateKey().naturalBytes().toByteArray();

    // Get public key
    PublicKey publicKey = keypair.publicKey();
    ECPoint publicKeyPoint = Secp256k1Operations.toEcPoint(publicKey);

    // 2. Hardcoded blinding factor: 0x01, 0x02, 0x03, ... 0x20 (32 bytes)
    byte[] blindingFactorBytes = new byte[32];
    for (int i = 0; i < 32; i++) {
      blindingFactorBytes[i] = (byte) (i + 1);
    }
    BlindingFactor blindingFactor = BlindingFactor.fromBytes(blindingFactorBytes);

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
    byte[] pkCompressed = Secp256k1Operations.serializeCompressed(publicKeyPoint);
    byte[] pkUncompressed = publicKeyPoint.getEncoded(false); // false = uncompressed (65 bytes with 04 prefix)
    // Get 64 bytes without prefix by slicing off the first byte
    byte[] pkUncompressedNoPrefix = new byte[64];
    System.arraycopy(pkUncompressed, 1, pkUncompressedNoPrefix, 0, 64);
    System.out.println("Compressed (33 bytes): " + BaseEncoding.base16().encode(pkCompressed));
    System.out.println("Uncompressed (65 bytes): " + BaseEncoding.base16().encode(pkUncompressed));
    System.out.println("Uncompressed no prefix (64 bytes): " + BaseEncoding.base16().encode(pkUncompressedNoPrefix));
    System.out.println();

    System.out.println("=== Blinding Factor (32 bytes) ===");
    System.out.println("Hex: " + blindingFactor.hexValue());
    System.out.println();

    System.out.println("=== Ciphertext ===");
    byte[] c1Bytes = Secp256k1Operations.serializeCompressed(ciphertext.c1());
    byte[] c2Bytes = Secp256k1Operations.serializeCompressed(ciphertext.c2());
    byte[] fullCiphertext = ciphertext.toBytes();
    System.out.println("C1 (33 bytes compressed): " + BaseEncoding.base16().encode(c1Bytes));
    System.out.println("C2 (33 bytes compressed): " + BaseEncoding.base16().encode(c2Bytes));
    String fullCipherText = BaseEncoding.base16().encode(fullCiphertext);
    System.out.println("Full ciphertext (66 bytes): " + fullCipherText);

    // compare with output from C implementation (mpt-crypto)
    // Note: The expected value depends on the key derivation method used.
    // With elGamalSecp256k1SeedFromPassphrase (32-byte entropy from SHA-512), the expected value is:
    assertThat(fullCipherText)
      .isEqualTo(
        "0284BF7562262BBD6940085748F3BE6AFA52AE317155181ECE31B66351CCFFA4B0020D93E59C5FEE8E5A66FA1CB5B7E2C5F2955075A521B59E9A1ABE192EF1B0F18A");
    System.out.println();

    // 6. Verify decryption works
    PrivateKey privateKey = keypair.privateKey();
    long decryptedAmount = elGamalBalanceDecryptor.decrypt(ciphertext, privateKey, 0, 1_000_000);
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
    PublicKey publicKey = keyPair.publicKey();
    BlindingFactor blindingFactor = BlindingFactor.generate();

    ElGamalCiphertext ciphertext = elGamalBalanceEncryptor.encrypt(
      UnsignedLong.valueOf(12345), publicKey, blindingFactor
    );

    assertThat(ciphertext).isNotNull();
    assertThat(ciphertext.c1()).isNotNull();
    assertThat(ciphertext.c2()).isNotNull();
    assertThat(ciphertext.c1().isInfinity()).isFalse();
    assertThat(ciphertext.c2().isInfinity()).isFalse();
  }

  @Test
  void testEncryptionDecryptionRoundTrip() {
    PublicKey publicKey = keyPair.publicKey();
    BlindingFactor blindingFactor = BlindingFactor.generate();
    UnsignedLong originalAmount = UnsignedLong.valueOf(10001);
    PrivateKey privateKey = keyPair.privateKey();

    ElGamalCiphertext ciphertext = elGamalBalanceEncryptor.encrypt(originalAmount, publicKey, blindingFactor);
    long decryptedAmount = elGamalBalanceDecryptor.decrypt(ciphertext, privateKey, 0, 1_000_000);

    assertThat(decryptedAmount).isEqualTo(originalAmount.longValue());
  }

  @Test
  void testZeroEncryption() {
    PublicKey publicKey = keyPair.publicKey();
    BlindingFactor blindingFactor = BlindingFactor.generate();
    UnsignedLong originalAmount = UnsignedLong.ZERO;
    PrivateKey privateKey = keyPair.privateKey();

    ElGamalCiphertext ciphertext = elGamalBalanceEncryptor.encrypt(
      originalAmount, publicKey, blindingFactor
    );
    long decryptedAmount = elGamalBalanceDecryptor.decrypt(ciphertext, privateKey, 0, 1_000_000);

    assertThat(decryptedAmount).isEqualTo(originalAmount.longValue());
  }


  @Test
  void testVerifyEncryptionValidCase() {
    PublicKey publicKey = keyPair.publicKey();
    BlindingFactor blindingFactor = BlindingFactor.generate();
    UnsignedLong amount = UnsignedLong.valueOf(12345);

    ElGamalCiphertext ciphertext = elGamalBalanceEncryptor.encrypt(amount, publicKey, blindingFactor);

    boolean isValid = elGamalBalanceEncryptionVerifier.verifyEncryption(ciphertext, publicKey, amount, blindingFactor);
    assertThat(isValid).isTrue();
  }

  @Test
  void testVerifyEncryptionInvalidAmount() {
    PublicKey publicKey = keyPair.publicKey();
    BlindingFactor blindingFactor = BlindingFactor.generate();
    UnsignedLong amount = UnsignedLong.valueOf(12345);
    UnsignedLong badAmount = UnsignedLong.valueOf(54321);

    ElGamalCiphertext ciphertext = elGamalBalanceEncryptor.encrypt(amount, publicKey, blindingFactor);

    boolean isValid = elGamalBalanceEncryptionVerifier.verifyEncryption(ciphertext, publicKey, badAmount, blindingFactor);
    assertThat(isValid).isFalse();
  }

  @Test
  void testVerifyEncryptionInvalidBlindingFactor() {
    PublicKey publicKey = keyPair.publicKey();
    BlindingFactor blindingFactor = BlindingFactor.generate();
    UnsignedLong amount = UnsignedLong.valueOf(12345);

    // Create a different blinding factor
    BlindingFactor badBlindingFactor = BlindingFactor.generate();

    ElGamalCiphertext ciphertext = elGamalBalanceEncryptor.encrypt(amount, publicKey, blindingFactor);

    boolean isValid = elGamalBalanceEncryptionVerifier.verifyEncryption(ciphertext, publicKey, amount, badBlindingFactor);
    assertThat(isValid).isFalse();
  }

  @Test
  void testCiphertextSerialization() {
    PublicKey publicKey = keyPair.publicKey();
    BlindingFactor blindingFactor = BlindingFactor.generate();
    UnsignedLong amount = UnsignedLong.valueOf(42);
    PrivateKey privateKey = keyPair.privateKey();

    ElGamalCiphertext original = elGamalBalanceEncryptor.encrypt(amount, publicKey, blindingFactor);

    // Serialize and deserialize
    byte[] serialized = original.toBytes();
    assertThat(serialized).hasSize(66); // 33 + 33 bytes

    ElGamalCiphertext deserialized = ElGamalCiphertext.fromBytes(serialized);

    assertThat(deserialized).isEqualTo(original);

    // Verify the deserialized ciphertext still decrypts correctly
    long decryptedAmount = elGamalBalanceDecryptor.decrypt(deserialized, privateKey, 0, 1_000_000);
    assertThat(decryptedAmount).isEqualTo(amount.longValue());
  }


}
