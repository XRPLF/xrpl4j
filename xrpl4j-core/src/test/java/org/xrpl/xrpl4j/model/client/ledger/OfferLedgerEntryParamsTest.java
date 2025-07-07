package org.xrpl.xrpl4j.model.client.ledger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.transactions.Address;

public class OfferLedgerEntryParamsTest {

  @Test
  public void testOfferLedgerEntryParams() {
    OfferLedgerEntryParams offerLedgerEntryParams = OfferLedgerEntryParams.builder()
      .account(Address.of("rK2vwKgQqXahHWUvi9VVTQsYe6gze5n1os"))
      .seq(UnsignedInteger.ONE)
      .build();

    assertThat(offerLedgerEntryParams.account()).isEqualTo(Address.of("rK2vwKgQqXahHWUvi9VVTQsYe6gze5n1os"));
    assertThat(offerLedgerEntryParams.seq()).isEqualTo(UnsignedInteger.ONE);
  }
}
