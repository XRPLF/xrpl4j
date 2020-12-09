package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.transactions.AbstractFlagsTest;

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
    Flags.TrustSetFlags flags = Flags.TrustSetFlags.builder()
        .tfFullyCanonicalSig(tfFullyCanonicalSig)
        .tfSetfAuth(tfSetfAuth)
        .tfSetNoRipple(tfSetNoRipple)
        .tfClearNoRipple(tfClearNoRipple)
        .tfSetFreeze(tfSetFreeze)
        .tfClearFreeze(tfClearFreeze)
        .build();

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
