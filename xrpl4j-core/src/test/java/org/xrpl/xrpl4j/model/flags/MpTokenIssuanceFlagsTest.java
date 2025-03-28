package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class MpTokenIssuanceFlagsTest extends AbstractFlagsTest {

  public static Stream<Arguments> data() {
    return getBooleanCombinations(7);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testDeriveIndividualFlagsFromFlags(
    boolean lsfMptLocked,
    boolean lsfMptCanLock,
    boolean lsfMptRequireAuth,
    boolean lsfMptCanEscrow,
    boolean lsfMptCanTrade,
    boolean lsfMptCanTransfer,
    boolean lsfMptCanClawback
  ) {
    long expectedFlags = (lsfMptLocked ? MpTokenIssuanceFlags.LOCKED.getValue() : 0L) |
                         (lsfMptCanLock ? MpTokenIssuanceFlags.CAN_LOCK.getValue() : 0L) |
                         (lsfMptRequireAuth ? MpTokenIssuanceFlags.REQUIRE_AUTH.getValue() : 0L) |
                         (lsfMptCanEscrow ? MpTokenIssuanceFlags.CAN_ESCROW.getValue() : 0L) |
                         (lsfMptCanTrade ? MpTokenIssuanceFlags.CAN_TRADE.getValue() : 0L) |
                         (lsfMptCanTransfer ? MpTokenIssuanceFlags.CAN_TRANSFER.getValue() : 0L) |
                         (lsfMptCanClawback ? MpTokenIssuanceFlags.CAN_CLAWBACK.getValue() : 0L);

    MpTokenIssuanceFlags flags = MpTokenIssuanceFlags.of(expectedFlags);

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.lsfMptLocked()).isEqualTo(lsfMptLocked);
    assertThat(flags.lsfMptCanLock()).isEqualTo(lsfMptCanLock);
    assertThat(flags.lsfMptRequireAuth()).isEqualTo(lsfMptRequireAuth);
    assertThat(flags.lsfMptCanEscrow()).isEqualTo(lsfMptCanEscrow);
    assertThat(flags.lsfMptCanTrade()).isEqualTo(lsfMptCanTrade);
    assertThat(flags.lsfMptCanTransfer()).isEqualTo(lsfMptCanTransfer);
    assertThat(flags.lsfMptCanClawback()).isEqualTo(lsfMptCanClawback);
  }

}