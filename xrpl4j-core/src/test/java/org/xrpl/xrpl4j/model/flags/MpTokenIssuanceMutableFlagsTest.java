package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class MpTokenIssuanceMutableFlagsTest extends AbstractFlagsTest {

  public static Stream<Arguments> data() {
    return getBooleanCombinations(8);
  }

  @ParameterizedTest
  @MethodSource("data")
  void testDeriveIndividualFlagsFromFlags(
    boolean lsmfMptCanEnableCanLock,
    boolean lsmfMptCanEnableRequireAuth,
    boolean lsmfMptCanEnableCanEscrow,
    boolean lsmfMptCanEnableCanTrade,
    boolean lsmfMptCanEnableCanTransfer,
    boolean lsmfMptCanEnableCanClawback,
    boolean lsmfMptCanMutateMetadata,
    boolean lsmfMptCanMutateTransferFee
  ) {
    long expectedFlags =
      (lsmfMptCanEnableCanLock ? MpTokenIssuanceMutableFlags.CAN_ENABLE_CAN_LOCK.getValue() : 0L) |
      (lsmfMptCanEnableRequireAuth ? MpTokenIssuanceMutableFlags.CAN_ENABLE_REQUIRE_AUTH.getValue() : 0L) |
      (lsmfMptCanEnableCanEscrow ? MpTokenIssuanceMutableFlags.CAN_ENABLE_CAN_ESCROW.getValue() : 0L) |
      (lsmfMptCanEnableCanTrade ? MpTokenIssuanceMutableFlags.CAN_ENABLE_CAN_TRADE.getValue() : 0L) |
      (lsmfMptCanEnableCanTransfer ? MpTokenIssuanceMutableFlags.CAN_ENABLE_CAN_TRANSFER.getValue() : 0L) |
      (lsmfMptCanEnableCanClawback ? MpTokenIssuanceMutableFlags.CAN_ENABLE_CAN_CLAWBACK.getValue() : 0L) |
      (lsmfMptCanMutateMetadata ? MpTokenIssuanceMutableFlags.CAN_MUTATE_METADATA.getValue() : 0L) |
      (lsmfMptCanMutateTransferFee ? MpTokenIssuanceMutableFlags.CAN_MUTATE_TRANSFER_FEE.getValue() : 0L);

    MpTokenIssuanceMutableFlags flags = MpTokenIssuanceMutableFlags.builder()
      .lsmfMptCanEnableCanLock(lsmfMptCanEnableCanLock)
      .lsmfMptCanEnableRequireAuth(lsmfMptCanEnableRequireAuth)
      .lsmfMptCanEnableCanEscrow(lsmfMptCanEnableCanEscrow)
      .lsmfMptCanEnableCanTrade(lsmfMptCanEnableCanTrade)
      .lsmfMptCanEnableCanTransfer(lsmfMptCanEnableCanTransfer)
      .lsmfMptCanEnableCanClawback(lsmfMptCanEnableCanClawback)
      .lsmfMptCanMutateMetadata(lsmfMptCanMutateMetadata)
      .lsmfMptCanMutateTransferFee(lsmfMptCanMutateTransferFee)
      .build();

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.lsmfMptCanEnableCanLock()).isEqualTo(lsmfMptCanEnableCanLock);
    assertThat(flags.lsmfMptCanEnableRequireAuth()).isEqualTo(lsmfMptCanEnableRequireAuth);
    assertThat(flags.lsmfMptCanEnableCanEscrow()).isEqualTo(lsmfMptCanEnableCanEscrow);
    assertThat(flags.lsmfMptCanEnableCanTrade()).isEqualTo(lsmfMptCanEnableCanTrade);
    assertThat(flags.lsmfMptCanEnableCanTransfer()).isEqualTo(lsmfMptCanEnableCanTransfer);
    assertThat(flags.lsmfMptCanEnableCanClawback()).isEqualTo(lsmfMptCanEnableCanClawback);
    assertThat(flags.lsmfMptCanMutateMetadata()).isEqualTo(lsmfMptCanMutateMetadata);
    assertThat(flags.lsmfMptCanMutateTransferFee()).isEqualTo(lsmfMptCanMutateTransferFee);
  }

  @Test
  void testEmptyBuilderProducesZeroFlags() {
    MpTokenIssuanceMutableFlags flags = MpTokenIssuanceMutableFlags.builder().build();
    assertThat(flags.getValue()).isEqualTo(0L);
    assertThat(flags.lsmfMptCanEnableCanLock()).isFalse();
    assertThat(flags.lsmfMptCanEnableRequireAuth()).isFalse();
    assertThat(flags.lsmfMptCanEnableCanEscrow()).isFalse();
    assertThat(flags.lsmfMptCanEnableCanTrade()).isFalse();
    assertThat(flags.lsmfMptCanEnableCanTransfer()).isFalse();
    assertThat(flags.lsmfMptCanEnableCanClawback()).isFalse();
    assertThat(flags.lsmfMptCanMutateMetadata()).isFalse();
    assertThat(flags.lsmfMptCanMutateTransferFee()).isFalse();
  }

  @Test
  void testOfRawValueRoundTrips() {
    long raw = MpTokenIssuanceMutableFlags.CAN_MUTATE_METADATA.getValue() |
      MpTokenIssuanceMutableFlags.CAN_ENABLE_CAN_LOCK.getValue();

    MpTokenIssuanceMutableFlags flags = MpTokenIssuanceMutableFlags.of(raw);

    assertThat(flags.getValue()).isEqualTo(raw);
    assertThat(flags.lsmfMptCanMutateMetadata()).isTrue();
    assertThat(flags.lsmfMptCanEnableCanLock()).isTrue();
    assertThat(flags.lsmfMptCanMutateTransferFee()).isFalse();
  }

  @Test
  void testStaticConstantBitValues() {
    assertThat(MpTokenIssuanceMutableFlags.CAN_ENABLE_CAN_LOCK.getValue()).isEqualTo(0x00000002L);
    assertThat(MpTokenIssuanceMutableFlags.CAN_ENABLE_REQUIRE_AUTH.getValue()).isEqualTo(0x00000004L);
    assertThat(MpTokenIssuanceMutableFlags.CAN_ENABLE_CAN_ESCROW.getValue()).isEqualTo(0x00000008L);
    assertThat(MpTokenIssuanceMutableFlags.CAN_ENABLE_CAN_TRADE.getValue()).isEqualTo(0x00000010L);
    assertThat(MpTokenIssuanceMutableFlags.CAN_ENABLE_CAN_TRANSFER.getValue()).isEqualTo(0x00000020L);
    assertThat(MpTokenIssuanceMutableFlags.CAN_ENABLE_CAN_CLAWBACK.getValue()).isEqualTo(0x00000040L);
    assertThat(MpTokenIssuanceMutableFlags.CAN_MUTATE_METADATA.getValue()).isEqualTo(0x00010000L);
    assertThat(MpTokenIssuanceMutableFlags.CAN_MUTATE_TRANSFER_FEE.getValue()).isEqualTo(0x00020000L);
  }
}
