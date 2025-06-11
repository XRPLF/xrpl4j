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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.flags.PaymentChannelClaimFlags;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Unit tests for {@link PaymentChannelClaim}.
 */
public class PaymentChannelClaimTest {

  @Test
  public void claimWithAllFields() {
    List<Hash256> credentialIds = IntStream.range(0, 8)
      .mapToObj(i -> Hash256.of("7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF" + i))
      .collect(Collectors.toList());

    String signature = "30440220718D264EF05CAED7C781FF6DE298DCAC68D002562C9BF3A07C1E721B420C0DAB02203A5A4779E\" +\n" +
      "        \"F4D2CCC7BC3EF886676D803A9981B928D3B8ACA483B80ECA3CD7B9B";

    PaymentChannelClaimFlags flags = PaymentChannelClaimFlags.builder().tfRenew(true).build();

    PaymentChannelClaim paymentChannelClaim = PaymentChannelClaim.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(100))
      .sequence(UnsignedInteger.ONE)
      .amount(XrpCurrencyAmount.ofDrops(10000))
      .channel(Hash256.of("7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF0"))
      .balance(XrpCurrencyAmount.ofDrops(10000))
      .signature(signature)
      .publicKey("32D2471DB72B27E3310F355BB33E339BF26F8392D5A93D3BC0FC3B566612DA0F0A")
      .credentialIds(credentialIds)
      .flags(flags)
      .build();

    assertThat(paymentChannelClaim.account()).isEqualTo(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"));
    assertThat(paymentChannelClaim.transactionType()).isEqualTo(TransactionType.PAYMENT_CHANNEL_CLAIM);
    assertThat(paymentChannelClaim.amount()).isPresent().get().isEqualTo(XrpCurrencyAmount.ofDrops(10000));
    assertThat(paymentChannelClaim.balance()).isPresent().get().isEqualTo(XrpCurrencyAmount.ofDrops(10000));
    assertThat(paymentChannelClaim.signature()).isPresent().get().isEqualTo(signature);
    assertThat(paymentChannelClaim.credentialIds()).isEqualTo(credentialIds);
    assertThat(paymentChannelClaim.flags().isEmpty()).isFalse();
    assertThat(paymentChannelClaim.flags()).isEqualTo(flags);
  }

  @Test
  public void testMoreThanEightCredentialIds() {
    List<Hash256> moreThanEight = IntStream.range(0, 9)
      .mapToObj(i -> Hash256.of("7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF" + i))
      .collect(Collectors.toList());

    assertThatThrownBy(() -> PaymentChannelClaim.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(100))
      .sequence(UnsignedInteger.ONE)
      .amount(XrpCurrencyAmount.ofDrops(10000))
      .publicKey("32D2471DB72B27E3310F355BB33E339BF26F8392D5A93D3BC0FC3B566612DA0F0A")
      .credentialIds(moreThanEight)
      .channel(Hash256.of("7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF1"))
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("CredentialIDs should have less than or equal to 8 items.");
  }

  @Test
  public void testDuplicateCredentialIds() {
    List<Hash256> randomIds = IntStream.range(0, 8)
      .mapToObj(i -> Hash256.of("7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF" + i))
      .collect(Collectors.toList());

    randomIds.set(1, randomIds.get(0));

    assertThatThrownBy(() -> PaymentChannelClaim.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(100))
      .sequence(UnsignedInteger.ONE)
      .amount(XrpCurrencyAmount.ofDrops(10000))
      .publicKey("32D2471DB72B27E3310F355BB33E339BF26F8392D5A93D3BC0FC3B566612DA0F0A")
      .credentialIds(randomIds)
      .channel(Hash256.of("7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF1"))
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("CredentialIDs should have unique values.");
  }

}
