package org.xrpl.xrpl4j.crypto.mpt.bulletproofs;

import org.bouncycastle.math.ec.ECPoint;
import org.immutables.value.Value;

/**
 * Represents a zero-knowledge range proof.
 *
 * <p>A range proof demonstrates that a committed value lies within a specific range
 * (typically [0, 2^64)) without revealing the actual value.</p>
 *
 * <p>This class wraps the underlying Bulletproof implementation and provides
 * a higher-level abstraction for use in confidential MPT operations.</p>
 */
@Value.Immutable
public interface RangeProof {

  /**
   * Construct a {@code RangeProof} builder.
   *
   * @return An {@link ImmutableRangeProof.Builder}.
   */
  static ImmutableRangeProof.Builder builder() {
    return ImmutableRangeProof.builder();
  }

  /**
   * Construct a {@code RangeProof} from a Bulletproof.
   *
   * @param bulletproof The underlying bulletproof.
   *
   * @return A {@link RangeProof}.
   */
  static RangeProof of(Bulletproof bulletproof) {
    return builder().bulletproof(bulletproof).build();
  }

  /**
   * Gets the underlying Bulletproof.
   *
   * @return The bulletproof.
   */
  Bulletproof bulletproof();

  /**
   * Gets the value commitment (V = value*G + blinding*H).
   *
   * @return The commitment point.
   */
  @Value.Derived
  default ECPoint commitment() {
    return bulletproof().vCommitment();
  }

  /**
   * Gets the A commitment point.
   *
   * @return The A point.
   */
  @Value.Derived
  default ECPoint aCommitment() {
    return bulletproof().aCommitment();
  }

  /**
   * Gets the S commitment point.
   *
   * @return The S point.
   */
  @Value.Derived
  default ECPoint sCommitment() {
    return bulletproof().sCommitment();
  }

  /**
   * Gets the inner product argument proof.
   *
   * @return The IPA proof.
   */
  @Value.Derived
  default IpaProof ipaProof() {
    return bulletproof().ipaProof();
  }

  /**
   * Serializes this range proof to bytes.
   *
   * @return The serialized proof.
   */
  default byte[] toBytes() {
    // TODO: Implement proper serialization
    throw new UnsupportedOperationException("Serialization not yet implemented");
  }

  /**
   * Deserializes a range proof from bytes.
   *
   * @param bytes The serialized proof.
   *
   * @return The deserialized RangeProof.
   */
  static RangeProof fromBytes(byte[] bytes) {
    // TODO: Implement proper deserialization
    throw new UnsupportedOperationException("Deserialization not yet implemented");
  }
}
