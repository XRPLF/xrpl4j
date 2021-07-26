package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;

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
  public void flagsForXrpPayment() {
    assertThat(xrpPayment().flags().tfFullyCanonicalSig()).isTrue();
  }

  @Test
  public void flagsForIssuedCurrency() {
    assertThat(issuedCurrencyPayment().flags().tfFullyCanonicalSig()).isTrue();
  }

  //////////////////
  // Private Helpers
  //////////////////

  private Payment xrpPayment() {
    return Payment.builder()
      .sequence(UnsignedInteger.ONE)
      .account(Address.of("foo"))
      .destination(Address.of("dest"))
      .fee(XrpCurrencyAmount.ofDrops(1000L))
      .amount(XrpCurrencyAmount.ofDrops(2000L))
      .build();
  }

  private Payment issuedCurrencyPayment() {
    return Payment.builder()
      .sequence(UnsignedInteger.ONE)
      .account(Address.of("foo"))
      .destination(Address.of("dest"))
      .fee(XrpCurrencyAmount.ofDrops(1000L))
      .amount(IssuedCurrencyAmount.builder().currency("USD").issuer(Address.of("foo")).value("500").build())
      .build();
  }
}
