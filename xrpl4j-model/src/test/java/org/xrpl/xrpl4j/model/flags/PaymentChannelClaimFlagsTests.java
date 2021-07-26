package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class PaymentChannelClaimFlagsTests extends AbstractFlagsTest {

  public static Stream<Arguments> data() {
    return getBooleanCombinations(3);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testFlagsConstructionWithIndividualFlags(
    boolean tfFullyCanonicalSig,
    boolean tfRenew,
    boolean tfClose
  ) {
    Flags.PaymentChannelClaimFlags flags = Flags.PaymentChannelClaimFlags.builder()
      .tfFullyCanonicalSig(tfFullyCanonicalSig)
      .tfRenew(tfRenew)
      .tfClose(tfClose)
      .build();

    assertThat(flags.getValue()).isEqualTo(getExpectedFlags(tfFullyCanonicalSig, tfRenew, tfClose));
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testDeriveIndividualFlagsFromFlags(
    boolean tfFullyCanonicalSig,
    boolean tfRenew,
    boolean tfClose
  ) {
    long expectedFlags = getExpectedFlags(tfFullyCanonicalSig, tfRenew, tfClose);
    Flags.PaymentChannelClaimFlags flags = Flags.PaymentChannelClaimFlags.of(expectedFlags);

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.tfFullyCanonicalSig()).isEqualTo(tfFullyCanonicalSig);
    assertThat(flags.tfRenew()).isEqualTo(tfRenew);
    assertThat(flags.tfClose()).isEqualTo(tfClose);
  }

  private long getExpectedFlags(boolean tfFullyCanonicalSig, boolean tfRenew, boolean tfClose) {
    return (tfFullyCanonicalSig ? Flags.PaymentChannelClaimFlags.FULLY_CANONICAL_SIG.getValue() : 0L) |
      (tfRenew ? Flags.PaymentChannelClaimFlags.RENEW.getValue() : 0L) |
      (tfClose ? Flags.PaymentChannelClaimFlags.CLOSE.getValue() : 0L);
  }
}
