package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

@RunWith(Parameterized.class)
public class PaymentFlagsTests extends AbstractFlagsTest {

  boolean tfFullyCanonicalSig;
  boolean tfNoDirectRipple;
  boolean tfPartialPayment;
  boolean tfLimitQuality;

  long expectedFlags;

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

    expectedFlags = (tfFullyCanonicalSig ? Flags.PaymentFlags.FULLY_CANONICAL_SIG.getValue() : 0L) |
        (tfNoDirectRipple ? Flags.PaymentFlags.NO_DIRECT_RIPPLE.getValue() : 0L) |
        (tfPartialPayment ? Flags.PaymentFlags.PARTIAL_PAYMENT.getValue() : 0L) |
        (tfLimitQuality ? Flags.PaymentFlags.LIMIT_QUALITY.getValue() : 0L);
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return getBooleanCombinations(4);
  }

  @Test
  public void testFlagsConstructionWithIndividualFlags() {
    Flags.PaymentFlags flags = Flags.PaymentFlags.builder()
        .fullyCanonicalSig(tfFullyCanonicalSig)
        .noDirectRipple(tfNoDirectRipple)
        .partialPayment(tfPartialPayment)
        .limitQuality(tfLimitQuality)
        .build();

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
  }

  @Test
  public void testDeriveIndividualFlagsFromFlags() {
    Flags.PaymentFlags flags = Flags.PaymentFlags.of(expectedFlags);

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.tfFullyCanonicalSig()).isEqualTo(tfFullyCanonicalSig);
    assertThat(flags.tfNoDirectRipple()).isEqualTo(tfNoDirectRipple);
    assertThat(flags.tfPartialPayment()).isEqualTo(tfPartialPayment);
    assertThat(flags.tfLimitQuality()).isEqualTo(tfLimitQuality);
  }
}
