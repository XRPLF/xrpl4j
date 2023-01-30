package org.xrpl.xrpl4j.model.client.accounts;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: model
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerIndexBound;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.util.Optional;

public class AccountTransactionsRequestParamsTests {

  @Test
  void constructDefaultParams() {
    AccountTransactionsRequestParams params = AccountTransactionsRequestParams.unboundedBuilder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .build();

    assertThat(params.ledgerIndexMinimum()).isEqualTo(LedgerIndexBound.of(-1));
    assertThat(params.ledgerIndexMaximum()).isEqualTo(LedgerIndexBound.of(-1));
    assertThat(params.ledgerSpecifier()).isEmpty();
  }

  @Test
  void constructWithLedgerIndexMax() {
    AccountTransactionsRequestParams params = AccountTransactionsRequestParams.unboundedBuilder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .ledgerIndexMaximum(LedgerIndexBound.of(12345))
      .build();

    assertThat(params.ledgerIndexMinimum()).isEqualTo(LedgerIndexBound.of(-1));
    assertThat(params.ledgerIndexMaximum()).isEqualTo(LedgerIndexBound.of(12345));
    assertThat(params.ledgerSpecifier()).isEmpty();
  }

  @Test
  void constructWithledgerIndexMinimum() {
    AccountTransactionsRequestParams params = AccountTransactionsRequestParams.unboundedBuilder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .ledgerIndexMinimum(LedgerIndexBound.of(12345))
      .build();

    assertThat(params.ledgerIndexMinimum()).isEqualTo(LedgerIndexBound.of(12345));
    assertThat(params.ledgerIndexMaximum()).isEqualTo(LedgerIndexBound.of(-1));
    assertThat(params.ledgerSpecifier()).isEmpty();
  }

  @Test
  void constructWithLedgerSpecifier() {
    AccountTransactionsRequestParams params = AccountTransactionsRequestParams.unboundedBuilder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .ledgerSpecifier(Optional.of(LedgerSpecifier.of(LedgerIndex.of(UnsignedInteger.ONE))))
      .build();

    assertThat(params.ledgerIndexMinimum()).isNull();
    assertThat(params.ledgerIndexMaximum()).isNull();
    assertThat(params.ledgerSpecifier()).isNotEmpty();
  }

  @Test
  void constructWithInvalidLedgerSpecifier() {
    assertThrows(
      IllegalArgumentException.class,
      () -> AccountTransactionsRequestParams.unboundedBuilder()
        .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .ledgerSpecifier(Optional.of(LedgerSpecifier.CURRENT))
        .build()
    );
  }

  @Test
  void constructWithLedgerSpecifierAndBounds() {
    AccountTransactionsRequestParams params = AccountTransactionsRequestParams.unboundedBuilder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .ledgerIndexMinimum(LedgerIndexBound.of(12345))
      .ledgerSpecifier(Optional.of(LedgerSpecifier.VALIDATED))
      .build();

    assertThat(params.ledgerIndexMinimum()).isNull();
    assertThat(params.ledgerIndexMaximum()).isNull();
    assertThat(params.ledgerSpecifier()).isNotEmpty();

    params = AccountTransactionsRequestParams.unboundedBuilder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .ledgerIndexMaximum(LedgerIndexBound.of(12345))
      .ledgerSpecifier(Optional.of(LedgerSpecifier.VALIDATED))
      .build();

    assertThat(params.ledgerIndexMinimum()).isNull();
    assertThat(params.ledgerIndexMaximum()).isNull();
    assertThat(params.ledgerSpecifier()).isNotEmpty();

    params = AccountTransactionsRequestParams.unboundedBuilder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .ledgerIndexMaximum(LedgerIndexBound.of(12345))
      .ledgerIndexMinimum(LedgerIndexBound.of(12345))
      .ledgerSpecifier(Optional.of(LedgerSpecifier.VALIDATED))
      .build();

    assertThat(params.ledgerIndexMinimum()).isNull();
    assertThat(params.ledgerIndexMaximum()).isNull();
    assertThat(params.ledgerSpecifier()).isNotEmpty();
  }

  @Test
  void builderWithLedgerSpecifier() {
    AccountTransactionsRequestParams params = AccountTransactionsRequestParams
        .builder(LedgerSpecifier.VALIDATED)
        .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .build();

    assertThat(params.ledgerSpecifier()).isEqualTo(Optional.of(LedgerSpecifier.VALIDATED));
  }

  @Test
  void builderWithLedgerIndex() {
    AccountTransactionsRequestParams params = AccountTransactionsRequestParams
        .builder(LedgerSpecifier.of(UnsignedInteger.ONE))
        .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .build();

    assertThat(params.ledgerSpecifier()).isEqualTo(Optional.of(LedgerSpecifier.of(UnsignedInteger.ONE)));
  }

  @Test
  void builderWithLedgerHash() {
    String random = "qmHJUF3lNC6rfEkZa3URngmwIRYU7bKMYKPz4er7UJKnWUItKuBCN9qKqXt8YYJ8";

    AccountTransactionsRequestParams params = AccountTransactionsRequestParams
        .builder(LedgerSpecifier.of(Hash256.of(random)))
        .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .build();

    assertThat(params.ledgerSpecifier()).isEqualTo(Optional.of(LedgerSpecifier.of(Hash256.of(random))));
  }

  @Test
  void builderWithLedgerIndexRange() {
    AccountTransactionsRequestParams params = AccountTransactionsRequestParams
        .builder(LedgerIndexBound.of(12345), LedgerIndexBound.of(12347))
        .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .build();

    assertThat(params.ledgerIndexMinimum()).isEqualTo(LedgerIndexBound.of(12345));
    assertThat(params.ledgerIndexMaximum()).isEqualTo(LedgerIndexBound.of(12347));
  }

  @Test
  void builderWithLedgerIndexMin() {
    AccountTransactionsRequestParams params = AccountTransactionsRequestParams
        .builder(LedgerIndexBound.of(12345), LedgerIndexBound.of(-1))
        .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .build();

    assertThat(params.ledgerIndexMinimum()).isEqualTo(LedgerIndexBound.of(12345));
    assertThat(params.ledgerIndexMaximum()).isEqualTo(LedgerIndexBound.of(-1));
  }

  @Test
  void builderWithLedgerIndexMax() {
    AccountTransactionsRequestParams params = AccountTransactionsRequestParams
        .builder(LedgerIndexBound.of(-1), LedgerIndexBound.of(12345))
        .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .build();

    assertThat(params.ledgerIndexMinimum()).isEqualTo(LedgerIndexBound.of(-1));
    assertThat(params.ledgerIndexMaximum()).isEqualTo(LedgerIndexBound.of(12345));
  }
}
