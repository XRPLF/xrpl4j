package org.xrpl.xrpl4j.model.client.ledger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.client.accounts.AccountLinesResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.ledger.LedgerHeader;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;

class LedgerResultTest {

  @Test
  void testWithHash() {
    LedgerResult result = LedgerResult.builder()
      .status("success")
      .ledgerHash(Hash256.of("3652D7FD0576BC452C0D2E9B747BDD733075971D1A9A1D98125055DEF428721A"))
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(54300940)))
      .validated(true)
      .ledger(mock(LedgerHeader.class))
      .build();

    assertThat(result.ledgerHash()).isNotEmpty().get().isEqualTo(result.ledgerHashSafe());
  }

  @Test
  void testWithoutHash() {
    LedgerResult result = LedgerResult.builder()
      .status("success")
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(54300940)))
      .validated(true)
      .ledger(mock(LedgerHeader.class))
      .build();

    assertThat(result.ledgerHash()).isEmpty();
    assertThrows(
      IllegalStateException.class,
      result::ledgerHashSafe
    );
  }

  @Test
  void testWithLedgerIndex() {
    LedgerResult result = LedgerResult.builder()
      .status("success")
      .ledgerHash(Hash256.of("3652D7FD0576BC452C0D2E9B747BDD733075971D1A9A1D98125055DEF428721A"))
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(54300940)))
      .validated(true)
      .ledger(mock(LedgerHeader.class))
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
    LedgerResult result = LedgerResult.builder()
      .status("success")
      .ledgerHash(Hash256.of("3652D7FD0576BC452C0D2E9B747BDD733075971D1A9A1D98125055DEF428721A"))
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.valueOf(54300940)))
      .validated(true)
      .ledger(mock(LedgerHeader.class))
      .build();

    assertThat(result.ledgerIndex()).isEmpty();
    assertThat(result.ledgerCurrentIndex()).isNotEmpty().get().isEqualTo(result.ledgerCurrentIndexSafe());
    assertThrows(
      IllegalStateException.class,
      result::ledgerIndexSafe
    );
  }

}
