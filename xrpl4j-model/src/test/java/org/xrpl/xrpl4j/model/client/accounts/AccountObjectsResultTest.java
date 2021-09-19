package org.xrpl.xrpl4j.model.client.accounts;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Address;

class AccountObjectsResultTest {

  @Test
  void testWithLedgerIndex() {
    AccountObjectsResult result = AccountObjectsResult.builder()
      .account(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(14380380)))
      .validated(true)
      .status("success")
      .build();
    assertThat(result.ledgerCurrentIndex()).isEmpty();
    assertThat(result.ledgerIndex()).isNotEmpty().get().isEqualTo(result.ledgerIndexSafe());
  }

  @Test
  void testWithLedgerCurrentIndex() {
    AccountObjectsResult result = AccountObjectsResult.builder()
      .account(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.valueOf(14380380)))
      .validated(true)
      .status("success")
      .build();

    assertThat(result.ledgerIndex()).isEmpty();
    assertThat(result.ledgerCurrentIndex()).isNotEmpty().get().isEqualTo(result.ledgerIndexSafe());
  }

}
