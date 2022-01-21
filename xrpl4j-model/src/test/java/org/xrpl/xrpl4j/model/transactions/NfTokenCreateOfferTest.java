package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class NfTokenCreateOfferTest {

  @Test
  public void buildTx() {

    String id = "000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65";
    NfTokenCreateOffer nfTokenCreateOffer = NfTokenCreateOffer.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(1))
      .tokenId(id)
      .amount(XrpCurrencyAmount.ofDrops(2000L))
      .build();

    assertThat(nfTokenCreateOffer.tokenId()).isEqualTo(id);
  }

  @Test
  public void tokenIdMissing() {

    assertThrows(
      IllegalStateException.class,
      () -> NfTokenCreateOffer.builder()
        .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
        .fee(XrpCurrencyAmount.ofDrops(1))
        .amount(XrpCurrencyAmount.ofDrops(2000L))
        .build()
    );
  }
}
