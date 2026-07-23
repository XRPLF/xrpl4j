package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class MpTokenIssuanceImmutableFlagsTest extends AbstractFlagsTest {

  public static Stream<Arguments> data() {
    return getBooleanCombinations(9);
  }

  @ParameterizedTest
  @MethodSource("data")
  void testDeriveIndividualFlagsFromFlags(
    boolean lsifMptCanLock,
    boolean lsifMptRequireAuth,
    boolean lsifMptCanEscrow,
    boolean lsifMptCanTrade,
    boolean lsifMptCanTransfer,
    boolean lsifMptCanClawback,
    boolean lsifMptCanHoldConfidentialBalance,
    boolean lsifMptMetadata,
    boolean lsifMptTransferFee
  ) {
    long expectedFlags =
      (lsifMptCanLock ? MpTokenIssuanceImmutableFlags.CAN_LOCK.getValue() : 0L) |
      (lsifMptRequireAuth ? MpTokenIssuanceImmutableFlags.REQUIRE_AUTH.getValue() : 0L) |
      (lsifMptCanEscrow ? MpTokenIssuanceImmutableFlags.CAN_ESCROW.getValue() : 0L) |
      (lsifMptCanTrade ? MpTokenIssuanceImmutableFlags.CAN_TRADE.getValue() : 0L) |
      (lsifMptCanTransfer ? MpTokenIssuanceImmutableFlags.CAN_TRANSFER.getValue() : 0L) |
      (lsifMptCanClawback ? MpTokenIssuanceImmutableFlags.CAN_CLAWBACK.getValue() : 0L) |
      (lsifMptCanHoldConfidentialBalance ?
        MpTokenIssuanceImmutableFlags.CAN_HOLD_CONFIDENTIAL_BALANCE.getValue() : 0L) |
      (lsifMptMetadata ? MpTokenIssuanceImmutableFlags.METADATA.getValue() : 0L) |
      (lsifMptTransferFee ? MpTokenIssuanceImmutableFlags.TRANSFER_FEE.getValue() : 0L);

    MpTokenIssuanceImmutableFlags flags = MpTokenIssuanceImmutableFlags.builder()
      .lsifMptCanLock(lsifMptCanLock)
      .lsifMptRequireAuth(lsifMptRequireAuth)
      .lsifMptCanEscrow(lsifMptCanEscrow)
      .lsifMptCanTrade(lsifMptCanTrade)
      .lsifMptCanTransfer(lsifMptCanTransfer)
      .lsifMptCanClawback(lsifMptCanClawback)
      .lsifMptCanHoldConfidentialBalance(lsifMptCanHoldConfidentialBalance)
      .lsifMptMetadata(lsifMptMetadata)
      .lsifMptTransferFee(lsifMptTransferFee)
      .build();

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.lsifMptCanLock()).isEqualTo(lsifMptCanLock);
    assertThat(flags.lsifMptRequireAuth()).isEqualTo(lsifMptRequireAuth);
    assertThat(flags.lsifMptCanEscrow()).isEqualTo(lsifMptCanEscrow);
    assertThat(flags.lsifMptCanTrade()).isEqualTo(lsifMptCanTrade);
    assertThat(flags.lsifMptCanTransfer()).isEqualTo(lsifMptCanTransfer);
    assertThat(flags.lsifMptCanClawback()).isEqualTo(lsifMptCanClawback);
    assertThat(flags.lsifMptCanHoldConfidentialBalance()).isEqualTo(lsifMptCanHoldConfidentialBalance);
    assertThat(flags.lsifMptMetadata()).isEqualTo(lsifMptMetadata);
    assertThat(flags.lsifMptTransferFee()).isEqualTo(lsifMptTransferFee);
  }

  @Test
  void testEmptyBuilderProducesZeroFlags() {
    MpTokenIssuanceImmutableFlags flags = MpTokenIssuanceImmutableFlags.builder().build();
    assertThat(flags.getValue()).isEqualTo(0L);
    assertThat(flags.lsifMptCanLock()).isFalse();
    assertThat(flags.lsifMptRequireAuth()).isFalse();
    assertThat(flags.lsifMptCanEscrow()).isFalse();
    assertThat(flags.lsifMptCanTrade()).isFalse();
    assertThat(flags.lsifMptCanTransfer()).isFalse();
    assertThat(flags.lsifMptCanClawback()).isFalse();
    assertThat(flags.lsifMptCanHoldConfidentialBalance()).isFalse();
    assertThat(flags.lsifMptMetadata()).isFalse();
    assertThat(flags.lsifMptTransferFee()).isFalse();
  }

  @Test
  void testOfRawValueRoundTrips() {
    long raw = MpTokenIssuanceImmutableFlags.METADATA.getValue() |
      MpTokenIssuanceImmutableFlags.CAN_LOCK.getValue();

    MpTokenIssuanceImmutableFlags flags = MpTokenIssuanceImmutableFlags.of(raw);

    assertThat(flags.getValue()).isEqualTo(raw);
    assertThat(flags.lsifMptMetadata()).isTrue();
    assertThat(flags.lsifMptCanLock()).isTrue();
    assertThat(flags.lsifMptTransferFee()).isFalse();
  }

  @Test
  void testStaticConstantBitValues() {
    assertThat(MpTokenIssuanceImmutableFlags.CAN_LOCK.getValue()).isEqualTo(0x00000002L);
    assertThat(MpTokenIssuanceImmutableFlags.REQUIRE_AUTH.getValue()).isEqualTo(0x00000004L);
    assertThat(MpTokenIssuanceImmutableFlags.CAN_ESCROW.getValue()).isEqualTo(0x00000008L);
    assertThat(MpTokenIssuanceImmutableFlags.CAN_TRADE.getValue()).isEqualTo(0x00000010L);
    assertThat(MpTokenIssuanceImmutableFlags.CAN_TRANSFER.getValue()).isEqualTo(0x00000020L);
    assertThat(MpTokenIssuanceImmutableFlags.CAN_CLAWBACK.getValue()).isEqualTo(0x00000040L);
    assertThat(MpTokenIssuanceImmutableFlags.CAN_HOLD_CONFIDENTIAL_BALANCE.getValue()).isEqualTo(0x00000080L);
    assertThat(MpTokenIssuanceImmutableFlags.METADATA.getValue()).isEqualTo(0x00010000L);
    assertThat(MpTokenIssuanceImmutableFlags.TRANSFER_FEE.getValue()).isEqualTo(0x00020000L);
  }

  @Test
  void testValidMask() {
    long expected = MpTokenIssuanceImmutableFlags.CAN_LOCK.getValue() |
      MpTokenIssuanceImmutableFlags.REQUIRE_AUTH.getValue() |
      MpTokenIssuanceImmutableFlags.CAN_ESCROW.getValue() |
      MpTokenIssuanceImmutableFlags.CAN_TRADE.getValue() |
      MpTokenIssuanceImmutableFlags.CAN_TRANSFER.getValue() |
      MpTokenIssuanceImmutableFlags.CAN_CLAWBACK.getValue() |
      MpTokenIssuanceImmutableFlags.CAN_HOLD_CONFIDENTIAL_BALANCE.getValue() |
      MpTokenIssuanceImmutableFlags.METADATA.getValue() |
      MpTokenIssuanceImmutableFlags.TRANSFER_FEE.getValue();

    assertThat(MpTokenIssuanceImmutableFlags.VALID_MASK).isEqualTo(expected);
    // Bit 0x1 (mirrors lsfMPTLocked) must not be part of the valid mask.
    assertThat(MpTokenIssuanceImmutableFlags.VALID_MASK & 0x1L).isEqualTo(0L);
  }
}
