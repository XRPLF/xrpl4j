package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;

/**
 * Unit tests for {@link OfferCancel}.
 */
class OfferCancelTest {

  @Test
  public void testOfferCancel() {
    OfferCancel offerCancel = OfferCancel.builder()
      .account(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(12))
      .offerSequence(UnsignedInteger.valueOf(13))
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      ).build();

    assertThat(offerCancel.transactionType()).isEqualTo(TransactionType.OFFER_CANCEL);
    assertThat(offerCancel.account()).isEqualTo(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"));
    assertThat(offerCancel.sequence()).isEqualTo(UnsignedInteger.ONE);
    assertThat(offerCancel.fee()).isEqualTo(XrpCurrencyAmount.ofDrops(12));
    assertThat(offerCancel.offerSequence()).isPresent().get().isEqualTo(UnsignedInteger.valueOf(13));
  }

  @Test
  public void transactionFlagsReturnsEmptyFlags() {
    OfferCancel offerCancel = OfferCancel.builder()
      .account(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(12))
      .offerSequence(UnsignedInteger.valueOf(13))
      .build();

    assertThat(offerCancel.transactionFlags()).isEqualTo(offerCancel.flags());
    assertThat(offerCancel.transactionFlags().isEmpty()).isTrue();
  }

  @Test
  public void builderFromCopiesFlagsCorrectly() {
    OfferCancel original = OfferCancel.builder()
      .account(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.ofDrops(12))
      .offerSequence(UnsignedInteger.valueOf(13))
      .build();

    OfferCancel copied = OfferCancel.builder()
      .from(original)
      .build();

    assertThat(copied.flags()).isEqualTo(original.flags());
    assertThat(copied.transactionFlags()).isEqualTo(original.transactionFlags());
  }
}
