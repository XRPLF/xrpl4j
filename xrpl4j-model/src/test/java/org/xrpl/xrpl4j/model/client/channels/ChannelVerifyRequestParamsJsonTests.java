package org.xrpl.xrpl4j.model.client.channels;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class ChannelVerifyRequestParamsJsonTests extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {

    ChannelVerifyRequestParams params = ChannelVerifyRequestParams.builder()
        .channelId(Hash256.of("5DB01B7FFED6B67E6B0414DED11E051D2EE2B7619CE0EAA6286D67A3A4D5BDB3"))
        .signature("304402204EF0AFB78AC23ED1C472E74F4299C0C21F1B21D07EFC0A3838A420F76D783A40022015" +
            "4FB11B6F54320666E4C36CA7F686C16A3A0456800BBC43746F34AF50290064")
        .publicKey("aB44YfzW24VDEJQ2UuLPV2PvqcPCSoLnL7y5M1EzhdW4LnK5xMS3")
        .amount(XrpCurrencyAmount.ofDrops(1000000))
        .build();

    String json = "{\n" +
        "        \"channel_id\": \"5DB01B7FFED6B67E6B0414DED11E051D2EE2B7619CE0EAA6286D67A3A4D5BDB3\",\n" +
        "        \"signature\": \"304402204EF0AFB78AC23ED1C472E74F4299C0C21F1B21D07EFC0A3838A420F76D783A40022015" +
        "4FB11B6F54320666E4C36CA7F686C16A3A0456800BBC43746F34AF50290064\",\n" +
        "        \"public_key\": \"aB44YfzW24VDEJQ2UuLPV2PvqcPCSoLnL7y5M1EzhdW4LnK5xMS3\",\n" +
        "        \"amount\": \"1000000\"\n" +
        "    }";

    assertCanSerializeAndDeserialize(params, json);
  }
}
