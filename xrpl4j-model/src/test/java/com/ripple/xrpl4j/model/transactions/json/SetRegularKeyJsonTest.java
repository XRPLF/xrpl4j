package com.ripple.xrpl4j.model.transactions.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.model.transactions.Address;
import com.ripple.xrpl4j.model.transactions.SetRegularKey;
import com.ripple.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.json.JSONException;
import org.junit.Test;

public class SetRegularKeyJsonTest extends AbstractJsonTest {

  @Test
  public void testSetRegularKeyJson() throws JsonProcessingException, JSONException {
    SetRegularKey setRegularKey = SetRegularKey.builder()
        .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
        .fee(XrpCurrencyAmount.ofDrops(12))
        .sequence(UnsignedInteger.ONE)
        .regularKey(Address.of("rAR8rR8sUkBoCZFawhkWzY4Y5YoyuznwD"))
        .build();

    String json = "{\n" +
        "    \"Flags\": 2147483648,\n" +
        "    \"Sequence\": 1,\n" +
        "    \"TransactionType\": \"SetRegularKey\",\n" +
        "    \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
        "    \"Fee\": \"12\",\n" +
        "    \"RegularKey\": \"rAR8rR8sUkBoCZFawhkWzY4Y5YoyuznwD\"\n" +
        "}";

    assertCanSerializeAndDeserialize(setRegularKey, json);
  }
}
