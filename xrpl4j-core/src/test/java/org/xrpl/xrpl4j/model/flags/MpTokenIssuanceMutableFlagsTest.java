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
    boolean lsmfMptCanMutateCanLock,
    boolean lsmfMptCanMutateRequireAuth,
    boolean lsmfMptCanMutateCanEscrow,
    boolean lsmfMptCanMutateCanTrade,
    boolean lsmfMptCanMutateCanTransfer,
    boolean lsmfMptCanMutateCanClawback,
    boolean lsmfMptCanMutateMetadata,
    boolean lsmfMptCanMutateTransferFee
  ) {
    long expectedFlags =
      (lsmfMptCanMutateCanLock ? MpTokenIssuanceMutableFlags.CAN_MUTATE_CAN_LOCK.getValue() : 0L) |
      (lsmfMptCanMutateRequireAuth ? MpTokenIssuanceMutableFlags.CAN_MUTATE_REQUIRE_AUTH.getValue() : 0L) |
      (lsmfMptCanMutateCanEscrow ? MpTokenIssuanceMutableFlags.CAN_MUTATE_CAN_ESCROW.getValue() : 0L) |
      (lsmfMptCanMutateCanTrade ? MpTokenIssuanceMutableFlags.CAN_MUTATE_CAN_TRADE.getValue() : 0L) |
      (lsmfMptCanMutateCanTransfer ? MpTokenIssuanceMutableFlags.CAN_MUTATE_CAN_TRANSFER.getValue() : 0L) |
      (lsmfMptCanMutateCanClawback ? MpTokenIssuanceMutableFlags.CAN_MUTATE_CAN_CLAWBACK.getValue() : 0L) |
      (lsmfMptCanMutateMetadata ? MpTokenIssuanceMutableFlags.CAN_MUTATE_METADATA.getValue() : 0L) |
      (lsmfMptCanMutateTransferFee ? MpTokenIssuanceMutableFlags.CAN_MUTATE_TRANSFER_FEE.getValue() : 0L);

    MpTokenIssuanceMutableFlags flags = MpTokenIssuanceMutableFlags.builder()
      .lsmfMptCanMutateCanLock(lsmfMptCanMutateCanLock)
      .lsmfMptCanMutateRequireAuth(lsmfMptCanMutateRequireAuth)
      .lsmfMptCanMutateCanEscrow(lsmfMptCanMutateCanEscrow)
      .lsmfMptCanMutateCanTrade(lsmfMptCanMutateCanTrade)
      .lsmfMptCanMutateCanTransfer(lsmfMptCanMutateCanTransfer)
      .lsmfMptCanMutateCanClawback(lsmfMptCanMutateCanClawback)
      .lsmfMptCanMutateMetadata(lsmfMptCanMutateMetadata)
      .lsmfMptCanMutateTransferFee(lsmfMptCanMutateTransferFee)
      .build();

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.lsmfMptCanMutateCanLock()).isEqualTo(lsmfMptCanMutateCanLock);
    assertThat(flags.lsmfMptCanMutateRequireAuth()).isEqualTo(lsmfMptCanMutateRequireAuth);
    assertThat(flags.lsmfMptCanMutateCanEscrow()).isEqualTo(lsmfMptCanMutateCanEscrow);
    assertThat(flags.lsmfMptCanMutateCanTrade()).isEqualTo(lsmfMptCanMutateCanTrade);
    assertThat(flags.lsmfMptCanMutateCanTransfer()).isEqualTo(lsmfMptCanMutateCanTransfer);
    assertThat(flags.lsmfMptCanMutateCanClawback()).isEqualTo(lsmfMptCanMutateCanClawback);
    assertThat(flags.lsmfMptCanMutateMetadata()).isEqualTo(lsmfMptCanMutateMetadata);
    assertThat(flags.lsmfMptCanMutateTransferFee()).isEqualTo(lsmfMptCanMutateTransferFee);
  }

  @Test
  void testEmptyBuilderProducesZeroFlags() {
    MpTokenIssuanceMutableFlags flags = MpTokenIssuanceMutableFlags.builder().build();
    assertThat(flags.getValue()).isEqualTo(0L);
    assertThat(flags.lsmfMptCanMutateCanLock()).isFalse();
    assertThat(flags.lsmfMptCanMutateRequireAuth()).isFalse();
    assertThat(flags.lsmfMptCanMutateCanEscrow()).isFalse();
    assertThat(flags.lsmfMptCanMutateCanTrade()).isFalse();
    assertThat(flags.lsmfMptCanMutateCanTransfer()).isFalse();
    assertThat(flags.lsmfMptCanMutateCanClawback()).isFalse();
    assertThat(flags.lsmfMptCanMutateMetadata()).isFalse();
    assertThat(flags.lsmfMptCanMutateTransferFee()).isFalse();
  }

  @Test
  void testOfRawValueRoundTrips() {
    long raw = MpTokenIssuanceMutableFlags.CAN_MUTATE_METADATA.getValue() |
      MpTokenIssuanceMutableFlags.CAN_MUTATE_CAN_LOCK.getValue();

    MpTokenIssuanceMutableFlags flags = MpTokenIssuanceMutableFlags.of(raw);

    assertThat(flags.getValue()).isEqualTo(raw);
    assertThat(flags.lsmfMptCanMutateMetadata()).isTrue();
    assertThat(flags.lsmfMptCanMutateCanLock()).isTrue();
    assertThat(flags.lsmfMptCanMutateTransferFee()).isFalse();
  }

  @Test
  void testStaticConstantBitValues() {
    assertThat(MpTokenIssuanceMutableFlags.CAN_MUTATE_CAN_LOCK.getValue()).isEqualTo(0x00000002L);
    assertThat(MpTokenIssuanceMutableFlags.CAN_MUTATE_REQUIRE_AUTH.getValue()).isEqualTo(0x00000004L);
    assertThat(MpTokenIssuanceMutableFlags.CAN_MUTATE_CAN_ESCROW.getValue()).isEqualTo(0x00000008L);
    assertThat(MpTokenIssuanceMutableFlags.CAN_MUTATE_CAN_TRADE.getValue()).isEqualTo(0x00000010L);
    assertThat(MpTokenIssuanceMutableFlags.CAN_MUTATE_CAN_TRANSFER.getValue()).isEqualTo(0x00000020L);
    assertThat(MpTokenIssuanceMutableFlags.CAN_MUTATE_CAN_CLAWBACK.getValue()).isEqualTo(0x00000040L);
    assertThat(MpTokenIssuanceMutableFlags.CAN_MUTATE_METADATA.getValue()).isEqualTo(0x00010000L);
    assertThat(MpTokenIssuanceMutableFlags.CAN_MUTATE_TRANSFER_FEE.getValue()).isEqualTo(0x00020000L);
  }
}
