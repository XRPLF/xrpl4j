package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MpTokenIssuanceSetFlagsTest {

  @Test
  void testEmpty() {
    assertThat(MpTokenIssuanceSetFlags.empty().isEmpty()).isTrue();
    assertThat(MpTokenIssuanceSetFlags.empty().tfInnerBatchTxn()).isFalse();
  }

  @Test
  void testGetters() {
    assertThat(MpTokenIssuanceSetFlags.LOCK.tfMptLock()).isTrue();
    assertThat(MpTokenIssuanceSetFlags.LOCK.tfMptUnlock()).isFalse();
    assertThat(MpTokenIssuanceSetFlags.LOCK.tfInnerBatchTxn()).isFalse();
    assertThat(MpTokenIssuanceSetFlags.UNLOCK.tfMptLock()).isFalse();
    assertThat(MpTokenIssuanceSetFlags.UNLOCK.tfMptUnlock()).isTrue();
    assertThat(MpTokenIssuanceSetFlags.UNLOCK.tfInnerBatchTxn()).isFalse();
    assertThat(MpTokenIssuanceSetFlags.empty().tfMptLock()).isFalse();
    assertThat(MpTokenIssuanceSetFlags.empty().tfMptUnlock()).isFalse();
  }

  @Test
  void testInnerBatchTxn() {
    MpTokenIssuanceSetFlags flags = MpTokenIssuanceSetFlags.INNER_BATCH_TXN;
    assertThat(flags.isEmpty()).isFalse();
    assertThat(flags.tfInnerBatchTxn()).isTrue();
    assertThat(flags.tfMptLock()).isFalse();
    assertThat(flags.tfMptUnlock()).isFalse();
    assertThat(flags.getValue()).isEqualTo(TransactionFlags.INNER_BATCH_TXN.getValue());
  }

  @Test
  void testSetCanEnableGetters() {
    assertThat(MpTokenIssuanceSetFlags.SET_CAN_LOCK.tfMptSetCanLock()).isTrue();
    assertThat(MpTokenIssuanceSetFlags.SET_CAN_LOCK.getValue()).isEqualTo(0x00000004L);
    assertThat(MpTokenIssuanceSetFlags.SET_REQUIRE_AUTH.tfMptSetRequireAuth()).isTrue();
    assertThat(MpTokenIssuanceSetFlags.SET_REQUIRE_AUTH.getValue()).isEqualTo(0x00000008L);
    assertThat(MpTokenIssuanceSetFlags.SET_CAN_ESCROW.tfMptSetCanEscrow()).isTrue();
    assertThat(MpTokenIssuanceSetFlags.SET_CAN_ESCROW.getValue()).isEqualTo(0x00000010L);
    assertThat(MpTokenIssuanceSetFlags.SET_CAN_TRADE.tfMptSetCanTrade()).isTrue();
    assertThat(MpTokenIssuanceSetFlags.SET_CAN_TRADE.getValue()).isEqualTo(0x00000020L);
    assertThat(MpTokenIssuanceSetFlags.SET_CAN_TRANSFER.tfMptSetCanTransfer()).isTrue();
    assertThat(MpTokenIssuanceSetFlags.SET_CAN_TRANSFER.getValue()).isEqualTo(0x00000040L);
    assertThat(MpTokenIssuanceSetFlags.SET_CAN_CLAWBACK.tfMptSetCanClawback()).isTrue();
    assertThat(MpTokenIssuanceSetFlags.SET_CAN_CLAWBACK.getValue()).isEqualTo(0x00000080L);
    assertThat(MpTokenIssuanceSetFlags.SET_CAN_HOLD_CONFIDENTIAL_BALANCE.tfMptSetCanHoldConfidentialBalance())
      .isTrue();
    assertThat(MpTokenIssuanceSetFlags.SET_CAN_HOLD_CONFIDENTIAL_BALANCE.getValue()).isEqualTo(0x00000100L);

    assertThat(MpTokenIssuanceSetFlags.empty().tfMptSetCanLock()).isFalse();
    assertThat(MpTokenIssuanceSetFlags.empty().tfMptSetRequireAuth()).isFalse();
    assertThat(MpTokenIssuanceSetFlags.empty().tfMptSetCanEscrow()).isFalse();
    assertThat(MpTokenIssuanceSetFlags.empty().tfMptSetCanTrade()).isFalse();
    assertThat(MpTokenIssuanceSetFlags.empty().tfMptSetCanTransfer()).isFalse();
    assertThat(MpTokenIssuanceSetFlags.empty().tfMptSetCanClawback()).isFalse();
    assertThat(MpTokenIssuanceSetFlags.empty().tfMptSetCanHoldConfidentialBalance()).isFalse();
  }

  @Test
  void testBuilder() {
    MpTokenIssuanceSetFlags flags = MpTokenIssuanceSetFlags.builder()
      .tfMptLock(true)
      .tfMptSetCanLock(true)
      .tfMptSetRequireAuth(true)
      .tfMptSetCanEscrow(true)
      .tfMptSetCanTrade(true)
      .tfMptSetCanTransfer(true)
      .tfMptSetCanClawback(true)
      .tfMptSetCanHoldConfidentialBalance(true)
      .build();

    assertThat(flags.tfMptLock()).isTrue();
    assertThat(flags.tfMptUnlock()).isFalse();
    assertThat(flags.tfMptSetCanLock()).isTrue();
    assertThat(flags.tfMptSetRequireAuth()).isTrue();
    assertThat(flags.tfMptSetCanEscrow()).isTrue();
    assertThat(flags.tfMptSetCanTrade()).isTrue();
    assertThat(flags.tfMptSetCanTransfer()).isTrue();
    assertThat(flags.tfMptSetCanClawback()).isTrue();
    assertThat(flags.tfMptSetCanHoldConfidentialBalance()).isTrue();

    MpTokenIssuanceSetFlags emptyBuilt = MpTokenIssuanceSetFlags.builder().build();
    assertThat(emptyBuilt.tfMptLock()).isFalse();
    assertThat(emptyBuilt.tfMptUnlock()).isFalse();
    assertThat(emptyBuilt.tfMptSetCanLock()).isFalse();
  }
}
