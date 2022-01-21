package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class NfTokenCancelOfferTest {

  @Test
  public void buildTx() {

    String offer = "000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65";
    String []offers = {offer};
    NfTokenCancelOffer nfTokenCancelOffer = NfTokenCancelOffer.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(1))
      .tokenIds(offers)
      .build();

    assertThat(nfTokenCancelOffer.tokenIds()).isEqualTo(offers);
  }

  @Test
  public void emptyTokenIds() {

    assertThrows(
      IllegalArgumentException.class,
      () -> NfTokenCancelOffer.builder()
        .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
        .fee(XrpCurrencyAmount.ofDrops(1))
        .tokenIds(new String[0])
        .build(),
      "List of tokenIds must be non-empty."
    );
  }

}
