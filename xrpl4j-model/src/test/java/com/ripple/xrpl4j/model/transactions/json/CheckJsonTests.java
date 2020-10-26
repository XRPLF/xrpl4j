package com.ripple.xrpl4j.model.transactions.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.model.transactions.Address;
import com.ripple.xrpl4j.model.transactions.CheckCancel;
import com.ripple.xrpl4j.model.transactions.CheckCash;
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

  @Test
  public void testCheckCashJsonWithDeliverMin() throws JsonProcessingException, JSONException {
    CheckCash checkCash = CheckCash.builder()
      .account(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
      .checkId(Hash256.of("838766BA2B995C00744175F69A1B11E32C3DBC40E64801A4056FCBD657F57334"))
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.of("12"))
      .deliverMin(XrpCurrencyAmount.of("100"))
      .build();

    String json = "{\n" +
      "    \"Account\": \"rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy\",\n" +
      "    \"TransactionType\": \"CheckCash\",\n" +
      "    \"DeliverMin\": \"100\",\n" +
      "    \"CheckID\": \"838766BA2B995C00744175F69A1B11E32C3DBC40E64801A4056FCBD657F57334\",\n" +
      "    \"Sequence\": 1,\n" +
      "    \"Flags\": 2147483648,\n" +
      "    \"Fee\": \"12\"\n" +
      "}";

    assertCanSerializeAndDeserialize(checkCash, json);
  }

  @Test
  public void testCheckCashJsonWithAmount() throws JsonProcessingException, JSONException {
    CheckCash checkCash = CheckCash.builder()
      .account(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
      .checkId(Hash256.of("838766BA2B995C00744175F69A1B11E32C3DBC40E64801A4056FCBD657F57334"))
      .sequence(UnsignedInteger.ONE)
      .fee(XrpCurrencyAmount.of("12"))
      .amount(XrpCurrencyAmount.of("100"))
      .build();

    String json = "{\n" +
      "    \"Account\": \"rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy\",\n" +
      "    \"TransactionType\": \"CheckCash\",\n" +
      "    \"Amount\": \"100\",\n" +
      "    \"CheckID\": \"838766BA2B995C00744175F69A1B11E32C3DBC40E64801A4056FCBD657F57334\",\n" +
      "    \"Sequence\": 1,\n" +
      "    \"Flags\": 2147483648,\n" +
      "    \"Fee\": \"12\"\n" +
      "}";

    assertCanSerializeAndDeserialize(checkCash, json);
  }
}
