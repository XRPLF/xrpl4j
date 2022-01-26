package org.xrpl.xrpl4j.model.client.nft;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.ledger.NfTokenOfferObject;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.NfTokenId;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.ArrayList;
import java.util.List;

public class NftBuyOffersResultTest extends AbstractJsonTest {

  @Test
  public void test() throws JsonProcessingException, JSONException {

    NfTokenOfferObject obj = NfTokenOfferObject.builder()
      .amount(XrpCurrencyAmount.ofDrops(10000))
      .destination(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .owner(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
      .previousTransactionId(Hash256.of("E3FE6EA3D48F0C2B639448020EA4F03D4F4F8FFDB243A852A0F59177921B4879"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(14090896))
      .tokenId(NfTokenId.of("000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65"))
      .flags(Flags.NfTokenOfferFlags.BUY_TOKEN)
      .build();

    List<NfTokenOfferObject> list = new ArrayList<>();
    list.add(obj);

    NftBuyOffersResult params = NftBuyOffersResult.builder()
      .tokenId(NfTokenId.of("000100001E962F495F07A990F4ED55ACCFEEF365DBAA76B6A048C0A200000007"))
      .offers(list)
      .build();

    String offer = "{\n" +
      "    \"Flags\": 1,\n" +
      "    \"Amount\": \"10000\",\n" +
      "    \"Owner\": \"rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW\",\n" +
      "    \"TokenID\": \"000B013A95F14B0044F78A264E41713C64B5F89242540EE208C3098E00000D65\",\n" +
      "    \"Destination\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"PreviousTxnID\": \"E3FE6EA3D48F0C2B639448020EA4F03D4F4F8FFDB243A852A0F59177921B4879\",\n" +
      "    \"PreviousTxnLgrSeq\": 14090896,\n" +
      "    \"LedgerEntryType\": \"NfTokenOffer\"\n" +
      "}";

    String json = "{\n" +
      "        \"TokenID\": \"000100001E962F495F07A990F4ED55ACCFEEF365DBAA76B6A048C0A200000007\",\n" +
      "        \"offers\": [" + offer + "]\n" +
      "}";

    assertCanSerializeAndDeserialize(params, json);
  }
}
