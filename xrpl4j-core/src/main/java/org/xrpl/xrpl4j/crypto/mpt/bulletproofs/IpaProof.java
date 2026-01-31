package org.xrpl.xrpl4j.crypto.mpt.bulletproofs;

import org.bouncycastle.math.ec.ECPoint;
import org.immutables.value.Value;

import java.util.List;

/**
 * Container for an Inner Product Argument (IPA) proof. Contains the L/R commitment points and final scalar values.
 */
@Value.Immutable
public interface IpaProof {

  /**
   * Construct an {@code IpaProof} builder.
   *
   * @return An {@link ImmutableIpaProof.Builder}.
   */
  static ImmutableIpaProof.Builder builder() {
    return ImmutableIpaProof.builder();
  }

  /**
   * Construct an {@code IpaProof} from the given values.
   *
   * @param lPoints    The L commitment points (one per round).
   * @param rPoints    The R commitment points (one per round).
   * @param aFinal     The final scalar a (32 bytes).
   * @param bFinal     The final scalar b (32 bytes).
   * @param dotProduct The initial inner product (32 bytes).
   *
   * @return An {@link IpaProof}.
   */
  @SuppressWarnings("checkstyle:ParameterName")
  static IpaProof of(
    final List<ECPoint> lPoints,
    final List<ECPoint> rPoints,
    final byte[] aFinal,
    final byte[] bFinal,
    final byte[] dotProduct
  ) {
    return builder()
      .lPoints(lPoints)
      .rPoints(rPoints)
      .aFinal(aFinal)
      .bFinal(bFinal)
      .dotProduct(dotProduct)
      .build();
  }

  /**
   * Gets the L commitment points.
   *
   * @return The L points as a list.
   */
  @SuppressWarnings("checkstyle:MethodName")
  List<ECPoint> lPoints();

  /**
   * Gets the R commitment points.
   *
   * @return The R points as a list.
   */
  @SuppressWarnings("checkstyle:MethodName")
  List<ECPoint> rPoints();

  /**
   * Gets the final scalar a.
   *
   * @return The a_final scalar (32 bytes).
   */
  @SuppressWarnings("checkstyle:MethodName")
  byte[] aFinal();

  /**
   * Gets the final scalar b.
   *
   * @return The b_final scalar (32 bytes).
   */
  @SuppressWarnings("checkstyle:MethodName")
  byte[] bFinal();

  /**
   * Gets the initial inner product.
   *
   * @return The dot product (32 bytes).
   */
  byte[] dotProduct();

  /**
   * Gets the number of rounds in the proof.
   *
   * @return The number of L/R pairs.
   */
  @Value.Derived
  default int rounds() {
    return lPoints().size();
  }
}
