package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FlagsTest {

  @Test
  void testEqualsForSameFlagsObject() {
    Flags flags = Flags.of(TransactionFlags.FULLY_CANONICAL_SIG);
    assertThat(flags).isEqualTo(flags);
  }

  @Test
  void testHashCode() {
    Flags flags = Flags.of(TransactionFlags.FULLY_CANONICAL_SIG);
    assertThat(flags.hashCode()).isEqualTo(-2147483617);
  }

  @Test
  void testToString() {
    Flags flags = Flags.of(TransactionFlags.FULLY_CANONICAL_SIG);
    assertThat(flags.toString()).isEqualTo(String.valueOf(flags.getValue()));
  }
}