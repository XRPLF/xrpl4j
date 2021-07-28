package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class TrustSetFlagsTests extends AbstractFlagsTest {

  public static Stream<Arguments> data() {
    return getBooleanCombinations(6);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testFlagsConstructionWithIndividualFlags(
    boolean tfFullyCanonicalSig,
    boolean tfSetfAuth,
    boolean tfSetNoRipple,
    boolean tfClearNoRipple,
    boolean tfSetFreeze,
    boolean tfClearFreeze
  ) {
    Flags.TrustSetFlags.Builder builder = Flags.TrustSetFlags.builder()
      .tfFullyCanonicalSig(tfFullyCanonicalSig)
      .tfSetfAuth(tfSetfAuth);

    if (tfSetNoRipple) {
      builder.tfSetNoRipple();
    }

    if (tfClearNoRipple) {
      builder.tfClearNoRipple();
    }

    if (tfSetFreeze) {
      builder.tfSetFreeze();
    }

    if (tfClearFreeze) {
      builder.tfClearFreeze();
    }

    Flags.TrustSetFlags flags = builder.build();

    long expectedFlags = getExpectedFlags(
      tfFullyCanonicalSig,
      tfSetfAuth,
      tfSetNoRipple,
      tfClearNoRipple,
      tfSetFreeze,
      tfClearFreeze
    );
    assertThat(flags.getValue()).isEqualTo(expectedFlags);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testDeriveIndividualFlagsFromFlags(
    boolean tfFullyCanonicalSig,
    boolean tfSetfAuth,
    boolean tfSetNoRipple,
    boolean tfClearNoRipple,
    boolean tfSetFreeze,
    boolean tfClearFreeze
  ) {
    long expectedFlags = getExpectedFlags(
      tfFullyCanonicalSig,
      tfSetfAuth,
      tfSetNoRipple,
      tfClearNoRipple,
      tfSetFreeze,
      tfClearFreeze
    );
    Flags.TrustSetFlags flags = Flags.TrustSetFlags.of(expectedFlags);

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.tfFullyCanonicalSig()).isEqualTo(tfFullyCanonicalSig);
    assertThat(flags.tfSetfAuth()).isEqualTo(tfSetfAuth);
    assertThat(flags.tfSetNoRipple()).isEqualTo(tfSetNoRipple);
    assertThat(flags.tfClearNoRipple()).isEqualTo(tfClearNoRipple);
    assertThat(flags.tfSetFreeze()).isEqualTo(tfSetFreeze);
    assertThat(flags.tfClearFreeze()).isEqualTo(tfClearFreeze);
  }

  private long getExpectedFlags(
    boolean tfFullyCanonicalSig,
    boolean tfSetfAuth,
    boolean tfSetNoRipple,
    boolean tfClearNoRipple,
    boolean tfSetFreeze,
    boolean tfClearFreeze
  ) {
    return (tfFullyCanonicalSig ? Flags.TrustSetFlags.FULLY_CANONICAL_SIG.getValue() : 0L) |
      (tfSetfAuth ? Flags.TrustSetFlags.SET_F_AUTH.getValue() : 0L) |
      (tfSetNoRipple ? Flags.TrustSetFlags.SET_NO_RIPPLE.getValue() : 0L) |
      (tfClearNoRipple ? Flags.TrustSetFlags.CLEAR_NO_RIPPLE.getValue() : 0L) |
      (tfSetFreeze ? Flags.TrustSetFlags.SET_FREEZE.getValue() : 0L) |
      (tfClearFreeze ? Flags.TrustSetFlags.CLEAR_FREEZE.getValue() : 0L);
  }
}
