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
}
