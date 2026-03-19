package org.xrpl.xrpl4j.model.client.accounts;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Marker;

public class AccountSponsoringRequestParamsTest {

  @Test
  public void buildWithMinimalFields() {
    AccountSponsoringRequestParams params = AccountSponsoringRequestParams.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .build();

    assertThat(params.account()).isEqualTo(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"));
    assertThat(params.ledgerSpecifier()).isEqualTo(LedgerSpecifier.VALIDATED);
    assertThat(params.deletionBlockersOnly()).isEmpty();
    assertThat(params.limit()).isEmpty();
    assertThat(params.marker()).isEmpty();
    assertThat(params.type()).isEmpty();
  }

  @Test
  public void buildWithAllFields() {
    Marker marker = Marker.of("marker");
    AccountSponsoringRequestParams params = AccountSponsoringRequestParams.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .deletionBlockersOnly(true)
      .ledgerSpecifier(LedgerSpecifier.of(LedgerIndex.of(UnsignedInteger.valueOf(1000))))
      .limit(UnsignedInteger.valueOf(50))
      .marker(marker)
      .type("offer")
      .build();

    assertThat(params.account()).isEqualTo(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"));
    assertThat(params.deletionBlockersOnly()).isPresent().get().isTrue();
    assertThat(params.ledgerSpecifier().ledgerIndex()).isPresent().get()
      .isEqualTo(LedgerIndex.of(UnsignedInteger.valueOf(1000)));
    assertThat(params.limit()).isPresent().get().isEqualTo(UnsignedInteger.valueOf(50));
    assertThat(params.marker()).isPresent().get().isEqualTo(marker);
    assertThat(params.type()).isPresent().get().isEqualTo("offer");
  }

  @Test
  public void buildWithLedgerHash() {
    Hash256 ledgerHash = Hash256.of("E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321");
    AccountSponsoringRequestParams params = AccountSponsoringRequestParams.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .ledgerSpecifier(LedgerSpecifier.of(ledgerHash))
      .build();

    assertThat(params.ledgerSpecifier().ledgerHash()).isPresent().get().isEqualTo(ledgerHash);
  }

  @Test
  public void buildWithDeletionBlockersOnly() {
    AccountSponsoringRequestParams params = AccountSponsoringRequestParams.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .deletionBlockersOnly(true)
      .build();

    assertThat(params.deletionBlockersOnly()).isPresent().get().isTrue();
  }

  @Test
  public void buildWithTypeFilter() {
    AccountSponsoringRequestParams params = AccountSponsoringRequestParams.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .type("escrow")
      .build();

    assertThat(params.type()).isPresent().get().isEqualTo("escrow");
  }

}

