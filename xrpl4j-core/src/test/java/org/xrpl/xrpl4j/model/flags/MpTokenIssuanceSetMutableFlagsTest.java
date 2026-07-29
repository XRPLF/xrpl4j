package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class MpTokenIssuanceSetMutableFlagsTest extends AbstractFlagsTest {

  /**
   * Data source exercising every combination of the 6 settable mutable flags
   * (6 booleans = 2^6 = 64 combinations), including the empty case.
   */
  public static Stream<Arguments> data() {
    return getBooleanCombinations(6);
  }

  @ParameterizedTest
  @MethodSource("data")
  void testSetFlags(
    boolean setCanLock,
    boolean setRequireAuth,
    boolean setCanEscrow,
    boolean setCanTrade,
    boolean setCanTransfer,
    boolean setCanClawback
  ) {
    long expectedFlags =
      (setCanLock ? MpTokenIssuanceSetMutableFlags.SET_CAN_LOCK.getValue() : 0L) |
      (setRequireAuth ? MpTokenIssuanceSetMutableFlags.SET_REQUIRE_AUTH.getValue() : 0L) |
      (setCanEscrow ? MpTokenIssuanceSetMutableFlags.SET_CAN_ESCROW.getValue() : 0L) |
      (setCanTrade ? MpTokenIssuanceSetMutableFlags.SET_CAN_TRADE.getValue() : 0L) |
      (setCanTransfer ? MpTokenIssuanceSetMutableFlags.SET_CAN_TRANSFER.getValue() : 0L) |
      (setCanClawback ? MpTokenIssuanceSetMutableFlags.SET_CAN_CLAWBACK.getValue() : 0L);

    MpTokenIssuanceSetMutableFlags flags = MpTokenIssuanceSetMutableFlags.builder()
      .tmfMptSetCanLock(setCanLock)
      .tmfMptSetRequireAuth(setRequireAuth)
      .tmfMptSetCanEscrow(setCanEscrow)
      .tmfMptSetCanTrade(setCanTrade)
      .tmfMptSetCanTransfer(setCanTransfer)
      .tmfMptSetCanClawback(setCanClawback)
      .build();

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.tmfMptSetCanLock()).isEqualTo(setCanLock);
    assertThat(flags.tmfMptSetRequireAuth()).isEqualTo(setRequireAuth);
    assertThat(flags.tmfMptSetCanEscrow()).isEqualTo(setCanEscrow);
    assertThat(flags.tmfMptSetCanTrade()).isEqualTo(setCanTrade);
    assertThat(flags.tmfMptSetCanTransfer()).isEqualTo(setCanTransfer);
    assertThat(flags.tmfMptSetCanClawback()).isEqualTo(setCanClawback);
  }

  @Test
  void testEmptyBuilderProducesZeroFlags() {
    MpTokenIssuanceSetMutableFlags flags = MpTokenIssuanceSetMutableFlags.builder().build();
    assertThat(flags.getValue()).isEqualTo(0L);
    assertThat(flags.tmfMptSetCanLock()).isFalse();
    assertThat(flags.tmfMptSetRequireAuth()).isFalse();
    assertThat(flags.tmfMptSetCanEscrow()).isFalse();
    assertThat(flags.tmfMptSetCanTrade()).isFalse();
    assertThat(flags.tmfMptSetCanTransfer()).isFalse();
    assertThat(flags.tmfMptSetCanClawback()).isFalse();
  }

  @Test
  void testOfRawValueRoundTrips() {
    long raw = MpTokenIssuanceSetMutableFlags.SET_CAN_LOCK.getValue() |
      MpTokenIssuanceSetMutableFlags.SET_CAN_TRANSFER.getValue();

    MpTokenIssuanceSetMutableFlags flags = MpTokenIssuanceSetMutableFlags.of(raw);

    assertThat(flags.getValue()).isEqualTo(raw);
    assertThat(flags.tmfMptSetCanLock()).isTrue();
    assertThat(flags.tmfMptSetCanTransfer()).isTrue();
    assertThat(flags.tmfMptSetCanTrade()).isFalse();
    assertThat(flags.tmfMptSetCanClawback()).isFalse();
  }

  @Test
  void testStaticConstantBitValues() {
    assertThat(MpTokenIssuanceSetMutableFlags.SET_CAN_LOCK.getValue()).isEqualTo(0x00000001L);
    assertThat(MpTokenIssuanceSetMutableFlags.SET_REQUIRE_AUTH.getValue()).isEqualTo(0x00000002L);
    assertThat(MpTokenIssuanceSetMutableFlags.SET_CAN_ESCROW.getValue()).isEqualTo(0x00000004L);
    assertThat(MpTokenIssuanceSetMutableFlags.SET_CAN_TRADE.getValue()).isEqualTo(0x00000008L);
    assertThat(MpTokenIssuanceSetMutableFlags.SET_CAN_TRANSFER.getValue()).isEqualTo(0x00000010L);
    assertThat(MpTokenIssuanceSetMutableFlags.SET_CAN_CLAWBACK.getValue()).isEqualTo(0x00000020L);
  }
}
