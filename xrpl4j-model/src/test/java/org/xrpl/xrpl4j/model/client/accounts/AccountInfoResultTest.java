package org.xrpl.xrpl4j.model.client.accounts;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.ledger.AccountRootObject;

class AccountInfoResultTest {

  @Test
  void testWithLedgerIndex() {
    AccountInfoResult result = AccountInfoResult.builder()
      .accountData(mock(AccountRootObject.class))
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(4)))
      .queueData(mock(QueueData.class))
      .status("success")
      .validated(true)
      .build();

    assertThat(result.ledgerIndex()).isNotEmpty().get().isEqualTo(result.ledgerIndexSafe());
    assertThat(result.ledgerCurrentIndex()).isEmpty();
    assertThrows(
      IllegalStateException.class,
      result::ledgerCurrentIndexSafe
    );
  }

  @Test
  void testWithLedgerCurrentIndex() {
    AccountInfoResult result = AccountInfoResult.builder()
      .accountData(mock(AccountRootObject.class))
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.valueOf(4)))
      .queueData(mock(QueueData.class))
      .status("success")
      .validated(true)
      .build();

    assertThat(result.ledgerIndex()).isEmpty();
    assertThat(result.ledgerCurrentIndex()).isNotEmpty().get().isEqualTo(result.ledgerCurrentIndexSafe());
    assertThrows(
      IllegalStateException.class,
      result::ledgerIndexSafe
    );
  }

}
