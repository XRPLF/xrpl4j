package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.OfferCreateFlags;

class OfferTests extends AbstractJsonTest {

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
      .flags(OfferCreateFlags.builder().tfHybrid(true).build())
      .networkId(NetworkId.of(1024)).build())
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("tfHybrid cannot be set if the offer doesn't have a DomainID.");
  }

  @Test
  public void testOfferCreate() {
    OfferCreate.builder()
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
      .flags(OfferCreateFlags.builder().tfHybrid(true).tfFillOrKill(true).build())
      .networkId(NetworkId.of(1024)).build();
  }
}