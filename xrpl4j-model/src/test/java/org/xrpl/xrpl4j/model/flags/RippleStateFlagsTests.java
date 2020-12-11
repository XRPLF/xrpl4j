package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.transactions.AbstractFlagsTest;

import java.util.Collection;

@RunWith(Parameterized.class)
public class RippleStateFlagsTests extends AbstractFlagsTest {

  boolean lsfLowReserve;
  boolean lsfHighReserve;
  boolean lsfLowAuth;
  boolean lsfHighAuth;
  boolean lsfLowNoRipple;
  boolean lsfHighNoRipple;
  boolean lsfLowFreeze;
  boolean lsfHighFreeze;

  long expectedFlags;

  public RippleStateFlagsTests(
      boolean lsfLowReserve,
      boolean lsfHighReserve,
      boolean lsfLowAuth,
      boolean lsfHighAuth,
      boolean lsfLowNoRipple,
      boolean lsfHighNoRipple,
      boolean lsfLowFreeze,
      boolean lsfHighFreeze
  ) {
    this.lsfLowReserve = lsfLowReserve;
    this.lsfHighReserve = lsfHighReserve;
    this.lsfLowAuth = lsfLowAuth;
    this.lsfHighAuth = lsfHighAuth;
    this.lsfLowNoRipple = lsfLowNoRipple;
    this.lsfHighNoRipple = lsfHighNoRipple;
    this.lsfLowFreeze = lsfLowFreeze;
    this.lsfHighFreeze = lsfHighFreeze;

    expectedFlags = (lsfLowReserve ? Flags.RippleStateFlags.LOW_RESERVE.getValue() : 0L) |
        (lsfHighReserve ? Flags.RippleStateFlags.HIGH_RESERVE.getValue() : 0L) |
        (lsfLowAuth ? Flags.RippleStateFlags.LOW_AUTH.getValue() : 0L) |
        (lsfHighAuth ? Flags.RippleStateFlags.HIGH_AUTH.getValue() : 0L) |
        (lsfLowNoRipple ? Flags.RippleStateFlags.LOW_NO_RIPPLE.getValue() : 0L) |
        (lsfHighNoRipple ? Flags.RippleStateFlags.HIGH_NO_RIPPLE.getValue() : 0L) |
        (lsfLowFreeze ? Flags.RippleStateFlags.LOW_FREEZE.getValue() : 0L) |
        (lsfHighFreeze ? Flags.RippleStateFlags.HIGH_FREEZE.getValue() : 0L);
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return getBooleanCombinations(8);
  }

  @Test
  public void testFlagsConstructionWithIndividualFlags() {
    Flags.RippleStateFlags flags = Flags.RippleStateFlags.builder()
        .lsfLowReserve(lsfLowReserve)
        .lsfHighReserve(lsfHighReserve)
        .lsfLowAuth(lsfLowAuth)
        .lsfHighAuth(lsfHighAuth)
        .lsfLowNoRipple(lsfLowNoRipple)
        .lsfHighNoRipple(lsfHighNoRipple)
        .lsfLowFreeze(lsfLowFreeze)
        .lsfHighFreeze(lsfHighFreeze)
        .build();

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
  }

  @Test
  public void testDeriveIndividualFlagsFromFlags() {
    Flags.RippleStateFlags flags = Flags.RippleStateFlags.of(expectedFlags);

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.lsfLowReserve()).isEqualTo(lsfLowReserve);
    assertThat(flags.lsfHighReserve()).isEqualTo(lsfHighReserve);
    assertThat(flags.lsfLowAuth()).isEqualTo(lsfLowAuth);
    assertThat(flags.lsfHighAuth()).isEqualTo(lsfHighAuth);
    assertThat(flags.lsfLowNoRipple()).isEqualTo(lsfLowNoRipple);
    assertThat(flags.lsfHighNoRipple()).isEqualTo(lsfHighNoRipple);
    assertThat(flags.lsfLowFreeze()).isEqualTo(lsfLowFreeze);
    assertThat(flags.lsfHighFreeze()).isEqualTo(lsfHighFreeze);
  }
}
