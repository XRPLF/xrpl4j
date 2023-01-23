package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Marker;

public class AccountNftsRequestParamsJsonTests extends AbstractJsonTest {

  @Test
  public void test() throws JsonProcessingException, JSONException {
    AccountNftsRequestParams params = AccountNftsRequestParams.builder()
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B"))
      .build();

    String json = "{\n" +
      "        \"account\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B\"\n" +
      "    }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testWithMarker() throws JsonProcessingException, JSONException {
    AccountNftsRequestParams params = AccountNftsRequestParams.builder()
      .account(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B"))
      .marker(Marker.of("marker1"))
      .build();

    String json = "{\n" +
      "        \"account\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B\",\n" +
      "        \"marker\": \"marker1\"\n" +
      "    }";

    assertCanSerializeAndDeserialize(params, json);
  }
}
