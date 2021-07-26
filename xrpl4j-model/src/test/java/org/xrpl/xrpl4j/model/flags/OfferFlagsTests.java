package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class OfferFlagsTests extends AbstractFlagsTest {

  public static Stream<Arguments> data() {
    return getBooleanCombinations(2);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testDeriveIndividualFlagsFromFlags(
    boolean lsfPassive,
    boolean lsfSell
  ) {
    long expectedFlags = (lsfPassive ? Flags.OfferFlags.PASSIVE.getValue() : 0L) |
      (lsfSell ? Flags.OfferFlags.SELL.getValue() : 0L);

    Flags.OfferFlags flags = Flags.OfferFlags.of(expectedFlags);

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.lsfPassive()).isEqualTo(lsfPassive);
    assertThat(flags.lsfSell()).isEqualTo(lsfSell);
  }
}
