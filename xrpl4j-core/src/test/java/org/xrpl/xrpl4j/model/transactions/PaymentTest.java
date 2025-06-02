package org.xrpl.xrpl4j.model.transactions;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Unit tests for {@link Payment}.
 */
public class PaymentTest {

  @Test
  public void paymentBuilder() {
    assertThat(xrpPayment()).isNotNull();
    assertThat(issuedCurrencyPayment()).isNotNull();
  }

  @Test
  void buildPaymentWithTicketSequence() {
    Payment payment = Payment.builder()
      .ticketSequence(UnsignedInteger.ONE)
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(1000L))
      .amount(XrpCurrencyAmount.ofDrops(2000L))
      .build();

    assertThat(payment.ticketSequence()).isNotEmpty().get().isEqualTo(UnsignedInteger.ONE);
    assertThat(payment.sequence()).isEqualTo(UnsignedInteger.ZERO);
  }

  @Test
  public void flagsForXrpPayment() {
    assertThat(xrpPayment().flags().isEmpty()).isTrue();
  }

  @Test
  public void flagsForIssuedCurrency() {
    assertThat(issuedCurrencyPayment().flags().isEmpty()).isTrue();
  }

  @Test
  public void testMoreThanEightCredentialIds() {
    List<Hash256> moreThanEight = IntStream.range(0, 9)
      .mapToObj(i ->
        Hash256.of("7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF" + i))
      .collect(Collectors.toList());

    assertThrows(
      IllegalArgumentException.class,
      () -> Payment.builder()
        .sequence(UnsignedInteger.ONE)
        .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
        .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .fee(XrpCurrencyAmount.ofDrops(1000L))
        .amount(XrpCurrencyAmount.ofDrops(2000L))
        .credentialIds(moreThanEight)
        .build(),
      "credentialIds shouldn't be empty and must have less than or equal to 8 items."
    );

  }

  @Test
  public void testEmptyCredentialIds() {
    assertThrows(
      IllegalArgumentException.class,
      () -> Payment.builder()
        .sequence(UnsignedInteger.ONE)
        .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
        .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .fee(XrpCurrencyAmount.ofDrops(1000L))
        .amount(XrpCurrencyAmount.ofDrops(2000L))
        .credentialIds(new ArrayList<>())
        .build(),
      "credentialIds shouldn't be empty and must have less than or equal to 8 items."
    );
  }

  @Test
  public void testDuplicateCredentialIds() {
    List<Hash256> randomIds = IntStream.range(0, 8)
      .mapToObj(i ->
        Hash256.of("7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF" + i))
      .collect(Collectors.toList());

    randomIds.set(1, randomIds.get(0));

    assertThrows(
      IllegalArgumentException.class,
      () -> Payment.builder()
        .sequence(UnsignedInteger.ONE)
        .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
        .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .fee(XrpCurrencyAmount.ofDrops(1000L))
        .amount(XrpCurrencyAmount.ofDrops(2000L))
        .credentialIds(randomIds)
        .build(),
      "credentialIds should have unique values."
    );
  }

  //////////////////
  // Private Helpers

  /// ///////////////

  private Payment xrpPayment() {
    List<Hash256> credentialIds = Collections.singletonList(
      Hash256.of("EA85602C1B41F6F1F5E83C0E6B87142FB8957BD209469E4CC347BA2D0C26F662")
    );
    return Payment.builder()
      .sequence(UnsignedInteger.ONE)
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(1000L))
      .amount(XrpCurrencyAmount.ofDrops(2000L))
      .credentialIds(credentialIds)
      .build();
  }

  private Payment issuedCurrencyPayment() {
    return Payment.builder()
      .sequence(UnsignedInteger.ONE)
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(1000L))
      .amount(IssuedCurrencyAmount.builder().currency("USD").issuer(
        Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba")).value("500").build()
      ).build();
  }
}
