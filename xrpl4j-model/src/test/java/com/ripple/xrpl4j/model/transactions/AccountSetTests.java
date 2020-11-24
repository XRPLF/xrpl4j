package com.ripple.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import com.ripple.xrpl4j.model.transactions.AccountSet.AccountSetFlag;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class AccountSetTests {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void simpleAccountSet() {
    AccountSet accountSet = AccountSet.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.valueOf(5))
      .domain("6578616D706C652E636F6D")
      .setFlag(AccountSetFlag.ACCOUNT_TXN_ID)
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
    assertThat(accountSet.messageKey()).isNotEmpty().get().isEqualTo("03AB40A0490F9B7ED8DF29D246BF2D6269820A0EE7742ACDD457BEA7C7D0931EDB");
    assertThat(accountSet.transferRate()).isNotEmpty().get().isEqualTo(UnsignedInteger.valueOf(1000000001));
  }

  @Test
  public void emailHashTooLong() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("emailHash must be 32 characters (128 bits), but was 33 characters long.");
    AccountSet.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.valueOf(5))
      .emailHash("f9879d71855b5ff21e4963273a886bfc1")
      .build();
  }

  @Test
  public void transferRateTooLow() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("transferRate must be between 1,000,000,000 and 2,000,000,000 or equal to 0.");
    AccountSet.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.valueOf(5))
      .transferRate(UnsignedInteger.valueOf(999999999))
      .build();
  }

  @Test
  public void transferRateTooHigh() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("transferRate must be between 1,000,000,000 and 2,000,000,000 or equal to 0.");
    AccountSet.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.valueOf(5))
      .transferRate(UnsignedInteger.valueOf(2000000001))
      .build();
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
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("tickSize must be between 3 and 15 inclusive or be equal to 0.");
    AccountSet.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.valueOf(5))
      .tickSize(UnsignedInteger.valueOf(2))
      .build();
  }

  @Test
  public void tickSizeTooHigh() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("tickSize must be between 3 and 15 inclusive or be equal to 0.");
    AccountSet.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.valueOf(5))
      .tickSize(UnsignedInteger.valueOf(16))
      .build();
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
