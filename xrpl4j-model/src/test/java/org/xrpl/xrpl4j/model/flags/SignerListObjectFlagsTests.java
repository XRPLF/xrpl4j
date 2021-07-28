package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class SignerListObjectFlagsTests extends AbstractFlagsTest {

  public static Stream<Arguments> data() {
    return getBooleanCombinations(1);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testDeriveIndividualFlagsFromFlags(boolean lsfOneOwnerCount) {
    long expectedFlags = lsfOneOwnerCount ? Flags.SignerListFlags.ONE_OWNER_COUNT.getValue() : 0L;
    Flags.SignerListFlags flags = Flags.SignerListFlags.of(expectedFlags);

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.lsfOneOwnerCount()).isEqualTo(lsfOneOwnerCount);
  }
}
