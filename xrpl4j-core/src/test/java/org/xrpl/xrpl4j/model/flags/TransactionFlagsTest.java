package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TransactionFlagsTest {

  @Test
  void testFlags() {
    TransactionFlags flags = new TransactionFlags.Builder().build();
    assertThat(flags.tfFullyCanonicalSig()).isTrue();
  }
}