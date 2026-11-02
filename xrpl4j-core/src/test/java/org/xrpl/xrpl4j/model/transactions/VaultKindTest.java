package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link VaultKind}.
 */
class VaultKindTest {

  @Test
  void forValue() {
    assertThat(VaultKind.forValue(0)).isEqualTo(VaultKind.OPEN_ENDED);
    assertThat(VaultKind.forValue(1)).isEqualTo(VaultKind.CLOSED_ENDED);
    assertThatThrownBy(() -> VaultKind.forValue(2))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("No matching VaultKind for value 2");
  }

  @Test
  void openEndedFilter() {
    VaultKind.OpenEndedFilter filter = new VaultKind.OpenEndedFilter();
    assertThat(filter.equals(VaultKind.OPEN_ENDED)).isTrue();
    assertThat(filter.equals(VaultKind.CLOSED_ENDED)).isFalse();
    assertThat(filter.hashCode()).isEqualTo(VaultKind.OPEN_ENDED.hashCode());
  }
}
