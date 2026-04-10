package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.flags.SponsorshipTransferFlags;

public class SponsorshipTransferTest {

  @Test
  public void buildEndSponsorship() {
    SponsorshipTransfer transfer = SponsorshipTransfer.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .objectId(Hash256.of("E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321"))
      .flags(SponsorshipTransferFlags.builder().tfSponsorshipEnd(true).build())
      .build();

    assertThat(transfer.account()).isEqualTo(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"));
    assertThat(transfer.objectId()).isPresent().get()
      .isEqualTo(Hash256.of("E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321"));
    assertThat(transfer.sponsor()).isEmpty();
    assertThat(transfer.flags().tfSponsorshipEnd()).isTrue();
  }

  @Test
  public void buildCreateSponsorship() {
    SponsorshipTransfer transfer = SponsorshipTransfer.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .objectId(Hash256.of("E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321"))
      .sponsor(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
      .flags(SponsorshipTransferFlags.builder().tfSponsorshipCreate(true).build())
      .build();

    assertThat(transfer.sponsor()).isPresent().get().isEqualTo(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"));
    assertThat(transfer.flags().tfSponsorshipCreate()).isTrue();
  }

  @Test
  public void buildReassignSponsorship() {
    SponsorshipTransfer transfer = SponsorshipTransfer.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .objectId(Hash256.of("E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321"))
      .sponsor(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
      .flags(SponsorshipTransferFlags.builder().tfSponsorshipReassign(true).build())
      .build();

    assertThat(transfer.sponsor()).isPresent().get().isEqualTo(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"));
    assertThat(transfer.flags().tfSponsorshipReassign()).isTrue();
  }

  @Test
  public void transactionType() {
    SponsorshipTransfer transfer = SponsorshipTransfer.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .objectId(Hash256.of("E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321"))
      .flags(SponsorshipTransferFlags.builder().tfSponsorshipEnd(true).build())
      .build();

    assertThat(transfer.transactionType()).isEqualTo(TransactionType.SPONSORSHIP_TRANSFER);
  }

  @Test
  public void noModeFlagsFails() {
    assertThatThrownBy(() ->
      SponsorshipTransfer.builder()
        .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.ONE)
        .objectId(Hash256.of("E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321"))
        .flags(SponsorshipTransferFlags.empty())
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("SponsorshipTransfer must have exactly one mode flag set");
  }

  @Test
  public void defaultFlagsFails() {
    // When flags() is not explicitly set, it defaults to empty flags which should fail validation
    assertThatThrownBy(() ->
      SponsorshipTransfer.builder()
        .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.ONE)
        .objectId(Hash256.of("E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321"))
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("SponsorshipTransfer must have exactly one mode flag set");
  }

  @Test
  public void multipleModeFlagsFails() {
    assertThatThrownBy(() ->
      SponsorshipTransfer.builder()
        .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.ONE)
        .objectId(Hash256.of("E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321"))
        .flags(SponsorshipTransferFlags.builder()
          .tfSponsorshipEnd(true)
          .tfSponsorshipCreate(true)
          .build())
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("SponsorshipTransfer must have exactly one mode flag set");
  }

  @Test
  public void allModeFlagsFails() {
    assertThatThrownBy(() ->
      SponsorshipTransfer.builder()
        .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.ONE)
        .objectId(Hash256.of("E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321"))
        .flags(SponsorshipTransferFlags.builder()
          .tfSponsorshipEnd(true)
          .tfSponsorshipCreate(true)
          .tfSponsorshipReassign(true)
          .build())
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("SponsorshipTransfer must have exactly one mode flag set");
  }

}
