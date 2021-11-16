package org.xrpl.xrpl4j.model.client.accounts;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class GatewayBalancesHotWalletsTest {

  @Test
  public void testDefaults() {
    assertThat(GatewayBalancesHotWallets.builder().build().balancesByHolder()).isEmpty();
  }
}
