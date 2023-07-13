package org.xrpl.xrpl4j.model.client.path;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.flags.OfferFlags;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.math.BigDecimal;
import java.util.Optional;

class BookOffersOfferTest {

  @Test
  void testOwnerFundsBigDecimal() {
    BookOffersOffer offer = constructOffer(Optional.of("3389276844"), Optional.empty());
    assertThat(offer.ownerFunds()).isNotEmpty().get().isEqualTo(BigDecimal.valueOf(3389276844L));

    offer = constructOffer(Optional.of("0"), Optional.empty());
    assertThat(offer.ownerFunds()).isNotEmpty().get().isEqualTo(BigDecimal.valueOf(0));

    offer = constructOffer(Optional.of("0.0000000000000001"), Optional.empty());
    assertThat(offer.ownerFunds()).isNotEmpty();
    assertThat(offer.ownerFunds().get().toPlainString()).isEqualTo("0.0000000000000001");

    offer = constructOffer(Optional.empty(), Optional.of("0.00000046645691"));
    assertThat(offer.ownerFunds()).isEmpty();
  }

  @Test
  void testQualityString() {
    BookOffersOffer offer = constructOffer(Optional.empty(), Optional.of("0.0000000046645691"));
    assertThat(offer.quality().toPlainString()).isEqualTo("0.0000000046645691");

    offer = constructOffer(Optional.empty(), Optional.of("0"));
    assertThat(offer.quality()).isEqualTo(BigDecimal.ZERO);

    offer = constructOffer(Optional.empty(), Optional.of("3389276844"));
    assertThat(offer.quality()).isEqualTo(BigDecimal.valueOf(3389276844L));
  }

  private static BookOffersOffer constructOffer(Optional<String> ownerFunds, Optional<String> quality) {
    return BookOffersOffer.builder()
      .account(Address.of("rPbMHxs7vy5t6e19tYfqG7XJ6Fog8EPZLk"))
      .bookDirectory(Hash256.of("DFA3B6DDAB58C7E8E5D944E736DA4B7046C30E4F460FD9DE4E109266A03C5B00"))
      .bookNode("0")
      .flags(OfferFlags.of(0))
      .ownerNode("0")
      .previousTransactionId(Hash256.of("3E8D2F2CC7593C40FF8EC2A5316B7E308473DC90B50CC54AD4D487E4E6515545"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(80959227))
      .sequence(UnsignedInteger.valueOf(1400403))
      .takerGets(XrpCurrencyAmount.ofDrops(2500000000L))
      .takerPays(
        IssuedCurrencyAmount.builder()
          .currency("USD")
          .issuer(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B"))
          .value("1166.142275")
          .build()
      )
      .index(Hash256.of("D17EA19846F265BD1244F2864B85ECDB7C86D462C00C8B658A035358BDDD6DE2"))
      .ownerFundsString(ownerFunds)
      .qualityString(quality.orElse("0.00000046645691"))
      .build();
  }
}