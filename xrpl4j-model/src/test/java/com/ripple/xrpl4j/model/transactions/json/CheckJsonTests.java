package com.ripple.xrpl4j.model.transactions.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.model.transactions.Address;
import com.ripple.xrpl4j.model.transactions.CheckCancel;
import com.ripple.xrpl4j.model.transactions.Hash256;
import com.ripple.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.json.JSONException;
import org.junit.Test;

public class CheckJsonTests extends AbstractJsonTest {

  @Test
  public void testCheckCancelJson() throws JsonProcessingException, JSONException {
    CheckCancel checkCancel = CheckCancel.builder()
      .account(Address.of("rUn84CUYbNjRoTQ6mSW7BVJPSVJNLb1QLo"))
      .checkId(Hash256.of("49647F0D748DC3FE26BDACBC57F251AADEFFF391403EC9BF87C97F67E9977FB0"))
      .sequence(UnsignedInteger.valueOf(12))
      .fee(XrpCurrencyAmount.of("12"))
      .build();

    String json = "{\n" +
      "    \"Account\": \"rUn84CUYbNjRoTQ6mSW7BVJPSVJNLb1QLo\",\n" +
      "    \"TransactionType\": \"CheckCancel\",\n" +
      "    \"CheckID\": \"49647F0D748DC3FE26BDACBC57F251AADEFFF391403EC9BF87C97F67E9977FB0\",\n" +
      "    \"Sequence\": 12,\n" +
      "    \"Flags\": 2147483648,\n" +
      "    \"Fee\": \"12\"\n" +
      "}";

    assertCanSerializeAndDeserialize(checkCancel, json);
  }
}
