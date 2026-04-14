package org.xrpl.xrpl4j.model.client.accounts;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.flags.OfferFlags;
import org.xrpl.xrpl4j.model.ledger.OfferObject;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Marker;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.Collections;

public class AccountSponsoringResultTest {

  @Test
  public void buildWithMinimalFields() {
    AccountSponsoringResult result = AccountSponsoringResult.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .sponsoredObjects(Collections.emptyList())
      .build();

    assertThat(result.account()).isEqualTo(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"));
    assertThat(result.sponsoredObjects()).isEmpty();
    assertThat(result.validated()).isFalse();
    assertThat(result.ledgerHash()).isEmpty();
    assertThat(result.ledgerIndex()).isEmpty();
    assertThat(result.ledgerCurrentIndex()).isEmpty();
    assertThat(result.limit()).isEmpty();
    assertThat(result.marker()).isEmpty();
  }

  @Test
  public void buildWithAllFields() {
    Hash256 ledgerHash = Hash256.of("E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321");
    Marker marker = Marker.of("marker");

    OfferObject offer = OfferObject.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .flags(OfferFlags.of(0))
      .sequence(UnsignedInteger.ONE)
      .takerGets(XrpCurrencyAmount.ofDrops(1000))
      .takerPays(XrpCurrencyAmount.ofDrops(2000))
      .bookDirectory(Hash256.of("4627DFFCFF8B5A265EDBD8AE8C14A52325DBFEDAF4F5C32E5E03E788E09BB35C"))
      .bookNode("0000000000000000")
      .ownerNode("0000000000000000")
      .previousTransactionId(Hash256.of("F0AB71E777B2DA54B86231E19B82554EF1F8211F92ECA473121C655BFC5329BF"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(14524914))
      .index(Hash256.of("96F76F27D8A327FC48753167EC04A46AA0E382E6F57F32FD12274144D00F1797"))
      .sponsor(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
      .build();

    AccountSponsoringResult result = AccountSponsoringResult.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .sponsoredObjects(Collections.singletonList(offer))
      .ledgerHash(ledgerHash)
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.valueOf(1000)))
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.valueOf(1001)))
      .limit(UnsignedInteger.valueOf(50))
      .marker(marker)
      .validated(true)
      .build();

    assertThat(result.account()).isEqualTo(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"));
    assertThat(result.sponsoredObjects()).hasSize(1);
    assertThat(result.sponsoredObjects().get(0)).isInstanceOf(OfferObject.class);
    assertThat(((OfferObject) result.sponsoredObjects().get(0)).sponsor()).isPresent()
      .get().isEqualTo(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"));
    assertThat(result.ledgerHash()).isPresent().get().isEqualTo(ledgerHash);
    assertThat(result.ledgerIndex()).isPresent().get()
      .isEqualTo(LedgerIndex.of(UnsignedInteger.valueOf(1000)));
    assertThat(result.ledgerCurrentIndex()).isPresent().get()
      .isEqualTo(LedgerIndex.of(UnsignedInteger.valueOf(1001)));
    assertThat(result.limit()).isPresent().get().isEqualTo(UnsignedInteger.valueOf(50));
    assertThat(result.marker()).isPresent().get().isEqualTo(marker);
    assertThat(result.validated()).isTrue();
  }

  @Test
  public void buildWithValidatedLedger() {
    AccountSponsoringResult result = AccountSponsoringResult.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .sponsoredObjects(Collections.emptyList())
      .validated(true)
      .build();

    assertThat(result.validated()).isTrue();
  }

  @Test
  public void ledgerHashSafeThrowsWhenEmpty() {
    AccountSponsoringResult result = AccountSponsoringResult.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .sponsoredObjects(Collections.emptyList())
      .build();

    org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, result::ledgerHashSafe);
  }

  @Test
  public void ledgerHashSafeReturnsWhenPresent() {
    Hash256 ledgerHash = Hash256.of("E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321");
    AccountSponsoringResult result = AccountSponsoringResult.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .sponsoredObjects(Collections.emptyList())
      .ledgerHash(ledgerHash)
      .build();

    assertThat(result.ledgerHashSafe()).isEqualTo(ledgerHash);
  }

  @Test
  public void ledgerIndexSafeThrowsWhenEmpty() {
    AccountSponsoringResult result = AccountSponsoringResult.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .sponsoredObjects(Collections.emptyList())
      .build();

    org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, result::ledgerIndexSafe);
  }

  @Test
  public void ledgerIndexSafeReturnsWhenPresent() {
    LedgerIndex ledgerIndex = LedgerIndex.of(UnsignedInteger.valueOf(1000));
    AccountSponsoringResult result = AccountSponsoringResult.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .sponsoredObjects(Collections.emptyList())
      .ledgerIndex(ledgerIndex)
      .build();

    assertThat(result.ledgerIndexSafe()).isEqualTo(ledgerIndex);
  }

  @Test
  public void ledgerCurrentIndexSafeThrowsWhenEmpty() {
    AccountSponsoringResult result = AccountSponsoringResult.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .sponsoredObjects(Collections.emptyList())
      .build();

    org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, result::ledgerCurrentIndexSafe);
  }

  @Test
  public void ledgerCurrentIndexSafeReturnsWhenPresent() {
    LedgerIndex ledgerCurrentIndex = LedgerIndex.of(UnsignedInteger.valueOf(1001));
    AccountSponsoringResult result = AccountSponsoringResult.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .sponsoredObjects(Collections.emptyList())
      .ledgerCurrentIndex(ledgerCurrentIndex)
      .build();

    assertThat(result.ledgerCurrentIndexSafe()).isEqualTo(ledgerCurrentIndex);
  }

}

