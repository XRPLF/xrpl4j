package com.ripple.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class OfferFlagsTest {

  @Test
  public void defaultFlags() {
    Flags.OfferFlags flags = Flags.OfferFlags.builder().build();
    assertThat(flags.tfFullyCanonicalSig()).isTrue();
    assertThat(flags.tfFillOrKill()).isFalse();
    assertThat(flags.tfImmediateOrCancel()).isFalse();
    assertThat(flags.tfPassive()).isFalse();
    assertThat(flags.tfSell()).isFalse();
  }

  @Test
  public void allFlagsSet() {
    Flags.OfferFlags flags = Flags.OfferFlags.builder()
      .fillOrKill(true)
      .immediateOrCancel(true)
      .passive(true)
      .sell(true)
      .build();
    assertThat(flags.tfFullyCanonicalSig()).isTrue();
    assertThat(flags.tfFillOrKill()).isTrue();
    assertThat(flags.tfImmediateOrCancel()).isTrue();
    assertThat(flags.tfPassive()).isTrue();
    assertThat(flags.tfSell()).isTrue();
  }

  @Test
  public void partialFlagsSet() {
    Flags.OfferFlags flags = Flags.OfferFlags.builder()
      .fillOrKill(true)
      .immediateOrCancel(false)
      .passive(true)
      .sell(false)
      .build();
    assertThat(flags.tfFullyCanonicalSig()).isTrue();
    assertThat(flags.tfFillOrKill()).isTrue();
    assertThat(flags.tfImmediateOrCancel()).isFalse();
    assertThat(flags.tfPassive()).isTrue();
    assertThat(flags.tfSell()).isFalse();
  }


}
