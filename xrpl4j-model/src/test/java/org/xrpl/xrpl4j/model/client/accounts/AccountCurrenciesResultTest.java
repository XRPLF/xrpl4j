package org.xrpl.xrpl4j.model.client.accounts;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.google.common.primitives.UnsignedInteger;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Hash256;

class AccountCurrenciesResultTest {

  @Test
  void testWithLedgerIndex() {
    AccountCurrenciesResult result = AccountCurrenciesResult.builder()
      .ledgerHash(Hash256.of("B9D3D80EDF4083A06B2D51202E0BFB63C46FC0985E015D06767C21A62853BF6D"))
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(37230600)))
      .status("success")
      .validated(true)
      .addSendCurrencies("USD")
      .addReceiveCurrencies("EUR")
      .addReceiveCurrencies("USD")
      .build();

    assertThat(result.ledgerCurrentIndex()).isEmpty();
    assertThat(result.ledgerIndexSafe()).isEqualTo(result.ledgerIndex());
  }

  @Test
  void testWithLedgerCurrentIndex() {
    AccountCurrenciesResult result = AccountCurrenciesResult.builder()
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.valueOf(66467750)))
      .status("success")
      .validated(false)
      .receiveCurrencies(Lists.newArrayList("BTC", "CNY", "015841551A748AD2C1F76FF6ECB0CCCD00000000"))
      .sendCurrencies(Lists.newArrayList("ASP", "BTC", "USD"))
      .build();

    assertThat(result.ledgerIndex()).isNull();
    assertThat(result.ledgerCurrentIndex()).isNotEmpty().get().isEqualTo(result.ledgerIndexSafe());
  }

}
