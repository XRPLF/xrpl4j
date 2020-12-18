package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

@RunWith(Parameterized.class)
public class PaymentChannelClaimFlagsTests extends AbstractFlagsTest {

  boolean tfFullyCanonicalSig;
  boolean tfRenew;
  boolean tfClose;
  long expectedFlags;

  /**
   * Required-args constructor.
   *
   * @param tfFullyCanonicalSig The current value of {@link this.tfFullyCanonicalSig}.
   * @param tfRenew             The current value of {@link this.tfRenew}.
   * @param tfClose             The current value of {@link this.tfClose}.
   */
  public PaymentChannelClaimFlagsTests(boolean tfFullyCanonicalSig, boolean tfRenew, boolean tfClose) {
    this.tfFullyCanonicalSig = tfFullyCanonicalSig;
    this.tfRenew = tfRenew;
    this.tfClose = tfClose;

    expectedFlags = (tfFullyCanonicalSig ? Flags.PaymentChannelClaimFlags.FULLY_CANONICAL_SIG.getValue() : 0L) |
        (tfRenew ? Flags.PaymentChannelClaimFlags.RENEW.getValue() : 0L) |
        (tfClose ? Flags.PaymentChannelClaimFlags.CLOSE.getValue() : 0L);
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return getBooleanCombinations(3);
  }

  @Test
  public void testFlagsConstructionWithIndividualFlags() {
    Flags.PaymentChannelClaimFlags flags = Flags.PaymentChannelClaimFlags.builder()
        .tfFullyCanonicalSig(tfFullyCanonicalSig)
        .tfRenew(tfRenew)
        .tfClose(tfClose)
        .build();

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
  }

  @Test
  public void testDeriveIndividualFlagsFromFlags() {
    Flags.PaymentChannelClaimFlags flags = Flags.PaymentChannelClaimFlags.of(expectedFlags);

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.tfFullyCanonicalSig()).isEqualTo(tfFullyCanonicalSig);
    assertThat(flags.tfRenew()).isEqualTo(tfRenew);
    assertThat(flags.tfClose()).isEqualTo(tfClose);
  }
}
