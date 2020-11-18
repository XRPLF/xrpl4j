package com.ripple.xrpl4j.model.transactions.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.model.transactions.Address;
import com.ripple.xrpl4j.model.transactions.PaymentChannelCreate;
import com.ripple.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.json.JSONException;
import org.junit.Test;

public class PaymentChannelJsonTests extends AbstractJsonTest {

  @Test
  public void testPaymentChannelCreateJson() throws JsonProcessingException, JSONException {
    PaymentChannelCreate create = PaymentChannelCreate.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .sourceTag(UnsignedInteger.valueOf(11747))
      .fee(XrpCurrencyAmount.of("10"))
      .sequence(UnsignedInteger.ONE)
      .amount(XrpCurrencyAmount.of("10000"))
      .destination(Address.of("rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW"))
      .destinationTag(UnsignedInteger.valueOf(23480))
      .settleDelay(UnsignedInteger.valueOf(86400))
      .publicKey("32D2471DB72B27E3310F355BB33E339BF26F8392D5A93D3BC0FC3B566612DA0F0A")
      .cancelAfter(UnsignedInteger.valueOf(533171558))
      .build();

    String json = "{\n" +
      "    \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"Fee\": \"10\",\n" +
      "    \"Sequence\": 1,\n" +
      "    \"Flags\": 2147483648,\n" +
      "    \"TransactionType\": \"PaymentChannelCreate\",\n" +
      "    \"Amount\": \"10000\",\n" +
      "    \"Destination\": \"rsA2LpzuawewSBQXkiju3YQTMzW13pAAdW\",\n" +
      "    \"SettleDelay\": 86400,\n" +
      "    \"PublicKey\": \"32D2471DB72B27E3310F355BB33E339BF26F8392D5A93D3BC0FC3B566612DA0F0A\",\n" +
      "    \"CancelAfter\": 533171558,\n" +
      "    \"DestinationTag\": 23480,\n" +
      "    \"SourceTag\": 11747\n" +
      "}";

    assertCanSerializeAndDeserialize(create, json);
  }
}
