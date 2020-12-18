package org.xrpl.xrpl4j.model.client.channels;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;

public class ChannelVerifyResultJsonTests extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {
    ChannelVerifyResult result = ChannelVerifyResult.builder()
        .signatureVerified(true)
        .status("success")
        .build();

    String json = "{\n" +
        "        \"signature_verified\":true,\n" +
        "        \"status\":\"success\"\n" +
        "    }";

    assertCanSerializeAndDeserialize(result, json);
  }
}
