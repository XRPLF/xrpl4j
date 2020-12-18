package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

@RunWith(Parameterized.class)
public class TrustSetFlagsTests extends AbstractFlagsTest {

  boolean tfFullyCanonicalSig;
  boolean tfSetfAuth;
  boolean tfSetNoRipple;
  boolean tfClearNoRipple;
  boolean tfSetFreeze;
  boolean tfClearFreeze;

  long expectedFlags;

  /**
   * Required-args constructor.
   *
   * @param tfFullyCanonicalSig The current value of {@link this.tfFullyCanonicalSig}.
   * @param tfSetfAuth          The current value of {@link this.tfSetfAuth}.
   * @param tfSetNoRipple       The current value of {@link this.tfSetNoRipple}.
   * @param tfClearNoRipple     The current value of {@link this.tfClearNoRipple}.
   * @param tfSetFreeze         The current value of {@link this.tfSetFreeze}.
   * @param tfClearFreeze       The current value of {@link this.tfClearFreeze}.
   */
  public TrustSetFlagsTests(
      boolean tfFullyCanonicalSig,
      boolean tfSetfAuth,
      boolean tfSetNoRipple,
      boolean tfClearNoRipple,
      boolean tfSetFreeze,
      boolean tfClearFreeze
  ) {
    this.tfFullyCanonicalSig = tfFullyCanonicalSig;
    this.tfSetfAuth = tfSetfAuth;
    this.tfSetNoRipple = tfSetNoRipple;
    this.tfClearNoRipple = tfClearNoRipple;
    this.tfSetFreeze = tfSetFreeze;
    this.tfClearFreeze = tfClearFreeze;

    expectedFlags = (tfFullyCanonicalSig ? Flags.TrustSetFlags.FULLY_CANONICAL_SIG.getValue() : 0L) |
        (tfSetfAuth ? Flags.TrustSetFlags.SET_F_AUTH.getValue() : 0L) |
        (tfSetNoRipple ? Flags.TrustSetFlags.SET_NO_RIPPLE.getValue() : 0L) |
        (tfClearNoRipple ? Flags.TrustSetFlags.CLEAR_NO_RIPPLE.getValue() : 0L) |
        (tfSetFreeze ? Flags.TrustSetFlags.SET_FREEZE.getValue() : 0L) |
        (tfClearFreeze ? Flags.TrustSetFlags.CLEAR_FREEZE.getValue() : 0L);
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return getBooleanCombinations(6);
  }

  @Test
  public void testFlagsConstructionWithIndividualFlags() {
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

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
  }

  @Test
  public void testDeriveIndividualFlagsFromFlags() {
    Flags.TrustSetFlags flags = Flags.TrustSetFlags.of(expectedFlags);

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.tfFullyCanonicalSig()).isEqualTo(tfFullyCanonicalSig);
    assertThat(flags.tfSetfAuth()).isEqualTo(tfSetfAuth);
    assertThat(flags.tfSetNoRipple()).isEqualTo(tfSetNoRipple);
    assertThat(flags.tfClearNoRipple()).isEqualTo(tfClearNoRipple);
    assertThat(flags.tfSetFreeze()).isEqualTo(tfSetFreeze);
    assertThat(flags.tfClearFreeze()).isEqualTo(tfClearFreeze);
  }
}
