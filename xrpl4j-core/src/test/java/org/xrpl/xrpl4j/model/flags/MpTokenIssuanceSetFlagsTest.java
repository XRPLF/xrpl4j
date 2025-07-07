package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class MpTokenIssuanceSetFlagsTest {

  @Test
  void testEmpty() {
    assertThat(MpTokenIssuanceSetFlags.empty().isEmpty()).isTrue();
  }

  @Test
  void testGetters() {
    assertThat(MpTokenIssuanceSetFlags.LOCK.tfMptLock()).isTrue();
    assertThat(MpTokenIssuanceSetFlags.LOCK.tfMptUnlock()).isFalse();
    assertThat(MpTokenIssuanceSetFlags.UNLOCK.tfMptLock()).isFalse();
    assertThat(MpTokenIssuanceSetFlags.UNLOCK.tfMptUnlock()).isTrue();
    assertThat(MpTokenIssuanceSetFlags.empty().tfMptLock()).isFalse();
    assertThat(MpTokenIssuanceSetFlags.empty().tfMptUnlock()).isFalse();
  }
}