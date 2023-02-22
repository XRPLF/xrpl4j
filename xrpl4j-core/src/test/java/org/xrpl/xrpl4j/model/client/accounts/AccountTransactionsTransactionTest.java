package org.xrpl.xrpl4j.model.client.accounts;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Payment;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

class AccountTransactionsTransactionTest {

  @Test
  void testCloseTimeHuman() {
    AccountTransactionsTransaction<Payment> payment = AccountTransactionsTransaction.<Payment>builder()
      .transaction(mock(Payment.class))
      .hash(Hash256.of("E939C30F233E3E6B0A9F829BDDA258CB9DA38D11C0F66C7D60E38B9D9FA987B8"))
      .closeDate(UnsignedLong.valueOf(666212460))
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.ONE))
      .build();

    assertThat(payment.closeDateHuman()).hasValue(
      ZonedDateTime.of(LocalDateTime.of(2021, 2, 9, 19, 1, 0), ZoneId.of("UTC"))
    );
  }

}