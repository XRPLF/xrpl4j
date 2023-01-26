package org.xrpl.xrpl4j.model.transactions.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.NfTokenAcceptOffer;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class NfTokenAcceptOfferJsonTests extends AbstractJsonTest {

  @Test
  public void testMinimalNfTokenAcceptOfferJson() throws JsonProcessingException, JSONException {

    Hash256 offer = Hash256.of("000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65");
    NfTokenAcceptOffer nfTokenAcceptOffer = NfTokenAcceptOffer.builder()
      .fee(XrpCurrencyAmount.ofDrops(12))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .sequence(UnsignedInteger.valueOf(12))
      .brokerFee(XrpCurrencyAmount.ofDrops(10))
      .buyOffer(offer)
      .sellOffer(offer)
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json = "{\n" +
      "    \"TransactionType\": \"NFTokenAcceptOffer\",\n" +
      "    \"Account\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba\",\n" +
      "    \"Fee\": \"12\",\n" +
      "    \"Sequence\": 12,\n" +
      "    \"NFTokenBuyOffer\": \"000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65\",\n" +
      "    \"NFTokenSellOffer\": \"000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65\",\n" +
      "    \"Flags\": 2147483648,\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"NFTokenBrokerFee\": \"10\"\n" +
      "}";

    assertCanSerializeAndDeserialize(nfTokenAcceptOffer, json);
  }

  @Test
  public void testNfTokenAcceptOfferWithOnlyBuyOffer() throws JsonProcessingException, JSONException {

    Hash256 offer = Hash256.of("000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65");
    NfTokenAcceptOffer nfTokenAcceptOffer = NfTokenAcceptOffer.builder()
      .fee(XrpCurrencyAmount.ofDrops(12))
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba"))
      .sequence(UnsignedInteger.valueOf(12))
      .buyOffer(offer)
      .signingPublicKey(
        PublicKey.fromBase16EncodedPublicKey("02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC")
      )
      .build();

    String json = "{\n" +
      "    \"TransactionType\": \"NFTokenAcceptOffer\",\n" +
      "    \"Account\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59Ba\",\n" +
      "    \"Fee\": \"12\",\n" +
      "    \"Sequence\": 12,\n" +
      "    \"NFTokenBuyOffer\": \"000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65\",\n" +
      "    \"SigningPubKey\" : \"02356E89059A75438887F9FEE2056A2890DB82A68353BE9C0C0C8F89C0018B37FC\",\n" +
      "    \"Flags\": 2147483648\n" +
      "}";

    assertCanSerializeAndDeserialize(nfTokenAcceptOffer, json);
  }
}
