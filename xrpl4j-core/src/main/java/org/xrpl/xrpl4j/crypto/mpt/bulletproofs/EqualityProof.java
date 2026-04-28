package org.xrpl.xrpl4j.crypto.mpt.bulletproofs;

import org.bouncycastle.math.ec.ECPoint;
import org.immutables.value.Value;

/**
 * Represents a zero-knowledge equality proof.
 *
 * <p>An equality proof demonstrates that two ElGamal ciphertexts encrypt the same
 * value under different public keys, without revealing the actual value.</p>
 *
 * <p>This is implemented using a Chaum-Pedersen-style proof adapted for ElGamal
 * encryption over elliptic curves.</p>
 */
@Value.Immutable
public interface EqualityProof {

  /**
   * Construct an {@code EqualityProof} builder.
   *
   * @return An {@link ImmutableEqualityProof.Builder}.
   */
  static ImmutableEqualityProof.Builder builder() {
    return ImmutableEqualityProof.builder();
  }

  /**
   * Construct an {@code EqualityProof} from the given values.
   *
   * @param commitment1 The first commitment point (for the first ciphertext).
   * @param commitment2 The second commitment point (for the second ciphertext).
   * @param challenge   The Fiat-Shamir challenge (32 bytes).
   * @param response1   The first response scalar (32 bytes).
   * @param response2   The second response scalar (32 bytes).
   *
   * @return An {@link EqualityProof}.
   */
  static EqualityProof of(
    final ECPoint commitment1,
    final ECPoint commitment2,
    final byte[] challenge,
    final byte[] response1,
    final byte[] response2
  ) {
    return builder()
      .commitment1(commitment1)
      .commitment2(commitment2)
      .challenge(challenge)
      .response1(response1)
      .response2(response2)
      .build();
  }

  /**
   * Gets the first commitment point.
   *
   * @return The first commitment.
   */
  ECPoint commitment1();

  /**
   * Gets the second commitment point.
   *
   * @return The second commitment.
   */
  ECPoint commitment2();

  /**
   * Gets the challenge scalar.
   *
   * @return The 32-byte challenge.
   */
  byte[] challenge();

  /**
   * Gets the first response scalar.
   *
   * @return The 32-byte response.
   */
  byte[] response1();

  /**
   * Gets the second response scalar.
   *
   * @return The 32-byte response.
   */
  byte[] response2();

  /**
   * Serializes this equality proof to bytes.
   *
   * @return The serialized proof.
   */
  @Value.Derived
  default byte[] toBytes() {
    // Format: commitment1 (33) + commitment2 (33) + challenge (32) + response1 (32) + response2 (32) = 162 bytes
    byte[] c1Bytes = commitment1().getEncoded(true);
    byte[] c2Bytes = commitment2().getEncoded(true);
    byte[] result = new byte[33 + 33 + 32 + 32 + 32];
    System.arraycopy(c1Bytes, 0, result, 0, 33);
    System.arraycopy(c2Bytes, 0, result, 33, 33);
    System.arraycopy(challenge(), 0, result, 66, 32);
    System.arraycopy(response1(), 0, result, 98, 32);
    System.arraycopy(response2(), 0, result, 130, 32);
    return result;
  }
}
