package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;
import static org.xrpl.xrpl4j.model.flags.AbstractFlagsTest.getBooleanCombinations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xrpl.xrpl4j.model.AbstractJsonTest;

import java.util.stream.Stream;

class MpTokenFlagsTest extends AbstractFlagsTest {

  public static Stream<Arguments> data() {
    return getBooleanCombinations(2);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testDeriveIndividualFlagsFromFlags(
    boolean lsfLocked,
    boolean lsfAuthorized
  ) {
    long expectedFlags = (lsfLocked ? MpTokenFlags.LOCKED.getValue() : 0L) |
                         (lsfAuthorized ? MpTokenFlags.AUTHORIZED.getValue() : 0L);

    MpTokenFlags flags = MpTokenFlags.of(expectedFlags);

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.lsfMptLocked()).isEqualTo(lsfLocked);
    assertThat(flags.lsfMptAuthorized()).isEqualTo(lsfAuthorized);
  }

}