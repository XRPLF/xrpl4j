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
    boolean tmfMptCanEnableCanLock,
    boolean tmfMptCanEnableRequireAuth,
    boolean tmfMptCanEnableCanEscrow,
    boolean tmfMptCanEnableCanTrade,
    boolean tmfMptCanEnableCanTransfer,
    boolean tmfMptCanEnableCanClawback,
    boolean tmfMptCanMutateMetadata,
    boolean tmfMptCanMutateTransferFee
  ) {
    long expectedFlags =
      (tmfMptCanEnableCanLock ? MpTokenIssuanceCreateMutableFlags.CAN_ENABLE_CAN_LOCK.getValue() : 0L) |
      (tmfMptCanEnableRequireAuth ? MpTokenIssuanceCreateMutableFlags.CAN_ENABLE_REQUIRE_AUTH.getValue() : 0L) |
      (tmfMptCanEnableCanEscrow ? MpTokenIssuanceCreateMutableFlags.CAN_ENABLE_CAN_ESCROW.getValue() : 0L) |
      (tmfMptCanEnableCanTrade ? MpTokenIssuanceCreateMutableFlags.CAN_ENABLE_CAN_TRADE.getValue() : 0L) |
      (tmfMptCanEnableCanTransfer ? MpTokenIssuanceCreateMutableFlags.CAN_ENABLE_CAN_TRANSFER.getValue() : 0L) |
      (tmfMptCanEnableCanClawback ? MpTokenIssuanceCreateMutableFlags.CAN_ENABLE_CAN_CLAWBACK.getValue() : 0L) |
      (tmfMptCanMutateMetadata ? MpTokenIssuanceCreateMutableFlags.CAN_MUTATE_METADATA.getValue() : 0L) |
      (tmfMptCanMutateTransferFee ? MpTokenIssuanceCreateMutableFlags.CAN_MUTATE_TRANSFER_FEE.getValue() : 0L);

    MpTokenIssuanceCreateMutableFlags flags = MpTokenIssuanceCreateMutableFlags.builder()
      .tmfMptCanEnableCanLock(tmfMptCanEnableCanLock)
      .tmfMptCanEnableRequireAuth(tmfMptCanEnableRequireAuth)
      .tmfMptCanEnableCanEscrow(tmfMptCanEnableCanEscrow)
      .tmfMptCanEnableCanTrade(tmfMptCanEnableCanTrade)
      .tmfMptCanEnableCanTransfer(tmfMptCanEnableCanTransfer)
      .tmfMptCanEnableCanClawback(tmfMptCanEnableCanClawback)
      .tmfMptCanMutateMetadata(tmfMptCanMutateMetadata)
      .tmfMptCanMutateTransferFee(tmfMptCanMutateTransferFee)
      .build();

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.tmfMptCanEnableCanLock()).isEqualTo(tmfMptCanEnableCanLock);
    assertThat(flags.tmfMptCanEnableRequireAuth()).isEqualTo(tmfMptCanEnableRequireAuth);
    assertThat(flags.tmfMptCanEnableCanEscrow()).isEqualTo(tmfMptCanEnableCanEscrow);
    assertThat(flags.tmfMptCanEnableCanTrade()).isEqualTo(tmfMptCanEnableCanTrade);
    assertThat(flags.tmfMptCanEnableCanTransfer()).isEqualTo(tmfMptCanEnableCanTransfer);
    assertThat(flags.tmfMptCanEnableCanClawback()).isEqualTo(tmfMptCanEnableCanClawback);
    assertThat(flags.tmfMptCanMutateMetadata()).isEqualTo(tmfMptCanMutateMetadata);
    assertThat(flags.tmfMptCanMutateTransferFee()).isEqualTo(tmfMptCanMutateTransferFee);
  }

  @Test
  void testEmptyBuilderProducesZeroFlags() {
    MpTokenIssuanceCreateMutableFlags flags = MpTokenIssuanceCreateMutableFlags.builder().build();
    assertThat(flags.getValue()).isEqualTo(0L);
    assertThat(flags.tmfMptCanEnableCanLock()).isFalse();
    assertThat(flags.tmfMptCanEnableRequireAuth()).isFalse();
    assertThat(flags.tmfMptCanEnableCanEscrow()).isFalse();
    assertThat(flags.tmfMptCanEnableCanTrade()).isFalse();
    assertThat(flags.tmfMptCanEnableCanTransfer()).isFalse();
    assertThat(flags.tmfMptCanEnableCanClawback()).isFalse();
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
    assertThat(flags.tmfMptCanEnableCanLock()).isFalse();
  }

  @Test
  void testStaticConstantBitValues() {
    assertThat(MpTokenIssuanceCreateMutableFlags.CAN_ENABLE_CAN_LOCK.getValue()).isEqualTo(0x00000002L);
    assertThat(MpTokenIssuanceCreateMutableFlags.CAN_ENABLE_REQUIRE_AUTH.getValue()).isEqualTo(0x00000004L);
    assertThat(MpTokenIssuanceCreateMutableFlags.CAN_ENABLE_CAN_ESCROW.getValue()).isEqualTo(0x00000008L);
    assertThat(MpTokenIssuanceCreateMutableFlags.CAN_ENABLE_CAN_TRADE.getValue()).isEqualTo(0x00000010L);
    assertThat(MpTokenIssuanceCreateMutableFlags.CAN_ENABLE_CAN_TRANSFER.getValue()).isEqualTo(0x00000020L);
    assertThat(MpTokenIssuanceCreateMutableFlags.CAN_ENABLE_CAN_CLAWBACK.getValue()).isEqualTo(0x00000040L);
    assertThat(MpTokenIssuanceCreateMutableFlags.CAN_MUTATE_METADATA.getValue()).isEqualTo(0x00010000L);
    assertThat(MpTokenIssuanceCreateMutableFlags.CAN_MUTATE_TRANSFER_FEE.getValue()).isEqualTo(0x00020000L);
  }
}
