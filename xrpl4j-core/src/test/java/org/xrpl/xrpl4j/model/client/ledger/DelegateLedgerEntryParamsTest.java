package org.xrpl.xrpl4j.model.client.ledger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.transactions.Address;

public class DelegateLedgerEntryParamsTest {

  @Test
  public void testDelegateLedgerEntryParams() {
    DelegateLedgerEntryParams delegateLedgerEntryParams = DelegateLedgerEntryParams.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMtthP4"))
      .authorize(Address.of("rfmDuhDyLGgx94qiwf3YF8BUV5j6KSvE8"))
      .build();

    assertThat(delegateLedgerEntryParams.account()).isEqualTo(Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMtthP4"));
    assertThat(delegateLedgerEntryParams.authorize()).isEqualTo(Address.of("rfmDuhDyLGgx94qiwf3YF8BUV5j6KSvE8"));
  }
}

