package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class NfTokenCancelOfferTest {

  @Test
  public void buildTx() {

    Hash256 offer = Hash256.of("000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65");
    List<Hash256> offers = new ArrayList<>();
    offers.add(offer);
    NfTokenCancelOffer nfTokenCancelOffer = NfTokenCancelOffer.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(1))
      .tokenOffers(offers)
      .build();

    Assertions.assertThat(offer.equals(nfTokenCancelOffer.tokenOffers().get(0)));
    assertThat(nfTokenCancelOffer.tokenOffers()).isEqualTo(offers);
  }

  @Test
  public void emptyTokenIds() {

    List<Hash256> offers = new ArrayList<>();
    assertThrows(
      IllegalArgumentException.class,
      () -> NfTokenCancelOffer.builder()
        .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
        .fee(XrpCurrencyAmount.ofDrops(1))
        .tokenOffers(offers)
        .build(),
      "List of tokenIds must be non-empty."
    );
  }

}
