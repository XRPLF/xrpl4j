package org.xrpl.xrpl4j.model.client.accounts;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Address;

class AccountLinesResultTest {

  @Test
  void testWithLedgerIndex() {
    AccountLinesResult result = AccountLinesResult.builder()
      .account(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .status("success")
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.ONE))
      .build();
    assertThat(result.ledgerCurrentIndex()).isEmpty();
    assertThat(result.ledgerIndex()).isNotEmpty().get().isEqualTo(result.ledgerIndexSafe());
  }

  @Test
  void testWithLedgerCurrentIndex() {
    AccountLinesResult result = AccountLinesResult.builder()
      .account(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .status("success")
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.ONE))
      .build();

    assertThat(result.ledgerIndex()).isEmpty();
    assertThat(result.ledgerCurrentIndex()).isNotEmpty().get().isEqualTo(result.ledgerIndexSafe());
  }

}
