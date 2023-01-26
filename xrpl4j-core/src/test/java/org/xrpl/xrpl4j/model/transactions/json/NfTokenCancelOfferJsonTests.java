package org.xrpl.xrpl4j.model.transactions.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.NfTokenCancelOffer;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.ArrayList;
import java.util.List;

public class NfTokenCancelOfferJsonTests extends AbstractJsonTest {

  @Test
  public void testMinimalNfTokenCancelOfferJson() throws JsonProcessingException, JSONException {

    Hash256 offer = Hash256.of("000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65");
    List<Hash256> offers = new ArrayList<>();
    offers.add(offer);
    NfTokenCancelOffer nfTokenCancelOffer = NfTokenCancelOffer.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.valueOf(12))
      .tokenOffers(offers)
      .signingPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      .build();

    String json = "{\n" +
      "    \"TransactionType\": \"NFTokenCancelOffer\",\n" +
      "    \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"Fee\": \"12\",\n" +
      "    \"Sequence\": 12,\n" +
      "    \"NFTokenOffers\": [" +
      "                     \"000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65\"" +
      "                  ],\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"Flags\": 2147483648\n" +
      "}";

    assertCanSerializeAndDeserialize(nfTokenCancelOffer, json);
  }
}
