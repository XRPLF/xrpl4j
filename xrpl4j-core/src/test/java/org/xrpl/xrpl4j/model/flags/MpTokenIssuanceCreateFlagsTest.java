package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class MpTokenIssuanceCreateFlagsTest extends AbstractFlagsTest {

  public static Stream<Arguments> data() {
    return getBooleanCombinations(7);
  }

  @ParameterizedTest
  @MethodSource("data")
  public void testDeriveIndividualFlagsFromFlags(
    boolean tfMptCanLock,
    boolean tfMptRequireAuth,
    boolean tfMptCanEscrow,
    boolean tfMptCanTrade,
    boolean tfMptCanTransfer,
    boolean tfMptCanClawback,
    boolean tfInnerBatchTxn
  ) {
    long expectedFlags = (MpTokenIssuanceCreateFlags.FULLY_CANONICAL_SIG.getValue()) |
                         (tfMptCanLock ? MpTokenIssuanceCreateFlags.CAN_LOCK.getValue() : 0L) |
                         (tfMptRequireAuth ? MpTokenIssuanceCreateFlags.REQUIRE_AUTH.getValue() : 0L) |
                         (tfMptCanEscrow ? MpTokenIssuanceCreateFlags.CAN_ESCROW.getValue() : 0L) |
                         (tfMptCanTrade ? MpTokenIssuanceCreateFlags.CAN_TRADE.getValue() : 0L) |
                         (tfMptCanTransfer ? MpTokenIssuanceCreateFlags.CAN_TRANSFER.getValue() : 0L) |
                         (tfMptCanClawback ? MpTokenIssuanceCreateFlags.CAN_CLAWBACK.getValue() : 0L) |
                         (tfInnerBatchTxn ? TransactionFlags.INNER_BATCH_TXN.getValue() : 0L);

    MpTokenIssuanceCreateFlags flags = MpTokenIssuanceCreateFlags.builder()
      .tfMptCanLock(tfMptCanLock)
      .tfMptRequireAuth(tfMptRequireAuth)
      .tfMptCanEscrow(tfMptCanEscrow)
      .tfMptCanTrade(tfMptCanTrade)
      .tfMptCanTransfer(tfMptCanTransfer)
      .tfMptCanClawback(tfMptCanClawback)
      .tfInnerBatchTxn(tfInnerBatchTxn)
      .build();

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.tfMptCanLock()).isEqualTo(tfMptCanLock);
    assertThat(flags.tfMptRequireAuth()).isEqualTo(tfMptRequireAuth);
    assertThat(flags.tfMptCanEscrow()).isEqualTo(tfMptCanEscrow);
    assertThat(flags.tfMptCanTrade()).isEqualTo(tfMptCanTrade);
    assertThat(flags.tfMptCanTransfer()).isEqualTo(tfMptCanTransfer);
    assertThat(flags.tfMptCanClawback()).isEqualTo(tfMptCanClawback);
    assertThat(flags.tfInnerBatchTxn()).isEqualTo(tfInnerBatchTxn);
  }

  @Test
  void testInnerBatchTxn() {
    MpTokenIssuanceCreateFlags flags = MpTokenIssuanceCreateFlags.INNER_BATCH_TXN;
    assertThat(flags.isEmpty()).isFalse();
    assertThat(flags.tfInnerBatchTxn()).isTrue();
    assertThat(flags.tfMptCanLock()).isFalse();
    assertThat(flags.tfMptRequireAuth()).isFalse();
    assertThat(flags.tfMptCanEscrow()).isFalse();
    assertThat(flags.tfMptCanTrade()).isFalse();
    assertThat(flags.tfMptCanTransfer()).isFalse();
    assertThat(flags.tfMptCanClawback()).isFalse();
    assertThat(flags.getValue()).isEqualTo(TransactionFlags.INNER_BATCH_TXN.getValue());
  }

  @Test
  void testEmptyFlags() {
    MpTokenIssuanceCreateFlags flags = MpTokenIssuanceCreateFlags.empty();
    assertThat(flags.isEmpty()).isTrue();
    assertThat(flags.tfMptCanLock()).isFalse();
    assertThat(flags.tfMptRequireAuth()).isFalse();
    assertThat(flags.tfMptCanEscrow()).isFalse();
    assertThat(flags.tfMptCanTrade()).isFalse();
    assertThat(flags.tfMptCanTransfer()).isFalse();
    assertThat(flags.tfMptCanClawback()).isFalse();
    assertThat(flags.tfInnerBatchTxn()).isFalse();
    assertThat(flags.getValue()).isEqualTo(0L);
  }
}
