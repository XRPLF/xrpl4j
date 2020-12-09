package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

@RunWith(Parameterized.class)
public class OfferFlagsTests extends AbstractFlagsTest {

  boolean tfFullyCanonicalSig;
  boolean tfPassive;
  boolean tfImmediateOrCancel;
  boolean tfFillOrKill;
  boolean tfSell;

  long expectedFlags;

  public OfferFlagsTests(
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

    expectedFlags = (tfFullyCanonicalSig ? Flags.OfferFlags.FULLY_CANONICAL_SIG.getValue() : 0L) |
        (tfPassive ? Flags.OfferFlags.PASSIVE.getValue() : 0L) |
        (tfImmediateOrCancel ? Flags.OfferFlags.IMMEDIATE_OR_CANCEL.getValue() : 0L) |
        (tfFillOrKill ? Flags.OfferFlags.FILL_OR_KILL.getValue() : 0L) |
        (tfSell ? Flags.OfferFlags.SELL.getValue() : 0L);
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return getBooleanCombinations(5);
  }

  @Test
  public void testFlagsConstructionWithIndividualFlags() {
    Flags.OfferFlags flags = Flags.OfferFlags.builder()
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
    Flags.OfferFlags flags = Flags.OfferFlags.of(expectedFlags);

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.tfFullyCanonicalSig()).isEqualTo(tfFullyCanonicalSig);
    assertThat(flags.tfPassive()).isEqualTo(tfPassive);
    assertThat(flags.tfImmediateOrCancel()).isEqualTo(tfImmediateOrCancel);
    assertThat(flags.tfFillOrKill()).isEqualTo(tfFillOrKill);
    assertThat(flags.tfSell()).isEqualTo(tfSell);
  }
}
