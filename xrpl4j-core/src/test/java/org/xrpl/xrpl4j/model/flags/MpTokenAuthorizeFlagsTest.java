package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MpTokenAuthorizeFlagsTest {

  @Test
  void testEmpty() {
    assertThat(MpTokenAuthorizeFlags.empty().isEmpty()).isTrue();
    assertThat(MpTokenAuthorizeFlags.empty().tfInnerBatchTxn()).isFalse();
  }

  @Test
  void tfMptUnauthorize() {
    assertThat(MpTokenAuthorizeFlags.UNAUTHORIZE.tfMptUnauthorize()).isTrue();
    assertThat(MpTokenAuthorizeFlags.UNAUTHORIZE.tfInnerBatchTxn()).isFalse();
    assertThat(MpTokenAuthorizeFlags.empty().tfMptUnauthorize()).isFalse();
  }

  @Test
  void testInnerBatchTxn() {
    MpTokenAuthorizeFlags flags = MpTokenAuthorizeFlags.INNER_BATCH_TXN;
    assertThat(flags.isEmpty()).isFalse();
    assertThat(flags.tfInnerBatchTxn()).isTrue();
    assertThat(flags.tfMptUnauthorize()).isFalse();
  }
}
