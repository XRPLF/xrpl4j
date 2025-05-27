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

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

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

  //////////////////
  // Private Helpers

  /// ///////////////

  private Payment xrpPayment() {
    List<CredentialID> credentialIDs = Collections.singletonList(
            CredentialID.of("EA85602C1B41F6F1F5E83C0E6B87142FB8957BD209469E4CC347BA2D0C26F662")
    );
    return Payment.builder()
            .sequence(UnsignedInteger.ONE)
            .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
            .destination(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
            .fee(XrpCurrencyAmount.ofDrops(1000L))
            .amount(XrpCurrencyAmount.ofDrops(2000L))
            .credentialIDs(credentialIDs)
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
