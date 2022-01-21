package org.xrpl.xrpl4j.model.client.nft;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.junit.jupiter.api.Test;

public class NftBuyOffersRequestParamsTest extends AbstractJsonTest {

  @Test
  public void test() throws JsonProcessingException, JSONException {
    NftBuyOffersRequestParams params = NftBuyOffersRequestParams.builder()
      .tokenId("000100001E962F495F07A990F4ED55ACCFEEF365DBAA76B6A048C0A200000007")
      .build();

    String json = "{\n" +
      "        \"TokenID\": \"000100001E962F495F07A990F4ED55ACCFEEF365DBAA76B6A048C0A200000007\"\n" +
      "    }";

    assertCanSerializeAndDeserialize(params, json);
  }
}
