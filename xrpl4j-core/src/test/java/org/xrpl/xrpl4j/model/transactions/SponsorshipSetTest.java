package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.flags.SponsorshipSetFlags;

public class SponsorshipSetTest {

  @Test
  public void buildAsSponsorForSponsee() {
    SponsorshipSet set = SponsorshipSet.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .sponsee(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
      .feeAmount(XrpCurrencyAmount.ofDrops(1000000))
      .maxFee(XrpCurrencyAmount.ofDrops(100))
      .remainingOwnerCount(UnsignedInteger.valueOf(5))
      .build();

    assertThat(set.sponsee()).isPresent().get()
      .isEqualTo(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"));
    assertThat(set.counterpartySponsor()).isEmpty();
    assertThat(set.transactionType()).isEqualTo(TransactionType.SPONSORSHIP_SET);
  }

  @Test
  public void buildAsSponseeDeletingSponsorship() {
    SponsorshipSet set = SponsorshipSet.builder()
      .account(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
      .fee(XrpCurrencyAmount.ofDrops(10))
      .sequence(UnsignedInteger.ONE)
      .counterpartySponsor(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .flags(SponsorshipSetFlags.builder().tfDeleteObject().build())
      .build();

    assertThat(set.counterpartySponsor()).isPresent().get()
      .isEqualTo(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"));
    assertThat(set.flags().tfDeleteObject()).isTrue();
  }

  @Test
  public void neitherSponseeNorCounterpartySponsorFails() {
    assertThatThrownBy(() ->
      SponsorshipSet.builder()
        .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.ONE)
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Exactly one of CounterpartySponsor or Sponsee must be specified");
  }

  @Test
  public void bothSponseeAndCounterpartySponsorFails() {
    assertThatThrownBy(() ->
      SponsorshipSet.builder()
        .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.ONE)
        .sponsee(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
        .counterpartySponsor(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
        .flags(SponsorshipSetFlags.builder().tfDeleteObject().build())
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Exactly one of CounterpartySponsor or Sponsee must be specified");
  }

  @Test
  public void selfSponsorshipViaSponseeFails() {
    assertThatThrownBy(() ->
      SponsorshipSet.builder()
        .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.ONE)
        .sponsee(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("must not name its own account as the counterparty");
  }

  @Test
  public void selfSponsorshipViaCounterpartySponsorFails() {
    assertThatThrownBy(() ->
      SponsorshipSet.builder()
        .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.ONE)
        .counterpartySponsor(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .flags(SponsorshipSetFlags.builder().tfDeleteObject().build())
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("must not name its own account as the counterparty");
  }

  @Test
  public void counterpartySponsorWithoutDeleteObjectFails() {
    assertThatThrownBy(() ->
      SponsorshipSet.builder()
        .account(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.ONE)
        .counterpartySponsor(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Only the sponsor can create or update a Sponsorship object");
  }

  @Test
  public void deleteObjectWithFeeAmountFails() {
    assertThatThrownBy(() ->
      SponsorshipSet.builder()
        .account(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.ONE)
        .counterpartySponsor(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .feeAmount(XrpCurrencyAmount.ofDrops(1000000))
        .flags(SponsorshipSetFlags.builder().tfDeleteObject().build())
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("must not include FeeAmount, MaxFee, or RemainingOwnerCount");
  }

  @Test
  public void deleteObjectWithRequireSignForFeeFlagFails() {
    assertThatThrownBy(() ->
      SponsorshipSet.builder()
        .account(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.ONE)
        .counterpartySponsor(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .flags(SponsorshipSetFlags.builder().tfDeleteObject().tfRequireSignForFee().build())
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("must not set any RequireSignFor*/ClearRequireSignFor* flags");
  }

  @Test
  public void requireAndClearSignForFeeBothSetFails() {
    assertThatThrownBy(() ->
      SponsorshipSet.builder()
        .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.ONE)
        .sponsee(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
        .flags(SponsorshipSetFlags.builder().tfRequireSignForFee().tfClearRequireSignForFee().build())
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("tfRequireSignForFee and tfClearRequireSignForFee must not both be set");
  }

  @Test
  public void requireAndClearSignForReserveBothSetFails() {
    assertThatThrownBy(() ->
      SponsorshipSet.builder()
        .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.ONE)
        .sponsee(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
        .flags(SponsorshipSetFlags.builder().tfRequireSignForReserve().tfClearRequireSignForReserve().build())
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("tfRequireSignForReserve and tfClearRequireSignForReserve must not both be set");
  }

  @Test
  public void negativeFeeAmountFails() {
    assertThatThrownBy(() ->
      SponsorshipSet.builder()
        .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.ONE)
        .sponsee(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
        .feeAmount(XrpCurrencyAmount.ofDrops(-1))
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("FeeAmount must not be negative");
  }

  @Test
  public void negativeMaxFeeFails() {
    assertThatThrownBy(() ->
      SponsorshipSet.builder()
        .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .fee(XrpCurrencyAmount.ofDrops(10))
        .sequence(UnsignedInteger.ONE)
        .sponsee(Address.of("rPEPPER7kfTD9w2To4CQk6UCfuHM9c6GDY"))
        .maxFee(XrpCurrencyAmount.ofDrops(-1))
        .build()
    ).isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("MaxFee must not be negative");
  }

}
