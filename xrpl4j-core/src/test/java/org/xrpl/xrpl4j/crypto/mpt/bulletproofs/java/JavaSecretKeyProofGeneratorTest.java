package org.xrpl.xrpl4j.crypto.mpt.bulletproofs.java;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.bouncycastle.math.ec.ECPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Passphrase;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.crypto.keys.bc.BcKeyUtils;
import org.xrpl.xrpl4j.crypto.mpt.BlindingFactor;
import org.xrpl.xrpl4j.crypto.mpt.Secp256k1Operations;
import org.xrpl.xrpl4j.crypto.mpt.context.ConfidentialMPTConvertContext;
import org.xrpl.xrpl4j.crypto.mpt.wrapper.SecretKeyProof;
import org.xrpl.xrpl4j.crypto.mpt.bulletproofs.SecretKeyProofGenerator;
import org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalPrivateKey;
import org.xrpl.xrpl4j.crypto.mpt.keys.ElGamalPublicKey;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.MpTokenIssuanceId;

/**
 * Unit tests for {@link JavaSecretKeyProofGenerator}.
 */
class JavaSecretKeyProofGeneratorTest {

  private SecretKeyProofGenerator<ElGamalPrivateKey> proofGenerator;

  @BeforeEach
  void setUp() {
    proofGenerator = new JavaSecretKeyProofGenerator();
  }

  /**
   * Deterministic test with hardcoded values for comparison with C implementation.
   *
   * <p>This test uses:
   * <ul>
   *   <li>Passphrase: "test_elgamal_key" to generate a deterministic secp256k1 keypair</li>
   *   <li>Account: rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh (genesis account)</li>
   *   <li>Sequence: 12345</li>
   *   <li>MPTokenIssuanceId: 00000143A58DCB491FD36A15A7D3172E6A9F088A5478BA41</li>
   *   <li>Amount: 500</li>
   *   <li>Nonce (k): 0x01, 0x02, 0x03, ... 0x20 (32 bytes)</li>
   * </ul>
   *
   * <p>Run this test and compare the output with the C code to verify correctness.</p>
   */
  @Test
  void testDeterministicContextGenerationForCComparison() {
    // 1. Generate deterministic keypair from passphrase (same as encryption test)
    Seed seed = Seed.secp256k1SeedFromPassphrase(Passphrase.of("test_elgamal_key"));
    KeyPair keypair = seed.deriveKeyPair();

    ElGamalPrivateKey privateKey = ElGamalPrivateKey.of(keypair.privateKey().naturalBytes());
    // Public key is derived from private key inside generateProof, but we still need it for verification
    ECPoint publicKeyPoint = BcKeyUtils.toEcPublicKeyParameters(keypair.publicKey()).getQ();
    ElGamalPublicKey publicKey = ElGamalPublicKey.fromEcPoint(publicKeyPoint);

    // 2. Fixed test values
    Address account = Address.of("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh");
    UnsignedInteger sequence = UnsignedInteger.valueOf(12345);
    MpTokenIssuanceId issuanceId = MpTokenIssuanceId.of("00000143A58DCB491FD36A15A7D3172E6A9F088A5478BA41");
    UnsignedLong amount = UnsignedLong.valueOf(500);

    // 3. Hardcoded nonce (k): 0x01, 0x02, 0x03, ... 0x20 (32 bytes)
    byte[] nonceBytes = new byte[32];
    for (int i = 0; i < 32; i++) {
      nonceBytes[i] = (byte) (i + 1);
    }
    BlindingFactor nonce = BlindingFactor.fromBytes(nonceBytes);

    // 4. Generate context hash using ConfidentialMPTConvertContext
    ConfidentialMPTConvertContext context = ConfidentialMPTConvertContext.generate(
      account, sequence, issuanceId, amount
    );

    // 5. Generate proof with deterministic nonce (public key derived internally)
    SecretKeyProof proof = proofGenerator.generateProof(privateKey, context, nonce);

    // 6. Print all values for C comparison
    System.out.println("\n========== SECRET KEY PROOF DETERMINISTIC TEST VALUES ==========");
    System.out.println("Passphrase: \"test_elgamal_key\"");
    System.out.println();

    System.out.println("=== Private Key (32 bytes) ===");
    System.out.println("Hex: " + BaseEncoding.base16().encode(privateKey.naturalBytes().toByteArray()));
    System.out.println();

    System.out.println("=== Public Key ===");
    byte[] pkCompressed = Secp256k1Operations.serializeCompressed(publicKeyPoint);
    System.out.println("Compressed (33 bytes): " + BaseEncoding.base16().encode(pkCompressed));

    // Uncompressed format (64 bytes, X || Y without 04 prefix)
    byte[] pkUncompressed = keypair.publicKey().uncompressedValue().toByteArray();
    System.out.println("Uncompressed (64 bytes): " + BaseEncoding.base16().encode(pkUncompressed));

    // Uncompressed reversed format (64 bytes, X_reversed || Y_reversed) for C compatibility
    byte[] pkUncompressedReversed = keypair.publicKey().uncompressedValueReversed().toByteArray();
    System.out.println("Uncompressed Reversed (64 bytes): " + BaseEncoding.base16().encode(pkUncompressedReversed));
    System.out.println();

    System.out.println("=== Nonce k (32 bytes) ===");
    System.out.println("Hex: " + nonce.hexValue());
    System.out.println();

    System.out.println("=== Context Generation Inputs ===");
    System.out.println("Transaction Type: 85 (ConfidentialMPTConvert)");
    System.out.println("Account: " + account.value());
    System.out.println("Sequence: " + sequence);
    System.out.println("MPTokenIssuanceId: " + issuanceId.value());
    System.out.println("Amount: " + amount);
    System.out.println();

    System.out.println("=== Context Hash (SHA512Half) ===");
    System.out.println("Context ID (32 bytes): " + context.hexValue());
    System.out.println();

    System.out.println("=== Proof (65 bytes) ===");
    byte[] proofBytes = proof.toBytes();
    byte[] T = new byte[33];
    byte[] s = new byte[32];
    System.arraycopy(proofBytes, 0, T, 0, 33);
    System.arraycopy(proofBytes, 33, s, 0, 32);
    System.out.println("T (33 bytes): " + BaseEncoding.base16().encode(T));
    System.out.println("s (32 bytes): " + BaseEncoding.base16().encode(s));
    System.out.println("Full proof:   " + proof.hexValue());
    System.out.println("================================================================\n");

    // 7. Assertions
    assertThat(context.toBytes()).hasSize(32);
    assertThat(proof.toBytes()).hasSize(65);
    assertThat(proofGenerator.verifyProof(proof, publicKey, context)).isTrue();
  }
}

