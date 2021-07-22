package org.xrpl.xrpl4j.model.client.accounts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.client.specifiers.LedgerIndex;
import org.xrpl.xrpl4j.model.client.specifiers.LedgerIndexBound;
import org.xrpl.xrpl4j.model.client.specifiers.LedgerIndexShortcut;
import org.xrpl.xrpl4j.model.client.specifiers.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.Address;

public class AccountTransactionsRequestParamsTests {

  @Test
  void constructDefaultParams() {
    AccountTransactionsRequestParams params = AccountTransactionsRequestParams.builder()
      .account(Address.of("foo"))
      .build();

    assertThat(params.ledgerIndexMin()).isEqualTo(LedgerIndexBound.of(-1));
    assertThat(params.ledgerIndexMax()).isEqualTo(LedgerIndexBound.of(-1));
    assertThat(params.ledgerSpecifier()).isEmpty();
  }

  @Test
  void constructWithLedgerIndexMax() {
    AccountTransactionsRequestParams params = AccountTransactionsRequestParams.builder()
      .account(Address.of("foo"))
      .ledgerIndexMax(LedgerIndexBound.of(12345))
      .build();

    assertThat(params.ledgerIndexMin()).isEqualTo(LedgerIndexBound.of(-1));
    assertThat(params.ledgerIndexMax()).isEqualTo(LedgerIndexBound.of(12345));
    assertThat(params.ledgerSpecifier()).isEmpty();
  }

  @Test
  void constructWithLedgerIndexMin() {
    AccountTransactionsRequestParams params = AccountTransactionsRequestParams.builder()
      .account(Address.of("foo"))
      .ledgerIndexMin(LedgerIndexBound.of(12345))
      .build();

    assertThat(params.ledgerIndexMin()).isEqualTo(LedgerIndexBound.of(12345));
    assertThat(params.ledgerIndexMax()).isEqualTo(LedgerIndexBound.of(-1));
    assertThat(params.ledgerSpecifier()).isEmpty();
  }

  @Test
  void constructWithLedgerSpecifier() {
    AccountTransactionsRequestParams params = AccountTransactionsRequestParams.builder()
      .account(Address.of("foo"))
      .ledgerSpecifier(LedgerSpecifier.ledgerIndex(LedgerIndex.of(UnsignedLong.ONE)))
      .build();

    assertThat(params.ledgerIndexMin()).isNull();
    assertThat(params.ledgerIndexMax()).isNull();
    assertThat(params.ledgerSpecifier()).isNotEmpty();
  }

  @Test
  void constructWithInvalidLedgerSpecifier() {
    assertThrows(
      IllegalArgumentException.class,
      () -> AccountTransactionsRequestParams.builder()
        .account(Address.of("foo"))
        .ledgerSpecifier(LedgerSpecifier.ledgerIndexShortcut(LedgerIndexShortcut.CURRENT))
        .build()
    );
  }

  @Test
  void constructWithLedgerSpecifierAndBounds() {
    AccountTransactionsRequestParams params = AccountTransactionsRequestParams.builder()
      .account(Address.of("foo"))
      .ledgerIndexMin(LedgerIndexBound.of(12345))
      .ledgerSpecifier(LedgerSpecifier.ledgerIndexShortcut(LedgerIndexShortcut.VALIDATED))
      .build();

    assertThat(params.ledgerIndexMin()).isNull();
    assertThat(params.ledgerIndexMax()).isNull();
    assertThat(params.ledgerSpecifier()).isNotEmpty();

    params = AccountTransactionsRequestParams.builder()
      .account(Address.of("foo"))
      .ledgerIndexMax(LedgerIndexBound.of(12345))
      .ledgerSpecifier(LedgerSpecifier.ledgerIndexShortcut(LedgerIndexShortcut.VALIDATED))
      .build();

    assertThat(params.ledgerIndexMin()).isNull();
    assertThat(params.ledgerIndexMax()).isNull();
    assertThat(params.ledgerSpecifier()).isNotEmpty();

    params = AccountTransactionsRequestParams.builder()
      .account(Address.of("foo"))
      .ledgerIndexMax(LedgerIndexBound.of(12345))
      .ledgerIndexMin(LedgerIndexBound.of(12345))
      .ledgerSpecifier(LedgerSpecifier.ledgerIndexShortcut(LedgerIndexShortcut.VALIDATED))
      .build();

    assertThat(params.ledgerIndexMin()).isNull();
    assertThat(params.ledgerIndexMax()).isNull();
    assertThat(params.ledgerSpecifier()).isNotEmpty();
  }
}
