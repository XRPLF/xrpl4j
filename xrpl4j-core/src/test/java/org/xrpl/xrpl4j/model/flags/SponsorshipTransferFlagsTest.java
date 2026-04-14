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
    assertThat(flags.getValue()).isEqualTo(0x80000001L);
  }

  @Test
  public void tfSponsorshipCreate() {
    SponsorshipTransferFlags flags = SponsorshipTransferFlags.builder()
      .tfSponsorshipCreate(true)
      .build();

    assertThat(flags.tfSponsorshipCreate()).isTrue();
    assertThat(flags.getValue()).isEqualTo(0x80000002L);
  }

  @Test
  public void tfSponsorshipReassign() {
    SponsorshipTransferFlags flags = SponsorshipTransferFlags.builder()
      .tfSponsorshipReassign(true)
      .build();

    assertThat(flags.tfSponsorshipReassign()).isTrue();
    assertThat(flags.getValue()).isEqualTo(0x80000004L);
  }

  @Test
  public void ofMethod() {
    SponsorshipTransferFlags flags = SponsorshipTransferFlags.of(0x80000001L);
    assertThat(flags.getValue()).isEqualTo(0x80000001L);
    assertThat(flags.tfSponsorshipEnd()).isTrue();
    assertThat(flags.tfSponsorshipCreate()).isFalse();
    assertThat(flags.tfSponsorshipReassign()).isFalse();
  }

  @Test
  public void tfFullyCanonicalSigDefaultsToTrue() {
    SponsorshipTransferFlags flags = SponsorshipTransferFlags.builder()
      .build();

    // tfFullyCanonicalSig is true by default, so the value should be 0x80000000L
    assertThat(flags.getValue()).isEqualTo(0x80000000L);
  }

  @Test
  public void tfFullyCanonicalSigCanBeDisabled() {
    SponsorshipTransferFlags flags = SponsorshipTransferFlags.builder()
      .tfFullyCanonicalSig(false)
      .build();

    // Without tfFullyCanonicalSig, value should be 0
    assertThat(flags.getValue()).isEqualTo(0L);
  }

  @Test
  public void combinedFlags() {
    SponsorshipTransferFlags flags = SponsorshipTransferFlags.builder()
      .tfSponsorshipEnd(true)
      .tfSponsorshipCreate(true)
      .tfSponsorshipReassign(true)
      .build();

    assertThat(flags.tfSponsorshipEnd()).isTrue();
    assertThat(flags.tfSponsorshipCreate()).isTrue();
    assertThat(flags.tfSponsorshipReassign()).isTrue();
    // 0x80000000 + 0x00000001 + 0x00000002 + 0x00000004 = 0x80000007
    assertThat(flags.getValue()).isEqualTo(0x80000007L);
  }

}

