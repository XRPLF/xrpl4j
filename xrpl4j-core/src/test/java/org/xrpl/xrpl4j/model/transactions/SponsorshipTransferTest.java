package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.signing.Signature;
import org.xrpl.xrpl4j.model.flags.SponsorFlags;
import org.xrpl.xrpl4j.model.flags.SponsorshipTransferFlags;

public class SponsorshipTransferTest {

  private static final String TEST_PUBLIC_KEY =
    "ED5F5AC8B98974A3CA843326D9B88CEBD0560177B973EE0B149F782CFAA06DC66A";
  private static final String TEST_SIGNATURE =
    "3045022100D184EB4AE5956FF600E7536EE459345C7BBCF097A84CC61A93B9AF7197EDB98702201E" +
      "F0EBFB08929B1C1171B4D4B943774D6388B3B2F1F1E2F3E4F5F6F7F8F9FA";

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
      .sponsorFlags(SponsorFlags.SPONSOR_RESERVE)
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
      .sponsorFlags(SponsorFlags.SPONSOR_RESERVE)
      .flags(SponsorshipTransferFlags.builder().tfSponsorshipReassign(true).build())
      .build();

    assertThat(transfer.sponsor()).isPresent().get().isEqualTo(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"));
    assertThat(transfer.flags().tfSponsorshipReassign()).isTrue();
  }

  @Test
  public void endSponsorshipWithSponsorPresentFails() {
    assertThatThrownBy(() -> SponsorshipTransfer.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .objectId(Hash256.of("E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321"))
      .sponsor(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
      .flags(SponsorshipTransferFlags.builder().tfSponsorshipEnd(true).build())
      .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Sponsor must not be present when tfSponsorshipEnd is set");
  }

  @Test
  public void endSponsorshipWithReserveFlagFails() {
    assertThatThrownBy(() -> SponsorshipTransfer.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .objectId(Hash256.of("E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321"))
      .sponsorFlags(SponsorFlags.SPONSOR_RESERVE)
      .flags(SponsorshipTransferFlags.builder().tfSponsorshipEnd(true).build())
      .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("SponsorFlags must not include spfSponsorReserve when tfSponsorshipEnd is set");
  }

  @Test
  public void endSponsorshipWithFeeFlagSucceeds() {
    // tfSponsorshipEnd permits SponsorFlags that do not include spfSponsorReserve (e.g. spfSponsorFee).
    SponsorshipTransfer transfer = SponsorshipTransfer.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .objectId(Hash256.of("E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321"))
      .sponsorFlags(SponsorFlags.SPONSOR_FEE)
      .flags(SponsorshipTransferFlags.builder().tfSponsorshipEnd(true).build())
      .build();

    assertThat(transfer.sponsorFlags()).isPresent().get().isEqualTo(SponsorFlags.SPONSOR_FEE);
  }

  @Test
  public void createSponsorshipWithoutSponsorFails() {
    assertThatThrownBy(() -> SponsorshipTransfer.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .objectId(Hash256.of("E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321"))
      .sponsorFlags(SponsorFlags.SPONSOR_RESERVE)
      .flags(SponsorshipTransferFlags.builder().tfSponsorshipCreate(true).build())
      .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Sponsor and SponsorFlags are both required");
  }

  @Test
  public void createSponsorshipWithoutSponsorFlagsFails() {
    assertThatThrownBy(() -> SponsorshipTransfer.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .objectId(Hash256.of("E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321"))
      .sponsor(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
      .flags(SponsorshipTransferFlags.builder().tfSponsorshipCreate(true).build())
      .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Sponsor and SponsorFlags are both required");
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

  @Test
  public void buildEndSponsorshipForAnotherAccount() {
    SponsorshipTransfer transfer = SponsorshipTransfer.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .objectId(Hash256.of("E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321"))
      .sponsee(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
      .flags(SponsorshipTransferFlags.builder().tfSponsorshipEnd(true).build())
      .build();

    assertThat(transfer.sponsee()).isPresent().get()
      .isEqualTo(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"));
  }

  @Test
  public void sponseeWithCreateFails() {
    assertThatThrownBy(() ->
      SponsorshipTransfer.builder()
        .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.ONE)
        .objectId(Hash256.of("E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321"))
        .sponsee(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
        .sponsor(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
        .flags(SponsorshipTransferFlags.builder().tfSponsorshipCreate(true).build())
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Sponsee must not be present unless tfSponsorshipEnd is set");
  }

  @Test
  public void sponseeWithReassignFails() {
    assertThatThrownBy(() ->
      SponsorshipTransfer.builder()
        .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.ONE)
        .objectId(Hash256.of("E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321"))
        .sponsee(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
        .sponsor(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
        .flags(SponsorshipTransferFlags.builder().tfSponsorshipReassign(true).build())
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Sponsee must not be present unless tfSponsorshipEnd is set");
  }

  @Test
  public void accountLevelReassignWithoutSponsorSignatureFails() {
    assertThatThrownBy(() ->
      SponsorshipTransfer.builder()
        .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.ONE)
        .sponsor(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
        .flags(SponsorshipTransferFlags.builder().tfSponsorshipReassign(true).build())
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("requires a SponsorSignature from the new sponsor");
  }

  @Test
  public void accountLevelReassignWithSponsorSignatureSucceeds() {
    SponsorSignature sponsorSignature = SponsorSignature.builder()
      .signingPublicKey(PublicKey.fromBase16EncodedPublicKey(TEST_PUBLIC_KEY))
      .transactionSignature(Signature.fromBase16(TEST_SIGNATURE))
      .build();

    SponsorshipTransfer transfer = SponsorshipTransfer.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .sponsor(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
      .sponsorFlags(SponsorFlags.SPONSOR_RESERVE)
      .sponsorSignature(sponsorSignature)
      .flags(SponsorshipTransferFlags.builder().tfSponsorshipReassign(true).build())
      .build();

    assertThat(transfer.objectId()).isEmpty();
    assertThat(transfer.sponsorSignature()).isPresent().get().isEqualTo(sponsorSignature);
  }

  @Test
  public void objectLevelReassignWithoutSponsorSignatureSucceeds() {
    SponsorshipTransfer transfer = SponsorshipTransfer.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .objectId(Hash256.of("E6DBAFC99223B42257915A63DFC6B0C032D4070F9A574B255AD97466726FC321"))
      .sponsor(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
      .sponsorFlags(SponsorFlags.SPONSOR_RESERVE)
      .flags(SponsorshipTransferFlags.builder().tfSponsorshipReassign(true).build())
      .build();

    assertThat(transfer.sponsorSignature()).isEmpty();
  }

}
