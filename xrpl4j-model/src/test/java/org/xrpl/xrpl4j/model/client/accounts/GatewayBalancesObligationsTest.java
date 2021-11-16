package org.xrpl.xrpl4j.model.client.accounts;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class GatewayBalancesObligationsTest {

  @Test
  public void testDefaults() {
    assertThat(GatewayBalancesObligations.builder().build().balances()).isEmpty();
  }
}
