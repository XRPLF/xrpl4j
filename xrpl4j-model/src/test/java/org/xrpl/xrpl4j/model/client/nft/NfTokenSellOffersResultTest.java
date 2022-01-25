package org.xrpl.xrpl4j.model.client.nft;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.NfTokenId;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import java.util.ArrayList;
import java.util.List;

public class NfTokenSellOffersResultTest extends AbstractJsonTest {

  @Test
  public void test() throws JsonProcessingException, JSONException {

    NfTokenOfferObject obj = NfTokenOfferObject.builder()
      .amount(XrpCurrencyAmount.ofDrops(100000))
      .build();

    List<NfTokenOfferObject> list = new ArrayList<>();
    list.add(obj);

    NftSellOffersResult params = NftSellOffersResult.builder()
      .tokenId(NfTokenId.of("000100001E962F495F07A990F4ED55ACCFEEF365DBAA76B6A048C0A200000007"))
      .offers(list)
      .build();

    String json = "{\n" +
      "        \"TokenID\": \"000100001E962F495F07A990F4ED55ACCFEEF365DBAA76B6A048C0A200000007\",\n" +
      "        \"offers\": [{\"Amount\":\"100000\"}]" +
      "    }";

    assertCanSerializeAndDeserialize(params, json);
  }
}
