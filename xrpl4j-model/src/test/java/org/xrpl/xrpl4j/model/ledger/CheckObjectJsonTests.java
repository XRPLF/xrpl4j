package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class CheckObjectJsonTests extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {
    CheckObject object = CheckObject.builder()
      .account(Address.of("rUn84CUYbNjRoTQ6mSW7BVJPSVJNLb1QLo"))
      .destination(Address.of("rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy"))
      .destinationNode("0000000000000000")
      .destinationTag(UnsignedInteger.ONE)
      .expiration(UnsignedInteger.valueOf(570113521))
      .invoiceId(Hash256.of("46060241FABCF692D4D934BA2A6C4427CD4279083E38C77CBE642243E43BE291"))
      .ownerNode("0000000000000000")
      .previousTxnId(Hash256.of("5463C6E08862A1FAE5EDAC12D70ADB16546A1F674930521295BC082494B62924"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(6))
      .sendMax(XrpCurrencyAmount.ofDrops(100000000))
      .sequence(UnsignedInteger.valueOf(2))
      .index(Hash256.of("49647F0D748DC3FE26BDACBC57F251AADEFFF391403EC9BF87C97F67E9977FB0"))
      .build();

    String json = "{\n" +
      "  \"Account\": \"rUn84CUYbNjRoTQ6mSW7BVJPSVJNLb1QLo\",\n" +
      "  \"Destination\": \"rfkE1aSy9G8Upk4JssnwBxhEv5p4mn2KTy\",\n" +
      "  \"DestinationNode\": \"0000000000000000\",\n" +
      "  \"DestinationTag\": 1,\n" +
      "  \"Expiration\": 570113521,\n" +
      "  \"Flags\": 0,\n" +
      "  \"InvoiceID\": \"46060241FABCF692D4D934BA2A6C4427CD4279083E38C77CBE642243E43BE291\",\n" +
      "  \"LedgerEntryType\": \"Check\",\n" +
      "  \"OwnerNode\": \"0000000000000000\",\n" +
      "  \"PreviousTxnID\": \"5463C6E08862A1FAE5EDAC12D70ADB16546A1F674930521295BC082494B62924\",\n" +
      "  \"PreviousTxnLgrSeq\": 6,\n" +
      "  \"SendMax\": \"100000000\",\n" +
      "  \"Sequence\": 2,\n" +
      "  \"index\": \"49647F0D748DC3FE26BDACBC57F251AADEFFF391403EC9BF87C97F67E9977FB0\"\n" +
      "}";

    assertCanSerializeAndDeserialize(object, json);
  }
}
