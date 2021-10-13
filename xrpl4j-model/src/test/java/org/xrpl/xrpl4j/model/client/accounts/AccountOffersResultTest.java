package org.xrpl.xrpl4j.model.client.accounts;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;

class AccountOffersResultTest {

  @Test
  void testWithHash() {
    AccountOffersResult result = AccountOffersResult.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .ledgerHash(Hash256.of("B9D3D80EDF4083A06B2D51202E0BFB63C46FC0985E015D06767C21A62853BF6D"))
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(37230600)))
      .status("success")
      .build();

    assertThat(result.ledgerHash()).isNotEmpty().get().isEqualTo(result.ledgerHashSafe());
  }

  @Test
  void testWithoutHash() {
    AccountOffersResult result = AccountOffersResult.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(37230600)))
      .status("success")
      .build();

    assertThat(result.ledgerHash()).isEmpty();
    assertThrows(
      IllegalStateException.class,
      result::ledgerHashSafe
    );
  }

  @Test
  void testWithLedgerIndex() {
    AccountOffersResult result = AccountOffersResult.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .ledgerHash(Hash256.of("B9D3D80EDF4083A06B2D51202E0BFB63C46FC0985E015D06767C21A62853BF6D"))
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(37230600)))
      .status("success")
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
    AccountOffersResult result = AccountOffersResult.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .ledgerHash(Hash256.of("B9D3D80EDF4083A06B2D51202E0BFB63C46FC0985E015D06767C21A62853BF6D"))
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.valueOf(37230600)))
      .status("success")
      .build();

    assertThat(result.ledgerIndex()).isEmpty();
    assertThat(result.ledgerCurrentIndex()).isNotEmpty().get().isEqualTo(result.ledgerCurrentIndexSafe());
    assertThrows(
      IllegalStateException.class,
      result::ledgerIndexSafe
    );
  }

}
