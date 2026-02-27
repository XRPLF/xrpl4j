package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.flags.OfferCreateFlags;

/**
 * Unit tests for {@link OfferCreate}.
 */
class OfferCreateTest {

  @Test
  public void testOfferCreate() {
    OfferCreate offerCreate = OfferCreate.builder()
      .account(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(12))
      .offerSequence(UnsignedInteger.valueOf(13))
      .takerPays(XrpCurrencyAmount.ofDrops(14))
      .takerGets(XrpCurrencyAmount.ofDrops(15))
      .expiration(UnsignedInteger.valueOf(16))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .domainId(Hash256.of("7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF0"))
      .flags(OfferCreateFlags.builder().tfHybrid(true).tfFillOrKill(true).build()).build();

    assertThat(offerCreate.transactionType()).isEqualTo(TransactionType.OFFER_CREATE);
    assertThat(offerCreate.account()).isEqualTo(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"));
    assertThat(offerCreate.sequence()).isEqualTo(UnsignedInteger.ONE);
    assertThat(offerCreate.fee()).isEqualTo(XrpCurrencyAmount.ofDrops(12));
    assertThat(offerCreate.offerSequence()).isPresent().get().isEqualTo(UnsignedInteger.valueOf(13));
    assertThat(offerCreate.takerPays()).isEqualTo(XrpCurrencyAmount.ofDrops(14));
    assertThat(offerCreate.takerGets()).isEqualTo(XrpCurrencyAmount.ofDrops(15));
    assertThat(offerCreate.expiration()).isPresent().get().isEqualTo(UnsignedInteger.valueOf(16));
    assertThat(offerCreate.domainId()).isPresent().get()
      .isEqualTo(Hash256.of("7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF0"));
    assertThat(offerCreate.flags()).isEqualTo(OfferCreateFlags.builder().tfHybrid(true).tfFillOrKill(true).build());
  }

  @Test
  public void testFlagAndDomainCombination() {
    assertThatThrownBy(() -> OfferCreate.builder()
      .account(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(12))
      .offerSequence(UnsignedInteger.valueOf(13))
      .takerPays(XrpCurrencyAmount.ofDrops(14))
      .takerGets(XrpCurrencyAmount.ofDrops(15))
      .expiration(UnsignedInteger.valueOf(16))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .flags(OfferCreateFlags.builder().tfHybrid(true).build()).build()
    )
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("tfHybrid flag cannot be set if the offer doesn't have a DomainID.");
  }

  @Test
  public void transactionFlagsReturnsEmptyFlagsWhenNoFlagsSet() {
    OfferCreate offerCreate = OfferCreate.builder()
      .account(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(12))
      .takerPays(XrpCurrencyAmount.ofDrops(14))
      .takerGets(XrpCurrencyAmount.ofDrops(15))
      .build();

    assertThat(offerCreate.transactionFlags()).isEqualTo(offerCreate.flags());
    assertThat(offerCreate.transactionFlags().isEmpty()).isTrue();
  }

  @Test
  public void transactionFlagsReturnsCorrectFlagsWhenFlagsSet() {
    OfferCreate offerCreate = OfferCreate.builder()
      .account(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(12))
      .takerPays(XrpCurrencyAmount.ofDrops(14))
      .takerGets(XrpCurrencyAmount.ofDrops(15))
      .flags(OfferCreateFlags.builder().tfPassive(true).tfSell(true).build())
      .build();

    assertThat(offerCreate.transactionFlags()).isEqualTo(offerCreate.flags());
    assertThat(offerCreate.transactionFlags().tfFullyCanonicalSig()).isTrue();
    assertThat(((OfferCreateFlags) offerCreate.transactionFlags()).tfPassive()).isTrue();
    assertThat(((OfferCreateFlags) offerCreate.transactionFlags()).tfSell()).isTrue();
  }

  @Test
  public void builderFromCopiesFlagsCorrectly() {
    OfferCreateFlags originalFlags = OfferCreateFlags.builder()
      .tfPassive(true)
      .tfSell(true)
      .build();

    OfferCreate original = OfferCreate.builder()
      .account(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(12))
      .takerPays(XrpCurrencyAmount.ofDrops(14))
      .takerGets(XrpCurrencyAmount.ofDrops(15))
      .flags(originalFlags)
      .build();

    OfferCreate copied = OfferCreate.builder()
      .from(original)
      .build();

    assertThat(copied.flags()).isEqualTo(original.flags());
    assertThat(copied.transactionFlags()).isEqualTo(original.transactionFlags());
    assertThat(((OfferCreateFlags) copied.transactionFlags()).tfPassive()).isTrue();
    assertThat(((OfferCreateFlags) copied.transactionFlags()).tfSell()).isTrue();
  }
}
