package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class RippleStateFlagsTests extends AbstractFlagsTest {

  public static Stream<Arguments> data() {
    return getBooleanCombinations(8);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testDeriveIndividualFlagsFromFlags(
    boolean lsfLowReserve,
    boolean lsfHighReserve,
    boolean lsfLowAuth,
    boolean lsfHighAuth,
    boolean lsfLowNoRipple,
    boolean lsfHighNoRipple,
    boolean lsfLowFreeze,
    boolean lsfHighFreeze
  ) {
    long expectedFlags = getExpectedFlags(
      lsfLowReserve,
      lsfHighReserve,
      lsfLowAuth,
      lsfHighAuth,
      lsfLowNoRipple,
      lsfHighNoRipple,
      lsfLowFreeze,
      lsfHighFreeze
    );
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

  protected long getExpectedFlags(
    boolean lsfLowReserve,
    boolean lsfHighReserve,
    boolean lsfLowAuth,
    boolean lsfHighAuth,
    boolean lsfLowNoRipple,
    boolean lsfHighNoRipple,
    boolean lsfLowFreeze,
    boolean lsfHighFreeze
  ) {
    return (lsfLowReserve ? Flags.RippleStateFlags.LOW_RESERVE.getValue() : 0L) |
      (lsfHighReserve ? Flags.RippleStateFlags.HIGH_RESERVE.getValue() : 0L) |
      (lsfLowAuth ? Flags.RippleStateFlags.LOW_AUTH.getValue() : 0L) |
      (lsfHighAuth ? Flags.RippleStateFlags.HIGH_AUTH.getValue() : 0L) |
      (lsfLowNoRipple ? Flags.RippleStateFlags.LOW_NO_RIPPLE.getValue() : 0L) |
      (lsfHighNoRipple ? Flags.RippleStateFlags.HIGH_NO_RIPPLE.getValue() : 0L) |
      (lsfLowFreeze ? Flags.RippleStateFlags.LOW_FREEZE.getValue() : 0L) |
      (lsfHighFreeze ? Flags.RippleStateFlags.HIGH_FREEZE.getValue() : 0L);
  }
}
