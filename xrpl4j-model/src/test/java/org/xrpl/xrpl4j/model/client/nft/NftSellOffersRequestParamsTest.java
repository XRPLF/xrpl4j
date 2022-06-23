package org.xrpl.xrpl4j.model.client.nft;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.NfTokenId;

public class NftSellOffersRequestParamsTest extends AbstractJsonTest {

  @Test
  public void test() throws JsonProcessingException, JSONException {
    NftSellOffersRequestParams params = NftSellOffersRequestParams.builder()
      .nfTokenId(NfTokenId.of("000100001E962F495F07A990F4ED55ACCFEEF365DBAA76B6A048C0A200000007"))
      .build();

    String json = "{\n" +
      "        \"nft_id\": \"000100001E962F495F07A990F4ED55ACCFEEF365DBAA76B6A048C0A200000007\"\n" +
      "    }";

    assertCanSerializeAndDeserialize(params, json);
  }
}
