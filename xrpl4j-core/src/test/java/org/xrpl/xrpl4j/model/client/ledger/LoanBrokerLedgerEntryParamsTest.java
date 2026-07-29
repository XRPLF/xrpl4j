package org.xrpl.xrpl4j.model.client.ledger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.transactions.Address;

public class LoanBrokerLedgerEntryParamsTest {

  @Test
  public void testLoanBrokerLedgerEntryParams() {
    LoanBrokerLedgerEntryParams params = LoanBrokerLedgerEntryParams.builder()
      .owner(Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMgk5j"))
      .seq(UnsignedInteger.valueOf(123))
      .build();

    assertThat(params.owner()).isEqualTo(Address.of("rN7n7otQDd6FczFgLdlqtyMVrn3HMgk5j"));
    assertThat(params.seq()).isEqualTo(UnsignedInteger.valueOf(123));
  }
}
