package org.xrpl.xrpl4j.crypto.mpt.bulletproofs;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link PedersenCommitmentGenerator}.
 *
 * <p>These tests use deterministic inputs to verify compatibility with C implementation.</p>
 */
class PedersenCommitmentGeneratorTest {

  private static final BaseEncoding HEX = BaseEncoding.base16().upperCase();

  private PedersenCommitmentGenerator generator;

  @BeforeEach
  void setUp() {
    generator = new PedersenCommitmentGenerator();
  }

  /**
   * Deterministic test: Commitment with fixed inputs should match C implementation.
   *
   * <p>To verify against C code, run:
   * <pre>
   * unsigned char rho[32];
   * memset(rho, 0x01, 32);
   * secp256k1_pubkey commitment;
   * secp256k1_mpt_pedersen_commit(ctx, &commitment, 1000, rho);
   * // Serialize and print
   * </pre>
   * </p>
   */
  @Test
  void testGenerateCommitmentDeterministic() {
    UnsignedLong amount = UnsignedLong.valueOf(1000);

    // Fixed blinding factor: hardcoded random value
    byte[] rho = HEX.decode("A4F5C7E9B2D4168F0E7A9C3B5D2F8E1A4C6B8D0F2E4A6C8B0D2F4E6A8C0B2D4F");

    // Print inputs
    System.out.println("Amount: " + amount);
    System.out.println("Rho (blinding factor): " + HEX.encode(rho));

    byte[] commitment = generator.generateCommitment(amount, rho);
    String commitmentHex = HEX.encode(commitment);

    // Print output
    System.out.println("Commitment: " + commitmentHex);

    // Verify output matches expected value
    String expectedCommitmentHex = "02FD5403A3B2339D2D364B621D2A148D309656A80503F9DEE9DB8AB0132C53DEEC";
    assertThat(commitmentHex).isEqualTo(expectedCommitmentHex);
  }
}

