package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * Unit tests for {@link Hash256}.
 */
public class Hash256Test {

  @Test
  public void hashEquality() {
    assertThat(Hash256.of("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"))
      .isEqualTo(Hash256.of("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"));

    assertThat(Hash256.of("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"))
      .isEqualTo(Hash256.of("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"));

    assertThat(Hash256.of("0000000000000000000000000000000000000000000000000000000000000000"))
      .isNotEqualTo(Hash256.of("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"));
  }

  @Test
  public void hashHashcode() {
    assertThat(Hash256.of("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF").hashCode())
      .isEqualTo(Hash256.of("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF").hashCode());

    assertThat(Hash256.of("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF").hashCode())
      .isEqualTo(Hash256.of("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff").hashCode());

    assertThat(Hash256.of("0000000000000000000000000000000000000000000000000000000000000000").hashCode())
      .isNotEqualTo(Hash256.of("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF").hashCode());
  }

}
