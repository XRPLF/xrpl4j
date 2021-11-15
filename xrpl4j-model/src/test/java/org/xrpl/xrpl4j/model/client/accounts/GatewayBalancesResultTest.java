package org.xrpl.xrpl4j.model.client.accounts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.transactions.Address;

public class GatewayBalancesResultTest {

  @Test
  public void testDefaults() {
    GatewayBalancesResult result = GatewayBalancesResult
      .builder()
      .account(Address.of("rMwjYedjc7qqtKYVLiAccJSmCwih4LnE2q"))
      .assets(GatewayBalancesAssets.builder().build())
      .balances(GatewayBalancesHotWallets.builder().build())
      .obligations(GatewayBalancesObligations.builder().build())
      .build();

    assertThat(result.validated()).isFalse();
    assertThat(result.ledgerHash()).isEmpty();
    assertThat(result.ledgerIndex()).isEmpty();
    assertThat(result.ledgerCurrentIndex()).isEmpty();
    assertThatThrownBy(result::ledgerCurrentIndexSafe).isInstanceOf(IllegalStateException.class);
    assertThatThrownBy(result::ledgerHashSafe).isInstanceOf(IllegalStateException.class);
    assertThatThrownBy(result::ledgerIndexSafe).isInstanceOf(IllegalStateException.class);
  }
}
