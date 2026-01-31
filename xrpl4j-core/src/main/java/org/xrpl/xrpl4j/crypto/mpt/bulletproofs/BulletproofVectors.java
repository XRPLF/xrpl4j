package org.xrpl.xrpl4j.crypto.mpt.bulletproofs;

import org.immutables.value.Value;

import java.util.List;

/**
 * Container for the four scalar vectors used in bulletproofs. These vectors encode a value and provide randomness for
 * the proof.
 */
@Value.Immutable
public interface BulletproofVectors {

  /**
   * Construct a {@code BulletproofVectors} builder.
   *
   * @return An {@link ImmutableBulletproofVectors.Builder}.
   */
  static ImmutableBulletproofVectors.Builder builder() {
    return ImmutableBulletproofVectors.builder();
  }

  /**
   * Construct a {@code BulletproofVectors} from the four vectors.
   *
   * @param al The left bit vector (a_L).
   * @param ar The right bit vector (a_R = a_L - 1).
   * @param sl The left random vector (s_L).
   * @param sr The right random vector (s_R).
   *
   * @return A {@link BulletproofVectors}.
   */
  static BulletproofVectors of(
    final List<byte[]> al,
    final List<byte[]> ar,
    final List<byte[]> sl,
    final List<byte[]> sr
  ) {
    return builder()
      .al(al)
      .ar(ar)
      .sl(sl)
      .sr(sr)
      .build();
  }

  /**
   * Gets the left bit vector (a_L).
   *
   * @return The a_L vector as a list of 32-byte scalars.
   */
  List<byte[]> al();

  /**
   * Gets the right bit vector (a_R).
   *
   * @return The a_R vector as a list of 32-byte scalars.
   */
  List<byte[]> ar();

  /**
   * Gets the left random vector (s_L).
   *
   * @return The s_L vector as a list of 32-byte scalars.
   */
  List<byte[]> sl();

  /**
   * Gets the right random vector (s_R).
   *
   * @return The s_R vector as a list of 32-byte scalars.
   */
  List<byte[]> sr();

  /**
   * Gets the length of the vectors.
   *
   * @return The vector length.
   */
  @Value.Derived
  default int length() {
    return al().size();
  }
}
