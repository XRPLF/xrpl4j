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
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.flags.Flags;

public class AccountSetTests {

  @Test
  public void simpleAccountSet() {
    AccountSet accountSet = AccountSet.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.valueOf(5))
      .domain("6578616D706C652E636F6D")
      .setFlag(AccountSet.AccountSetFlag.ACCOUNT_TXN_ID)
      .messageKey("03AB40A0490F9B7ED8DF29D246BF2D6269820A0EE7742ACDD457BEA7C7D0931EDB")
      .transferRate(UnsignedInteger.valueOf(1000000001))
      .tickSize(UnsignedInteger.valueOf(15))
      .build();

    assertThat(accountSet.transactionType()).isEqualTo(TransactionType.ACCOUNT_SET);
    assertThat(accountSet.account()).isEqualTo(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"));
    assertThat(accountSet.fee().value()).isEqualTo(UnsignedLong.valueOf(12));
    assertThat(accountSet.sequence()).isEqualTo(UnsignedInteger.valueOf(5));
    assertThat(accountSet.domain()).isNotEmpty().get().isEqualTo("6578616D706C652E636F6D");
    assertThat(accountSet.setFlag()).isNotEmpty().get().isEqualTo(AccountSet.AccountSetFlag.ACCOUNT_TXN_ID);
    assertThat(accountSet.messageKey()).isNotEmpty().get()
      .isEqualTo("03AB40A0490F9B7ED8DF29D246BF2D6269820A0EE7742ACDD457BEA7C7D0931EDB");
    assertThat(accountSet.transferRate()).isNotEmpty().get().isEqualTo(UnsignedInteger.valueOf(1000000001));
    assertThat(accountSet.flags()).isEqualTo(Flags.AccountSetTransactionFlags.builder().build());
  }

  @Test
  void accountSetWithSetFlagAndTransactionFlags() {
    Flags.AccountSetTransactionFlags flags = Flags.AccountSetTransactionFlags.builder()
      .tfRequireAuth()
      .tfRequireDestTag()
      .tfDisallowXrp()
      .build();
    AccountSet accountSet = AccountSet.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.valueOf(5))
      .domain("6578616D706C652E636F6D")
      .setFlag(AccountSet.AccountSetFlag.ACCOUNT_TXN_ID)
      .flags(flags)
      .messageKey("03AB40A0490F9B7ED8DF29D246BF2D6269820A0EE7742ACDD457BEA7C7D0931EDB")
      .transferRate(UnsignedInteger.valueOf(1000000001))
      .tickSize(UnsignedInteger.valueOf(15))
      .build();

    assertThat(accountSet.transactionType()).isEqualTo(TransactionType.ACCOUNT_SET);
    assertThat(accountSet.account()).isEqualTo(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"));
    assertThat(accountSet.fee().value()).isEqualTo(UnsignedLong.valueOf(12));
    assertThat(accountSet.sequence()).isEqualTo(UnsignedInteger.valueOf(5));
    assertThat(accountSet.domain()).isNotEmpty().get().isEqualTo("6578616D706C652E636F6D");
    assertThat(accountSet.setFlag()).isNotEmpty().get().isEqualTo(AccountSet.AccountSetFlag.ACCOUNT_TXN_ID);
    assertThat(accountSet.messageKey()).isNotEmpty().get()
      .isEqualTo("03AB40A0490F9B7ED8DF29D246BF2D6269820A0EE7742ACDD457BEA7C7D0931EDB");
    assertThat(accountSet.transferRate()).isNotEmpty().get().isEqualTo(UnsignedInteger.valueOf(1000000001));
    assertThat(accountSet.flags()).isEqualTo(flags);
  }

  @Test
  public void emailHashTooLong() {
    assertThrows(
      IllegalArgumentException.class,
      () -> AccountSet.builder()
        .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
        .fee(XrpCurrencyAmount.ofDrops(12))
        .sequence(UnsignedInteger.valueOf(5))
        .emailHash("f9879d71855b5ff21e4963273a886bfc1")
        .build(),
      "emailHash must be 32 characters (128 bits), but was 33 characters long."
    );

  }

  @Test
  public void transferRateTooLow() {
    assertThrows(
      IllegalArgumentException.class,
      () -> AccountSet.builder()
        .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
        .fee(XrpCurrencyAmount.ofDrops(12))
        .sequence(UnsignedInteger.valueOf(5))
        .transferRate(UnsignedInteger.valueOf(999999999))
        .build(),
      "transferRate must be between 1,000,000,000 and 2,000,000,000 or equal to 0."
    );
  }

  @Test
  public void transferRateTooHigh() {
    assertThrows(
      IllegalArgumentException.class,
      () -> AccountSet.builder()
        .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
        .fee(XrpCurrencyAmount.ofDrops(12))
        .sequence(UnsignedInteger.valueOf(5))
        .transferRate(UnsignedInteger.valueOf(2000000001))
        .build(),
      "transferRate must be between 1,000,000,000 and 2,000,000,000 or equal to 0."
    );
  }

  @Test
  public void transferRateIsZero() {
    AccountSet accountSet = AccountSet.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.valueOf(5))
      .transferRate(UnsignedInteger.ZERO)
      .build();

    assertThat(accountSet.transferRate()).isNotEmpty().get().isEqualTo(UnsignedInteger.ZERO);
  }

  @Test
  public void tickSizeTooLow() {
    assertThrows(
      IllegalArgumentException.class,
      () -> AccountSet.builder()
        .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
        .fee(XrpCurrencyAmount.ofDrops(12))
        .sequence(UnsignedInteger.valueOf(5))
        .tickSize(UnsignedInteger.valueOf(2))
        .build(),
      "tickSize must be between 3 and 15 inclusive or be equal to 0."
    );
  }

  @Test
  public void tickSizeTooHigh() {
    assertThrows(
      IllegalArgumentException.class,
      () -> AccountSet.builder()
        .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
        .fee(XrpCurrencyAmount.ofDrops(12))
        .sequence(UnsignedInteger.valueOf(5))
        .tickSize(UnsignedInteger.valueOf(16))
        .build(),
      "tickSize must be between 3 and 15 inclusive or be equal to 0."
    );
  }

  @Test
  public void tickSizeIsZero() {
    AccountSet accountSet = AccountSet.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.valueOf(5))
      .tickSize(UnsignedInteger.ZERO)
      .build();

    assertThat(accountSet.tickSize()).isNotEmpty().get().isEqualTo(UnsignedInteger.ZERO);
  }
}
