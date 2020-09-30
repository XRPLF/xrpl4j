package com.ripple.xrpl4j.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedInteger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class PaymentFlagsTests {


  boolean tfFullyCanonicalSig;
  boolean tfNoDirectRipple;
  boolean tfPartialPayment;
  boolean tfLimitQuality;

  public PaymentFlagsTests(
    boolean tfFullyCanonicalSig,
    boolean tfNoDirectRipple,
    boolean tfPartialPayment,
    boolean tfLimitQuality
  ) {
    this.tfFullyCanonicalSig = tfFullyCanonicalSig;
    this.tfNoDirectRipple = tfNoDirectRipple;
    this.tfPartialPayment = tfPartialPayment;
    this.tfLimitQuality = tfLimitQuality;
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    // Every combination of 4 booleans
    List<Object[]> params = new ArrayList<>();
    int n = 4;
    for (int i = 0; i < Math.pow(2, n); i++) {
      String bin = Integer.toBinaryString(i);
      while (bin.length() < n) {
        bin = "0" + bin;
      }

      char[] chars = bin.toCharArray();
      Boolean[] booleans = new Boolean[n];
      for (int j = 0; j < chars.length; j++) {
        booleans[j] = chars[j] == '0';
      }

      params.add(booleans);
    }

    return params;
  }

  @Test
  public void testFlagsConstructionWithIndividualFlags() {
    Payment payment = Payment.builder()
      .account(Address.of("r9TeThyi5xiuUUrFjtPKZiHcDxs7K9H6Rb"))
      .destination(Address.of("r4BPgS7DHebQiU31xWELvZawwSG2fSPJ7C"))
      .amount(XrpCurrencyAmount.of("25000000"))
      .fee(XrpCurrencyAmount.of("10"))
      .tfFullyCanonicalSig(tfFullyCanonicalSig)
      .tfNoDirectRipple(tfNoDirectRipple)
      .tfPartialPayment(tfPartialPayment)
      .tfLimitQuality(tfLimitQuality)
      .sequence(UnsignedInteger.ONE)
      .build();

    long expectedFlags = (tfFullyCanonicalSig ? 0x80000000L : 0L) |
      (tfNoDirectRipple ? 0x00010000L : 0L) |
      (tfPartialPayment ? 0x00020000L : 0L) |
      (tfLimitQuality ? 0x00040000L : 0L);

    assertThat(payment.flags()).isNotEmpty();
    assertThat(payment.flags().get().getValue()).isEqualTo(expectedFlags);
  }

  @Test
  public void testDeriveIndividualFlagsFromFlags() {
    long expectedFlags = (tfFullyCanonicalSig ? 0x80000000L : 0L) |
      (tfNoDirectRipple ? 0x00010000L : 0L) |
      (tfPartialPayment ? 0x00020000L : 0L) |
      (tfLimitQuality ? 0x00040000L : 0L);

    Payment payment = Payment.builder()
      .account(Address.of("r9TeThyi5xiuUUrFjtPKZiHcDxs7K9H6Rb"))
      .destination(Address.of("r4BPgS7DHebQiU31xWELvZawwSG2fSPJ7C"))
      .amount(XrpCurrencyAmount.of("25000000"))
      .fee(XrpCurrencyAmount.of("10"))
      .flags(Flags.of(expectedFlags))
      .sequence(UnsignedInteger.ONE)
      .build();

    assertThat(payment.flags()).isNotEmpty();
    assertThat(payment.flags().get().getValue()).isEqualTo(expectedFlags);
    assertThat(payment.tfFullyCanonicalSig()).isEqualTo(tfFullyCanonicalSig);
    assertThat(payment.tfNoDirectRipple()).isEqualTo(tfNoDirectRipple);
    assertThat(payment.tfPartialPayment()).isEqualTo(tfPartialPayment);
    assertThat(payment.tfLimitQuality()).isEqualTo(tfLimitQuality);
  }
}
