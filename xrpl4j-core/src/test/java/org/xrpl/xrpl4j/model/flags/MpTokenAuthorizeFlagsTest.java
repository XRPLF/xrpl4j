package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MpTokenAuthorizeFlagsTest {

  @Test
  void testEmpty() {
    assertThat(MpTokenAuthorizeFlags.empty().isEmpty()).isTrue();
  }

  @Test
  void tfMptUnauthorize() {
    assertThat(MpTokenAuthorizeFlags.UNAUTHORIZE.tfMptUnauthorize()).isTrue();
    assertThat(MpTokenAuthorizeFlags.empty().tfMptUnauthorize()).isFalse();
  }
}