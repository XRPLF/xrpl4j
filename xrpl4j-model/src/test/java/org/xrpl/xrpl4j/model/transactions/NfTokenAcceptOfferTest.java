package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import java.util.Optional;

public class NfTokenAcceptOfferTest {

  private final Address address = Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn");
  private final Hash256 offer = Hash256.of("000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65");

  @Test
  public void buildTx() {
    NfTokenAcceptOffer nfTokenAcceptOffer = NfTokenAcceptOffer.builder()
      .account(address)
      .fee(XrpCurrencyAmount.ofDrops(1))
      .buyOffer(offer)
      .build();

    assertThat(offer.equals(nfTokenAcceptOffer.buyOffer()));
    assertThat(nfTokenAcceptOffer.buyOffer()).isEqualTo(Optional.of(offer));
    assertThat(nfTokenAcceptOffer.account().equals(address));
  }

  @Test
  public void directModeWithBrokerFee_throws() {
    assertThatThrownBy(() -> NfTokenAcceptOffer.builder()
      .account(address)
      .sellOffer(offer)
      .brokerFee(XrpCurrencyAmount.ofDrops(1000))
      .fee(XrpCurrencyAmount.ofDrops(100))
      .build())
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("No BrokerFee needed in direct mode.");
  }

  @Test
  public void brokeredModeWithAndWithoutFee() {
    NfTokenAcceptOffer nfTokenAcceptOffer = NfTokenAcceptOffer.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(1))
      .buyOffer(offer)
      .sellOffer(offer)
      .build();

    assertThat(offer.equals(nfTokenAcceptOffer.buyOffer()));
    assertThat(nfTokenAcceptOffer.buyOffer()).isEqualTo(Optional.of(offer));

    nfTokenAcceptOffer = NfTokenAcceptOffer.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(1))
      .buyOffer(offer)
      .sellOffer(offer)
      .brokerFee(XrpCurrencyAmount.ofDrops(10000))
      .build();
    assertThat(offer.equals(nfTokenAcceptOffer.buyOffer()));
    assertThat(nfTokenAcceptOffer.buyOffer()).isEqualTo(Optional.of(offer));
  }

  @Test
  public void offerValueTooShort() {

    assertThrows(
      IllegalArgumentException.class,
      () -> NfTokenAcceptOffer.builder()
        .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
        .fee(XrpCurrencyAmount.ofDrops(1))
        .buyOffer(Hash256.of("offer"))
        .build(),
      "Hash256 Strings must be 64 characters long."
    );
  }

}
