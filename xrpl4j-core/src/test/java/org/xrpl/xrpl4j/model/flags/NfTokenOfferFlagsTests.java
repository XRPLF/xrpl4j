package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class NfTokenOfferFlagsTests extends AbstractFlagsTest {

  public static Stream<Arguments> data() {
    return getBooleanCombinations(2);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testDeriveIndividualFlagsFromFlags(
    boolean lsfBuyToken,
    boolean lsfAuthorized
  ) {
    long expectedFlags = (lsfBuyToken ? NfTokenOfferFlags.BUY_TOKEN.getValue() : 0L) |
      (lsfAuthorized ? NfTokenOfferFlags.AUTHORIZED.getValue() : 0L);

    NfTokenOfferFlags flags = NfTokenOfferFlags.of(expectedFlags);

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.lsfBuyToken()).isEqualTo(lsfBuyToken);
    assertThat(flags.lsfAuthorized()).isEqualTo(lsfAuthorized);
  }
}
