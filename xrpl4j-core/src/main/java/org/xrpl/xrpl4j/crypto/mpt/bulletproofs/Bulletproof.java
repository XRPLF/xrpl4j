package org.xrpl.xrpl4j.crypto.mpt.bulletproofs;

import org.bouncycastle.math.ec.ECPoint;
import org.immutables.value.Value;

/**
 * Container for a complete bulletproof. Contains the value commitment, A/S commitments, and the IPA proof.
 */
@Value.Immutable
public interface Bulletproof {

  /**
   * Construct a {@code Bulletproof} builder.
   *
   * @return An {@link ImmutableBulletproof.Builder}.
   */
  static ImmutableBulletproof.Builder builder() {
    return ImmutableBulletproof.builder();
  }

  /**
   * Construct a {@code Bulletproof} from the given values.
   *
   * @param vCommitment The value commitment V = v*G + r*H.
   * @param aCommitment The A commitment point.
   * @param sCommitment The S commitment point.
   * @param ipaProof    The inner product argument proof.
   *
   * @return A {@link Bulletproof}.
   */
  static Bulletproof of(
    final ECPoint vCommitment,
    final ECPoint aCommitment,
    final ECPoint sCommitment,
    final IpaProof ipaProof
  ) {
    return builder()
      .vCommitment(vCommitment)
      .aCommitment(aCommitment)
      .sCommitment(sCommitment)
      .ipaProof(ipaProof)
      .build();
  }

  /**
   * Gets the value commitment.
   *
   * @return The V commitment point.
   */
  ECPoint vCommitment();

  /**
   * Gets the A commitment.
   *
   * @return The A commitment point.
   */
  ECPoint aCommitment();

  /**
   * Gets the S commitment.
   *
   * @return The S commitment point.
   */
  ECPoint sCommitment();

  /**
   * Gets the IPA proof.
   *
   * @return The inner product argument proof.
   */
  IpaProof ipaProof();
}
