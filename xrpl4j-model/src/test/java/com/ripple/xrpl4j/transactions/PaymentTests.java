package com.ripple.xrpl4j.transactions;

import com.google.common.primitives.UnsignedInteger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PaymentTests {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void constructXrpToXrpPaymentWithSendMax() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("SendMax cannot be set for an XRP-to-XRP payment.");

    Payment.builder()
      .account(Address.of("r9TeThyi5xiuUUrFjtPKZiHcDxs7K9H6Rb"))
      .destination(Address.of("r4BPgS7DHebQiU31xWELvZawwSG2fSPJ7C"))
      .amount(XrpCurrencyAmount.of("25000000"))
      .fee("10")
      .sendMax(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(Address.of("r9TeThyi5xiuUUrFjtPKZiHcDxs7K9H6Rb"))
        .value("30000000")
        .build())
      .build();
  }

  @Test
  public void constructIssuedToIssuedPaymentWithSendMax() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("SendMax cannot be set for an XRP-to-XRP payment.");

    Payment.builder()
      .account(Address.of("r9TeThyi5xiuUUrFjtPKZiHcDxs7K9H6Rb"))
      .destination(Address.of("r4BPgS7DHebQiU31xWELvZawwSG2fSPJ7C"))
      .amount(XrpCurrencyAmount.of("25000000"))
      .fee("10")
      .sendMax(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(Address.of("r9TeThyi5xiuUUrFjtPKZiHcDxs7K9H6Rb"))
        .value("30000000")
        .build())
      .build();
  }
}
