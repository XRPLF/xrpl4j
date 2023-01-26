package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class NfTokenCreateOfferFlagsTests  extends AbstractFlagsTest {

  public static Stream<Arguments> data() {
    return getBooleanCombinations(4);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testFlagsConstructionWithIndividualFlags(
    boolean tfSellToken
  ) {
    NfTokenCreateOfferFlags flags = NfTokenCreateOfferFlags.builder()
      .tfSellToken(tfSellToken)
      .build();

    assertThat(flags.getValue())
      .isEqualTo(getExpectedFlags(tfSellToken));
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testDeriveIndividualFlagsFromFlags(
    boolean tfSellToken
  ) {
    long expectedFlags = getExpectedFlags(tfSellToken);
    NfTokenCreateOfferFlags flags = NfTokenCreateOfferFlags.of(expectedFlags);

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.tfFullyCanonicalSig()).isEqualTo(true);
    assertThat(flags.tfSellNfToken()).isEqualTo(tfSellToken);
  }

  private long getExpectedFlags(
    boolean tfSellToken
  ) {
    return (TransactionFlags.FULLY_CANONICAL_SIG.getValue()) |
      (tfSellToken ? NfTokenCreateOfferFlags.SELL_NFTOKEN.getValue() : 0L);
  }
}
