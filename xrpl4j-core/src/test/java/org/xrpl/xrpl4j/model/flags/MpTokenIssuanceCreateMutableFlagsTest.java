package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class MpTokenIssuanceCreateMutableFlagsTest extends AbstractFlagsTest {

  public static Stream<Arguments> data() {
    return getBooleanCombinations(8);
  }

  @ParameterizedTest
  @MethodSource("data")
  void testDeriveIndividualFlagsFromFlags(
    boolean tmfMptCanMutateCanLock,
    boolean tmfMptCanMutateRequireAuth,
    boolean tmfMptCanMutateCanEscrow,
    boolean tmfMptCanMutateCanTrade,
    boolean tmfMptCanMutateCanTransfer,
    boolean tmfMptCanMutateCanClawback,
    boolean tmfMptCanMutateMetadata,
    boolean tmfMptCanMutateTransferFee
  ) {
    long expectedFlags =
      (tmfMptCanMutateCanLock ? MpTokenIssuanceCreateMutableFlags.CAN_MUTATE_CAN_LOCK.getValue() : 0L) |
      (tmfMptCanMutateRequireAuth ? MpTokenIssuanceCreateMutableFlags.CAN_MUTATE_REQUIRE_AUTH.getValue() : 0L) |
      (tmfMptCanMutateCanEscrow ? MpTokenIssuanceCreateMutableFlags.CAN_MUTATE_CAN_ESCROW.getValue() : 0L) |
      (tmfMptCanMutateCanTrade ? MpTokenIssuanceCreateMutableFlags.CAN_MUTATE_CAN_TRADE.getValue() : 0L) |
      (tmfMptCanMutateCanTransfer ? MpTokenIssuanceCreateMutableFlags.CAN_MUTATE_CAN_TRANSFER.getValue() : 0L) |
      (tmfMptCanMutateCanClawback ? MpTokenIssuanceCreateMutableFlags.CAN_MUTATE_CAN_CLAWBACK.getValue() : 0L) |
      (tmfMptCanMutateMetadata ? MpTokenIssuanceCreateMutableFlags.CAN_MUTATE_METADATA.getValue() : 0L) |
      (tmfMptCanMutateTransferFee ? MpTokenIssuanceCreateMutableFlags.CAN_MUTATE_TRANSFER_FEE.getValue() : 0L);

    MpTokenIssuanceCreateMutableFlags flags = MpTokenIssuanceCreateMutableFlags.builder()
      .tmfMptCanMutateCanLock(tmfMptCanMutateCanLock)
      .tmfMptCanMutateRequireAuth(tmfMptCanMutateRequireAuth)
      .tmfMptCanMutateCanEscrow(tmfMptCanMutateCanEscrow)
      .tmfMptCanMutateCanTrade(tmfMptCanMutateCanTrade)
      .tmfMptCanMutateCanTransfer(tmfMptCanMutateCanTransfer)
      .tmfMptCanMutateCanClawback(tmfMptCanMutateCanClawback)
      .tmfMptCanMutateMetadata(tmfMptCanMutateMetadata)
      .tmfMptCanMutateTransferFee(tmfMptCanMutateTransferFee)
      .build();

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.tmfMptCanMutateCanLock()).isEqualTo(tmfMptCanMutateCanLock);
    assertThat(flags.tmfMptCanMutateRequireAuth()).isEqualTo(tmfMptCanMutateRequireAuth);
    assertThat(flags.tmfMptCanMutateCanEscrow()).isEqualTo(tmfMptCanMutateCanEscrow);
    assertThat(flags.tmfMptCanMutateCanTrade()).isEqualTo(tmfMptCanMutateCanTrade);
    assertThat(flags.tmfMptCanMutateCanTransfer()).isEqualTo(tmfMptCanMutateCanTransfer);
    assertThat(flags.tmfMptCanMutateCanClawback()).isEqualTo(tmfMptCanMutateCanClawback);
    assertThat(flags.tmfMptCanMutateMetadata()).isEqualTo(tmfMptCanMutateMetadata);
    assertThat(flags.tmfMptCanMutateTransferFee()).isEqualTo(tmfMptCanMutateTransferFee);
  }

  @Test
  void testEmptyBuilderProducesZeroFlags() {
    MpTokenIssuanceCreateMutableFlags flags = MpTokenIssuanceCreateMutableFlags.builder().build();
    assertThat(flags.getValue()).isEqualTo(0L);
    assertThat(flags.tmfMptCanMutateCanLock()).isFalse();
    assertThat(flags.tmfMptCanMutateRequireAuth()).isFalse();
    assertThat(flags.tmfMptCanMutateCanEscrow()).isFalse();
    assertThat(flags.tmfMptCanMutateCanTrade()).isFalse();
    assertThat(flags.tmfMptCanMutateCanTransfer()).isFalse();
    assertThat(flags.tmfMptCanMutateCanClawback()).isFalse();
    assertThat(flags.tmfMptCanMutateMetadata()).isFalse();
    assertThat(flags.tmfMptCanMutateTransferFee()).isFalse();
  }

  @Test
  void testOfRawValueRoundTrips() {
    long raw = MpTokenIssuanceCreateMutableFlags.CAN_MUTATE_METADATA.getValue() |
      MpTokenIssuanceCreateMutableFlags.CAN_MUTATE_TRANSFER_FEE.getValue();

    MpTokenIssuanceCreateMutableFlags flags = MpTokenIssuanceCreateMutableFlags.of(raw);

    assertThat(flags.getValue()).isEqualTo(raw);
    assertThat(flags.tmfMptCanMutateMetadata()).isTrue();
    assertThat(flags.tmfMptCanMutateTransferFee()).isTrue();
    assertThat(flags.tmfMptCanMutateCanLock()).isFalse();
  }

  @Test
  void testStaticConstantBitValues() {
    assertThat(MpTokenIssuanceCreateMutableFlags.CAN_MUTATE_CAN_LOCK.getValue()).isEqualTo(0x00000002L);
    assertThat(MpTokenIssuanceCreateMutableFlags.CAN_MUTATE_REQUIRE_AUTH.getValue()).isEqualTo(0x00000004L);
    assertThat(MpTokenIssuanceCreateMutableFlags.CAN_MUTATE_CAN_ESCROW.getValue()).isEqualTo(0x00000008L);
    assertThat(MpTokenIssuanceCreateMutableFlags.CAN_MUTATE_CAN_TRADE.getValue()).isEqualTo(0x00000010L);
    assertThat(MpTokenIssuanceCreateMutableFlags.CAN_MUTATE_CAN_TRANSFER.getValue()).isEqualTo(0x00000020L);
    assertThat(MpTokenIssuanceCreateMutableFlags.CAN_MUTATE_CAN_CLAWBACK.getValue()).isEqualTo(0x00000040L);
    assertThat(MpTokenIssuanceCreateMutableFlags.CAN_MUTATE_METADATA.getValue()).isEqualTo(0x00010000L);
    assertThat(MpTokenIssuanceCreateMutableFlags.CAN_MUTATE_TRANSFER_FEE.getValue()).isEqualTo(0x00020000L);
  }
}
