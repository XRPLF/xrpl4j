package org.xrpl.xrpl4j.model.flags;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class SponsorshipTransferFlagsTest {

  @Test
  public void emptyFlags() {
    SponsorshipTransferFlags flags = SponsorshipTransferFlags.empty();
    // empty() returns 0, which is truly empty
    assertThat(flags.getValue()).isEqualTo(0L);
    assertThat(flags.tfSponsorshipEnd()).isFalse();
    assertThat(flags.tfSponsorshipCreate()).isFalse();
    assertThat(flags.tfSponsorshipReassign()).isFalse();
  }

  @Test
  public void tfSponsorshipEnd() {
    SponsorshipTransferFlags flags = SponsorshipTransferFlags.builder()
      .tfSponsorshipEnd(true)
      .build();

    assertThat(flags.tfSponsorshipEnd()).isTrue();
    assertThat(flags.getValue()).isEqualTo(0x80010000L);
  }

  @Test
  public void tfSponsorshipCreate() {
    SponsorshipTransferFlags flags = SponsorshipTransferFlags.builder()
      .tfSponsorshipCreate(true)
      .build();

    assertThat(flags.tfSponsorshipCreate()).isTrue();
    assertThat(flags.getValue()).isEqualTo(0x80020000L);
  }

  @Test
  public void tfSponsorshipReassign() {
    SponsorshipTransferFlags flags = SponsorshipTransferFlags.builder()
      .tfSponsorshipReassign(true)
      .build();

    assertThat(flags.tfSponsorshipReassign()).isTrue();
    assertThat(flags.getValue()).isEqualTo(0x80040000L);
  }

}

