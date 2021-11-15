package org.xrpl.xrpl4j.model.client.accounts;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class GatewayBalancesAssetsTest {

  @Test
  public void testDefaults() {
    assertThat(GatewayBalancesAssets.builder().build().balancesByIssuer()).isEmpty();
  }
}
