package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

@RunWith(Parameterized.class)
public class OfferFlagsTests extends AbstractFlagsTest {

  boolean lsfPassive;
  boolean lsfSell;

  long expectedFlags;

  public OfferFlagsTests(
      boolean lsfPassive,
      boolean lsfSell
  ) {
    this.lsfPassive = lsfPassive;
    this.lsfSell = lsfSell;

    expectedFlags = (lsfPassive ? Flags.OfferFlags.PASSIVE.getValue() : 0L) |
        (lsfSell ? Flags.OfferFlags.SELL.getValue() : 0L);
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return getBooleanCombinations(2);
  }

  @Test
  public void testDeriveIndividualFlagsFromFlags() {
    Flags.OfferFlags flags = Flags.OfferFlags.of(expectedFlags);

    assertThat(flags.getValue()).isEqualTo(expectedFlags);
    assertThat(flags.lsfPassive()).isEqualTo(lsfPassive);
    assertThat(flags.lsfSell()).isEqualTo(lsfSell);
  }
}
