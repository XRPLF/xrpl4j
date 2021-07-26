package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class OfferCreateFlagsTests extends AbstractFlagsTest {

  public static Stream<Arguments> data() {
    return getBooleanCombinations(5);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testFlagsConstructionWithIndividualFlags(
    boolean tfFullyCanonicalSig,
    boolean tfPassive,
    boolean tfImmediateOrCancel,
    boolean tfFillOrKill,
    boolean tfSell
  ) {
    Flags.OfferCreateFlags flags = Flags.OfferCreateFlags.builder()
        .tfFullyCanonicalSig(tfFullyCanonicalSig)
        .tfPassive(tfPassive)
        .tfImmediateOrCancel(tfImmediateOrCancel)
        .tfFillOrKill(tfFillOrKill)
        .tfSell(tfSell)
        .build();

    assertThat(flags.getValue())
      .isEqualTo(getExpectedFlags(tfFullyCanonicalSig, tfPassive, tfImmediateOrCancel, tfFillOrKill, tfSell));
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testDeriveIndividualFlagsFromFlags(
    boolean tfFullyCanonicalSig,
    boolean tfPassive,
    boolean tfImmediateOrCancel,
    boolean tfFillOrKill,
    boolean tfSell
  ) {
    long expectedFlags = getExpectedFlags(tfFullyCanonicalSig, tfPassive, tfImmediateOrCancel, tfFillOrKill, tfSell);
    Flags.OfferCreateFlags flags = Flags.OfferCreateFlags.of(expectedFlags);

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.tfFullyCanonicalSig()).isEqualTo(tfFullyCanonicalSig);
    assertThat(flags.tfPassive()).isEqualTo(tfPassive);
    assertThat(flags.tfImmediateOrCancel()).isEqualTo(tfImmediateOrCancel);
    assertThat(flags.tfFillOrKill()).isEqualTo(tfFillOrKill);
    assertThat(flags.tfSell()).isEqualTo(tfSell);
  }

  private long getExpectedFlags(
    boolean tfFullyCanonicalSig,
    boolean tfPassive,
    boolean tfImmediateOrCancel,
    boolean tfFillOrKill,
    boolean tfSell
  ) {
    return (tfFullyCanonicalSig ? Flags.OfferCreateFlags.FULLY_CANONICAL_SIG.getValue() : 0L) |
      (tfPassive ? Flags.OfferCreateFlags.PASSIVE.getValue() : 0L) |
      (tfImmediateOrCancel ? Flags.OfferCreateFlags.IMMEDIATE_OR_CANCEL.getValue() : 0L) |
      (tfFillOrKill ? Flags.OfferCreateFlags.FILL_OR_KILL.getValue() : 0L) |
      (tfSell ? Flags.OfferCreateFlags.SELL.getValue() : 0L);
  }
}
