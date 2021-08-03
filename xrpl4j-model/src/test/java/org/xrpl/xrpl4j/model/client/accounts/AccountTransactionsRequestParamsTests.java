package org.xrpl.xrpl4j.model.client.accounts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerIndexBound;
import org.xrpl.xrpl4j.model.client.common.LedgerIndexShortcut;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.util.Optional;

public class AccountTransactionsRequestParamsTests {

  @Test
  void constructDefaultParams() {
    AccountTransactionsRequestParams params = AccountTransactionsRequestParams.builder()
      .account(Address.of("foo"))
      .build();

    assertThat(params.ledgerIndexMinimum()).isEqualTo(LedgerIndexBound.of(-1));
    assertThat(params.ledgerIndexMaximum()).isEqualTo(LedgerIndexBound.of(-1));
    assertThat(params.ledgerSpecifier()).isEmpty();
  }

  @Test
  void constructWithLedgerIndexMax() {
    AccountTransactionsRequestParams params = AccountTransactionsRequestParams.builder()
      .account(Address.of("foo"))
      .ledgerIndexMaximum(LedgerIndexBound.of(12345))
      .build();

    assertThat(params.ledgerIndexMinimum()).isEqualTo(LedgerIndexBound.of(-1));
    assertThat(params.ledgerIndexMaximum()).isEqualTo(LedgerIndexBound.of(12345));
    assertThat(params.ledgerSpecifier()).isEmpty();
  }

  @Test
  void constructWithledgerIndexMinimum() {
    AccountTransactionsRequestParams params = AccountTransactionsRequestParams.builder()
      .account(Address.of("foo"))
      .ledgerIndexMinimum(LedgerIndexBound.of(12345))
      .build();

    assertThat(params.ledgerIndexMinimum()).isEqualTo(LedgerIndexBound.of(12345));
    assertThat(params.ledgerIndexMaximum()).isEqualTo(LedgerIndexBound.of(-1));
    assertThat(params.ledgerSpecifier()).isEmpty();
  }

  @Test
  void constructWithLedgerSpecifier() {
    AccountTransactionsRequestParams params = AccountTransactionsRequestParams.builder()
      .account(Address.of("foo"))
      .ledgerSpecifier(Optional.of(LedgerSpecifier.ledgerIndex(LedgerIndex.of(UnsignedInteger.ONE))))
      .build();

    assertThat(params.ledgerIndexMinimum()).isNull();
    assertThat(params.ledgerIndexMaximum()).isNull();
    assertThat(params.ledgerSpecifier()).isNotEmpty();
  }

  @Test
  void constructWithInvalidLedgerSpecifier() {
    assertThrows(
      IllegalArgumentException.class,
      () -> AccountTransactionsRequestParams.builder()
        .account(Address.of("foo"))
        .ledgerSpecifier(Optional.of(LedgerSpecifier.ledgerIndexShortcut(LedgerIndexShortcut.CURRENT)))
        .build()
    );
  }

  @Test
  void constructWithLedgerSpecifierAndBounds() {
    AccountTransactionsRequestParams params = AccountTransactionsRequestParams.builder()
      .account(Address.of("foo"))
      .ledgerIndexMinimum(LedgerIndexBound.of(12345))
      .ledgerSpecifier(Optional.of(LedgerSpecifier.ledgerIndexShortcut(LedgerIndexShortcut.VALIDATED)))
      .build();

    assertThat(params.ledgerIndexMinimum()).isNull();
    assertThat(params.ledgerIndexMaximum()).isNull();
    assertThat(params.ledgerSpecifier()).isNotEmpty();

    params = AccountTransactionsRequestParams.builder()
      .account(Address.of("foo"))
      .ledgerIndexMaximum(LedgerIndexBound.of(12345))
      .ledgerSpecifier(Optional.of(LedgerSpecifier.ledgerIndexShortcut(LedgerIndexShortcut.VALIDATED)))
      .build();

    assertThat(params.ledgerIndexMinimum()).isNull();
    assertThat(params.ledgerIndexMaximum()).isNull();
    assertThat(params.ledgerSpecifier()).isNotEmpty();

    params = AccountTransactionsRequestParams.builder()
      .account(Address.of("foo"))
      .ledgerIndexMaximum(LedgerIndexBound.of(12345))
      .ledgerIndexMinimum(LedgerIndexBound.of(12345))
      .ledgerSpecifier(Optional.of(LedgerSpecifier.ledgerIndexShortcut(LedgerIndexShortcut.VALIDATED)))
      .build();

    assertThat(params.ledgerIndexMinimum()).isNull();
    assertThat(params.ledgerIndexMaximum()).isNull();
    assertThat(params.ledgerSpecifier()).isNotEmpty();
  }
}
