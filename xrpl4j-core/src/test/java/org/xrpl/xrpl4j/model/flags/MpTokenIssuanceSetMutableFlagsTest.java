package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class MpTokenIssuanceSetMutableFlagsTest extends AbstractFlagsTest {

  /**
   * Shared data source for {@code testCanLockSetAndClear} and {@code testTradeTransferClawbackSetAndClear}.
   * Each test method covers 3 of the 6 set/clear flag pairs (6 booleans = 2^6 = 64 combinations),
   * exhaustively exercising every builder bit, the empty case, and the simultaneous-set-and-clear case.
   */
  public static Stream<Arguments> data() {
    return getBooleanCombinations(6);
  }

  @ParameterizedTest
  @MethodSource("data")
  void testCanLockSetAndClear(
    boolean setCanLock,
    boolean clearCanLock,
    boolean setRequireAuth,
    boolean clearRequireAuth,
    boolean setCanEscrow,
    boolean clearCanEscrow
  ) {
    long expectedFlags =
      (setCanLock ? MpTokenIssuanceSetMutableFlags.SET_CAN_LOCK.getValue() : 0L) |
      (clearCanLock ? MpTokenIssuanceSetMutableFlags.CLEAR_CAN_LOCK.getValue() : 0L) |
      (setRequireAuth ? MpTokenIssuanceSetMutableFlags.SET_REQUIRE_AUTH.getValue() : 0L) |
      (clearRequireAuth ? MpTokenIssuanceSetMutableFlags.CLEAR_REQUIRE_AUTH.getValue() : 0L) |
      (setCanEscrow ? MpTokenIssuanceSetMutableFlags.SET_CAN_ESCROW.getValue() : 0L) |
      (clearCanEscrow ? MpTokenIssuanceSetMutableFlags.CLEAR_CAN_ESCROW.getValue() : 0L);

    MpTokenIssuanceSetMutableFlags flags = MpTokenIssuanceSetMutableFlags.builder()
      .tmfMptSetCanLock(setCanLock)
      .tmfMptClearCanLock(clearCanLock)
      .tmfMptSetRequireAuth(setRequireAuth)
      .tmfMptClearRequireAuth(clearRequireAuth)
      .tmfMptSetCanEscrow(setCanEscrow)
      .tmfMptClearCanEscrow(clearCanEscrow)
      .build();

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.tmfMptSetCanLock()).isEqualTo(setCanLock);
    assertThat(flags.tmfMptClearCanLock()).isEqualTo(clearCanLock);
    assertThat(flags.tmfMptSetRequireAuth()).isEqualTo(setRequireAuth);
    assertThat(flags.tmfMptClearRequireAuth()).isEqualTo(clearRequireAuth);
    assertThat(flags.tmfMptSetCanEscrow()).isEqualTo(setCanEscrow);
    assertThat(flags.tmfMptClearCanEscrow()).isEqualTo(clearCanEscrow);
  }

  @ParameterizedTest
  @MethodSource("data")
  void testTradeTransferClawbackSetAndClear(
    boolean setCanTrade,
    boolean clearCanTrade,
    boolean setCanTransfer,
    boolean clearCanTransfer,
    boolean setCanClawback,
    boolean clearCanClawback
  ) {
    long expectedFlags =
      (setCanTrade ? MpTokenIssuanceSetMutableFlags.SET_CAN_TRADE.getValue() : 0L) |
      (clearCanTrade ? MpTokenIssuanceSetMutableFlags.CLEAR_CAN_TRADE.getValue() : 0L) |
      (setCanTransfer ? MpTokenIssuanceSetMutableFlags.SET_CAN_TRANSFER.getValue() : 0L) |
      (clearCanTransfer ? MpTokenIssuanceSetMutableFlags.CLEAR_CAN_TRANSFER.getValue() : 0L) |
      (setCanClawback ? MpTokenIssuanceSetMutableFlags.SET_CAN_CLAWBACK.getValue() : 0L) |
      (clearCanClawback ? MpTokenIssuanceSetMutableFlags.CLEAR_CAN_CLAWBACK.getValue() : 0L);

    MpTokenIssuanceSetMutableFlags flags = MpTokenIssuanceSetMutableFlags.builder()
      .tmfMptSetCanTrade(setCanTrade)
      .tmfMptClearCanTrade(clearCanTrade)
      .tmfMptSetCanTransfer(setCanTransfer)
      .tmfMptClearCanTransfer(clearCanTransfer)
      .tmfMptSetCanClawback(setCanClawback)
      .tmfMptClearCanClawback(clearCanClawback)
      .build();

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.tmfMptSetCanTrade()).isEqualTo(setCanTrade);
    assertThat(flags.tmfMptClearCanTrade()).isEqualTo(clearCanTrade);
    assertThat(flags.tmfMptSetCanTransfer()).isEqualTo(setCanTransfer);
    assertThat(flags.tmfMptClearCanTransfer()).isEqualTo(clearCanTransfer);
    assertThat(flags.tmfMptSetCanClawback()).isEqualTo(setCanClawback);
    assertThat(flags.tmfMptClearCanClawback()).isEqualTo(clearCanClawback);
  }

  @Test
  void testEmptyBuilderProducesZeroFlags() {
    MpTokenIssuanceSetMutableFlags flags = MpTokenIssuanceSetMutableFlags.builder().build();
    assertThat(flags.getValue()).isEqualTo(0L);
    assertThat(flags.tmfMptSetCanLock()).isFalse();
    assertThat(flags.tmfMptClearCanLock()).isFalse();
    assertThat(flags.tmfMptSetRequireAuth()).isFalse();
    assertThat(flags.tmfMptClearRequireAuth()).isFalse();
    assertThat(flags.tmfMptSetCanEscrow()).isFalse();
    assertThat(flags.tmfMptClearCanEscrow()).isFalse();
    assertThat(flags.tmfMptSetCanTrade()).isFalse();
    assertThat(flags.tmfMptClearCanTrade()).isFalse();
    assertThat(flags.tmfMptSetCanTransfer()).isFalse();
    assertThat(flags.tmfMptClearCanTransfer()).isFalse();
    assertThat(flags.tmfMptSetCanClawback()).isFalse();
    assertThat(flags.tmfMptClearCanClawback()).isFalse();
  }

  @Test
  void testOfRawValueRoundTrips() {
    long raw = MpTokenIssuanceSetMutableFlags.SET_CAN_LOCK.getValue() |
      MpTokenIssuanceSetMutableFlags.CLEAR_CAN_TRANSFER.getValue();

    MpTokenIssuanceSetMutableFlags flags = MpTokenIssuanceSetMutableFlags.of(raw);

    assertThat(flags.getValue()).isEqualTo(raw);
    assertThat(flags.tmfMptSetCanLock()).isTrue();
    assertThat(flags.tmfMptClearCanTransfer()).isTrue();
    assertThat(flags.tmfMptSetCanTransfer()).isFalse();
    assertThat(flags.tmfMptClearCanLock()).isFalse();
  }

  @Test
  void testStaticConstantBitValues() {
    assertThat(MpTokenIssuanceSetMutableFlags.SET_CAN_LOCK.getValue()).isEqualTo(0x00000001L);
    assertThat(MpTokenIssuanceSetMutableFlags.CLEAR_CAN_LOCK.getValue()).isEqualTo(0x00000002L);
    assertThat(MpTokenIssuanceSetMutableFlags.SET_REQUIRE_AUTH.getValue()).isEqualTo(0x00000004L);
    assertThat(MpTokenIssuanceSetMutableFlags.CLEAR_REQUIRE_AUTH.getValue()).isEqualTo(0x00000008L);
    assertThat(MpTokenIssuanceSetMutableFlags.SET_CAN_ESCROW.getValue()).isEqualTo(0x00000010L);
    assertThat(MpTokenIssuanceSetMutableFlags.CLEAR_CAN_ESCROW.getValue()).isEqualTo(0x00000020L);
    assertThat(MpTokenIssuanceSetMutableFlags.SET_CAN_TRADE.getValue()).isEqualTo(0x00000040L);
    assertThat(MpTokenIssuanceSetMutableFlags.CLEAR_CAN_TRADE.getValue()).isEqualTo(0x00000080L);
    assertThat(MpTokenIssuanceSetMutableFlags.SET_CAN_TRANSFER.getValue()).isEqualTo(0x00000100L);
    assertThat(MpTokenIssuanceSetMutableFlags.CLEAR_CAN_TRANSFER.getValue()).isEqualTo(0x00000200L);
    assertThat(MpTokenIssuanceSetMutableFlags.SET_CAN_CLAWBACK.getValue()).isEqualTo(0x00000400L);
    assertThat(MpTokenIssuanceSetMutableFlags.CLEAR_CAN_CLAWBACK.getValue()).isEqualTo(0x00000800L);
  }
}
