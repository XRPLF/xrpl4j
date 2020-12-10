package org.xrpl.xrpl4j.model.client.channels;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.json.JSONException;
import org.junit.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.accounts.AccountObjectsResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.flags.Flags;
import org.xrpl.xrpl4j.model.ledger.RippleStateObject;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;

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
