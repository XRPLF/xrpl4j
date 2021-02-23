package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedInteger;
import org.junit.Test;

/**
 * Unit tests for {@link Payment}.
 */
public class PaymentTest {

  @Test
  public void paymentBuilder() {
    assertThat(xrpPayment()).isNotNull();
  }

  @Test
  public void flags() {
    assertThat(xrpPayment().flags().tfFullyCanonicalSig()).isTrue();
  }

  @Test
  public void amountAsXrp() {
    assertThat(xrpPayment().amountAsXrp()).isPresent();
    assertThat(issuedCurrencyPayment().amountAsXrp()).isEmpty();
  }

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