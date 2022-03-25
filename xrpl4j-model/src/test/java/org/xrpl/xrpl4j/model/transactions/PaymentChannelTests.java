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
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;

public class PaymentChannelTests {

  @Test
  public void createWithoutCancelAfterOrDestinationTag() {
    PaymentChannelCreate create = PaymentChannelCreate.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(100))
      .sequence(UnsignedInteger.ONE)
      .amount(XrpCurrencyAmount.ofDrops(10000))
      .destination(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
      .settleDelay(UnsignedInteger.ONE)
      .publicKey("32D2471DB72B27E3310F355BB33E339BF26F8392D5A93D3BC0FC3B566612DA0F0A")
      .build();

    assertThat(create.account()).isEqualTo(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"));
    assertThat(create.transactionType()).isEqualTo(TransactionType.PAYMENT_CHANNEL_CREATE);
    assertThat(create.amount()).isEqualTo(XrpCurrencyAmount.ofDrops(10000));
    assertThat(create.destination()).isEqualTo(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"));
    assertThat(create.settleDelay()).isEqualTo(UnsignedInteger.ONE);
    assertThat(create.publicKey()).isEqualTo("32D2471DB72B27E3310F355BB33E339BF26F8392D5A93D3BC0FC3B566612DA0F0A");
    assertThat(create.flags().tfFullyCanonicalSig()).isTrue();
  }

  @Test
  public void createWithSourceAndDestinationTags() {
    PaymentChannelCreate create = PaymentChannelCreate.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .sourceTag(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(100))
      .sequence(UnsignedInteger.ONE)
      .amount(XrpCurrencyAmount.ofDrops(10000))
      .destination(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
      .destinationTag(UnsignedInteger.valueOf(2))
      .settleDelay(UnsignedInteger.ONE)
      .publicKey("32D2471DB72B27E3310F355BB33E339BF26F8392D5A93D3BC0FC3B566612DA0F0A")
      .build();

    assertThat(create.account()).isEqualTo(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"));
    assertThat(create.sourceTag()).isNotEmpty().get().isEqualTo(UnsignedInteger.ONE);
    assertThat(create.transactionType()).isEqualTo(TransactionType.PAYMENT_CHANNEL_CREATE);
    assertThat(create.amount()).isEqualTo(XrpCurrencyAmount.ofDrops(10000));
    assertThat(create.destination()).isEqualTo(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"));
    assertThat(create.destinationTag()).isNotEmpty().get().isEqualTo(UnsignedInteger.valueOf(2));
    assertThat(create.settleDelay()).isEqualTo(UnsignedInteger.ONE);
    assertThat(create.publicKey()).isEqualTo("32D2471DB72B27E3310F355BB33E339BF26F8392D5A93D3BC0FC3B566612DA0F0A");
    assertThat(create.flags().tfFullyCanonicalSig()).isTrue();
  }

  @Test
  public void createWithCancelAfterButWithoutDestinationTag() {
    PaymentChannelCreate create = PaymentChannelCreate.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(100))
      .sequence(UnsignedInteger.ONE)
      .amount(XrpCurrencyAmount.ofDrops(10000))
      .destination(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
      .settleDelay(UnsignedInteger.ONE)
      .publicKey("32D2471DB72B27E3310F355BB33E339BF26F8392D5A93D3BC0FC3B566612DA0F0A")
      .cancelAfter(UnsignedLong.valueOf(533171558))
      .build();

    assertThat(create.account()).isEqualTo(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"));
    assertThat(create.transactionType()).isEqualTo(TransactionType.PAYMENT_CHANNEL_CREATE);
    assertThat(create.amount()).isEqualTo(XrpCurrencyAmount.ofDrops(10000));
    assertThat(create.destination()).isEqualTo(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"));
    assertThat(create.settleDelay()).isEqualTo(UnsignedInteger.ONE);
    assertThat(create.publicKey()).isEqualTo("32D2471DB72B27E3310F355BB33E339BF26F8392D5A93D3BC0FC3B566612DA0F0A");
    assertThat(create.flags().tfFullyCanonicalSig()).isTrue();
    assertThat(create.cancelAfter()).isPresent().get().isEqualTo(UnsignedLong.valueOf(533171558));
  }

}
