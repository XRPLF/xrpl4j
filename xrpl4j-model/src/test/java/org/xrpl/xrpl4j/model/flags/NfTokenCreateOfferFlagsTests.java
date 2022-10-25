package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class NfTokenCreateOfferFlagsTests  extends AbstractFlagsTest {

  public static Stream<Arguments> data() {
    return getBooleanCombinations(5);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testFlagsConstructionWithIndividualFlags(
    boolean tfFullyCanonicalSig,
    boolean tfSellToken
  ) {
    Flags.NfTokenCreateOfferFlags flags = Flags.NfTokenCreateOfferFlags.builder()
      .tfFullyCanonicalSig(tfFullyCanonicalSig)
      .tfSellToken(tfSellToken)
      .build();

    assertThat(flags.getValue())
      .isEqualTo(getExpectedFlags(tfFullyCanonicalSig,tfSellToken));
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testDeriveIndividualFlagsFromFlags(
    boolean tfFullyCanonicalSig,
    boolean tfSellToken
  ) {
    long expectedFlags = getExpectedFlags(tfFullyCanonicalSig,tfSellToken);
    Flags.NfTokenCreateOfferFlags flags = Flags.NfTokenCreateOfferFlags.of(expectedFlags);

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.tfFullyCanonicalSig()).isEqualTo(tfFullyCanonicalSig);
    assertThat(flags.tfSellNfToken()).isEqualTo(tfSellToken);
  }

  private long getExpectedFlags(
    boolean tfFullyCanonicalSig,
    boolean tfSellToken
  ) {
    return (tfFullyCanonicalSig ? Flags.TransactionFlags.FULLY_CANONICAL_SIG.getValue() : 0L) |
      (tfSellToken ? Flags.NfTokenCreateOfferFlags.SELL_NFTOKEN.getValue() : 0L);
  }
}
