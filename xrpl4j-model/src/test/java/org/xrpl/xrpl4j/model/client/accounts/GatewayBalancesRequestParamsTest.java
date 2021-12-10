package org.xrpl.xrpl4j.model.client.accounts;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.transactions.Address;

public class GatewayBalancesRequestParamsTest {

  @Test
  public void testDefaults() {
    GatewayBalancesRequestParams params = GatewayBalancesRequestParams
      .builder()
      .account(Address.of("rMwjYedjc7qqtKYVLiAccJSmCwih4LnE2q"))
      .build();

    assertThat(params.strict()).isTrue();
    assertThat(params.ledgerSpecifier()).isNull();
    assertThat(params.hotWallets()).isEmpty();
  }
}
