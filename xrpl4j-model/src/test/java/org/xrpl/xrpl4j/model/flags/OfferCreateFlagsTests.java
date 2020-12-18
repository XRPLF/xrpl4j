package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

@RunWith(Parameterized.class)
public class OfferCreateFlagsTests extends AbstractFlagsTest {

  boolean tfFullyCanonicalSig;
  boolean tfPassive;
  boolean tfImmediateOrCancel;
  boolean tfFillOrKill;
  boolean tfSell;

  long expectedFlags;

  /**
   * Required-args constructor.
   *
   * @param tfFullyCanonicalSig The current value of {@link this.tfFullyCanonicalSig}.
   * @param tfPassive           The current value of {@link this.tfPassive}.
   * @param tfImmediateOrCancel The current value of {@link this.tfImmediateOrCancel}.
   * @param tfFillOrKill        The current value of {@link this.tfFillOrKill}.
   * @param tfSell              The current value of {@link this.tfSell}.
   */
  public OfferCreateFlagsTests(
      boolean tfFullyCanonicalSig,
      boolean tfPassive,
      boolean tfImmediateOrCancel,
      boolean tfFillOrKill,
      boolean tfSell
  ) {
    this.tfFullyCanonicalSig = tfFullyCanonicalSig;
    this.tfPassive = tfPassive;
    this.tfImmediateOrCancel = tfImmediateOrCancel;
    this.tfFillOrKill = tfFillOrKill;
    this.tfSell = tfSell;

    expectedFlags = (tfFullyCanonicalSig ? Flags.OfferCreateFlags.FULLY_CANONICAL_SIG.getValue() : 0L) |
        (tfPassive ? Flags.OfferCreateFlags.PASSIVE.getValue() : 0L) |
        (tfImmediateOrCancel ? Flags.OfferCreateFlags.IMMEDIATE_OR_CANCEL.getValue() : 0L) |
        (tfFillOrKill ? Flags.OfferCreateFlags.FILL_OR_KILL.getValue() : 0L) |
        (tfSell ? Flags.OfferCreateFlags.SELL.getValue() : 0L);
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return getBooleanCombinations(5);
  }

  @Test
  public void testFlagsConstructionWithIndividualFlags() {
    Flags.OfferCreateFlags flags = Flags.OfferCreateFlags.builder()
        .tfFullyCanonicalSig(tfFullyCanonicalSig)
        .tfPassive(tfPassive)
        .tfImmediateOrCancel(tfImmediateOrCancel)
        .tfFillOrKill(tfFillOrKill)
        .tfSell(tfSell)
        .build();

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
  }

  @Test
  public void testDeriveIndividualFlagsFromFlags() {
    Flags.OfferCreateFlags flags = Flags.OfferCreateFlags.of(expectedFlags);

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.tfFullyCanonicalSig()).isEqualTo(tfFullyCanonicalSig);
    assertThat(flags.tfPassive()).isEqualTo(tfPassive);
    assertThat(flags.tfImmediateOrCancel()).isEqualTo(tfImmediateOrCancel);
    assertThat(flags.tfFillOrKill()).isEqualTo(tfFillOrKill);
    assertThat(flags.tfSell()).isEqualTo(tfSell);
  }
}
