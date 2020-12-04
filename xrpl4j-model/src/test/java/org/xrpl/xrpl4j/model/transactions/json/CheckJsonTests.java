package org.xrpl.xrpl4j.model.transactions.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.CheckCancel;
import org.xrpl.xrpl4j.model.transactions.CheckCash;
import org.xrpl.xrpl4j.model.transactions.CheckCreate;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.json.JSONException;
import org.junit.Test;

public class CheckJsonTests extends AbstractJsonTest {

  @Test
  public void testCheckCancelJson() throws JsonProcessingException, JSONException {
    CheckCancel checkCancel = CheckCancel.builder()
        .account(Address.of("rUn84CUYbNjRoTQ6mSW7BVJPSVJNLb1QLo"))
        .checkId(Hash256.of("49647F0D748DC3FE26BDACBC57F251AADEFFF391403EC9BF87C97F67E9977FB0"))
        .sequence(UnsignedInteger.valueOf(12))
        .fee(XrpCurrencyAmount.ofDrops(12))
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
        .fee(XrpCurrencyAmount.ofDrops(12))
        .deliverMin(XrpCurrencyAmount.ofDrops(100))
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
        .fee(XrpCurrencyAmount.ofDrops(12))
        .amount(XrpCurrencyAmount.ofDrops(100))
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

  @Test
  public void testCheckCreateJson() throws JsonProcessingException, JSONException {
    CheckCreate checkCreate = CheckCreate.builder()
        .account(Address.of("rUn84CUYbNjRoTQ6mSW7BVJPSVJNLb1QLo"))
        .sequence(UnsignedInteger.ONE)
        .fee(XrpCurrencyAmount.ofDrops(12))
        .destination(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
        .destinationTag(UnsignedInteger.ONE)
        .sendMax(XrpCurrencyAmount.ofDrops(100000000))
        .expiration(UnsignedInteger.valueOf(570113521))
        .invoiceId(Hash256.of("6F1DFD1D0FE8A32E40E1F2C05CF1C15545BAB56B617F9C6C2D63A6B704BEF59B"))
        .build();

    String json = "{\n" +
        "  \"TransactionType\": \"CheckCreate\",\n" +
        "  \"Account\": \"rUn84CUYbNjRoTQ6mSW7BVJPSVJNLb1QLo\",\n" +
        "  \"Destination\": \"rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy\",\n" +
        "  \"SendMax\": \"100000000\",\n" +
        "  \"Expiration\": 570113521,\n" +
        "  \"InvoiceID\": \"6F1DFD1D0FE8A32E40E1F2C05CF1C15545BAB56B617F9C6C2D63A6B704BEF59B\",\n" +
        "  \"DestinationTag\": 1,\n" +
        "  \"Sequence\": 1,\n" +
        "  \"Flags\": 2147483648,\n" +
        "  \"Fee\": \"12\"\n" +
        "}";

    assertCanSerializeAndDeserialize(checkCreate, json);
  }

  @Test
  public void testMinimalCheckCreateJson() throws JsonProcessingException, JSONException {
    CheckCreate checkCreate = CheckCreate.builder()
        .account(Address.of("rUn84CUYbNjRoTQ6mSW7BVJPSVJNLb1QLo"))
        .sequence(UnsignedInteger.ONE)
        .fee(XrpCurrencyAmount.ofDrops(12))
        .destination(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
        .sendMax(XrpCurrencyAmount.ofDrops(100000000))
        .build();

    String json = "{\n" +
        "  \"TransactionType\": \"CheckCreate\",\n" +
        "  \"Account\": \"rUn84CUYbNjRoTQ6mSW7BVJPSVJNLb1QLo\",\n" +
        "  \"Destination\": \"rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy\",\n" +
        "  \"SendMax\": \"100000000\",\n" +
        "  \"Sequence\": 1,\n" +
        "  \"Flags\": 2147483648,\n" +
        "  \"Fee\": \"12\"\n" +
        "}";

    assertCanSerializeAndDeserialize(checkCreate, json);
  }
}
