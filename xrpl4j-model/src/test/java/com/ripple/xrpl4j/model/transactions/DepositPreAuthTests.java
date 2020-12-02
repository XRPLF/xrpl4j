package com.ripple.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedInteger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DepositPreAuthTests {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void depositPreAuthWithAuthorize() {
    Address authorize = Address.of("rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de");
    DepositPreAuth depositPreAuth = DepositPreAuth.builder()
        .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.valueOf(2))
        .authorize(authorize)
        .build();

    assertThat(depositPreAuth.authorize()).isNotEmpty().get().isEqualTo(authorize);
    assertThat(depositPreAuth.unauthorize()).isEmpty();
  }

  @Test
  public void depositPreAuthWithUnauthorize() {
    Address unauthorize = Address.of("rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de");
    DepositPreAuth depositPreAuth = DepositPreAuth.builder()
        .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.valueOf(2))
        .unauthorize(unauthorize)
        .build();

    assertThat(depositPreAuth.unauthorize()).isNotEmpty().get().isEqualTo(unauthorize);
    assertThat(depositPreAuth.authorize()).isEmpty();
  }

  @Test
  public void depositPreAuthWithoutAuthorizeOrUnauthorizeThrows() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage(
        "The DepositPreAuth transaction must include either Authorize or Unauthorize, but not both."
    );

    DepositPreAuth.builder()
        .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.valueOf(2))
        .build();
  }

  @Test
  public void depositPreAuthWithAuthorizeAndUnauthorizeThrows() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage(
        "The DepositPreAuth transaction must include either Authorize or Unauthorize, but not both."
    );

    DepositPreAuth.builder()
        .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.valueOf(2))
        .authorize(Address.of("rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de"))
        .unauthorize(Address.of("rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de"))
        .build();
  }
}
