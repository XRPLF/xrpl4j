package org.xrpl.xrpl4j.model.transactions.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.TicketCreate;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

class TicketCreateJsonTest extends AbstractJsonTest {

  @Test
  void testJson() throws JSONException, JsonProcessingException {
    TicketCreate ticketCreate = TicketCreate.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.ofDrops(12))
      .sequence(UnsignedInteger.ONE)
      .ticketCount(UnsignedInteger.valueOf(200))
      .build();

    String json = "{\n" +
      "    \"TransactionType\": \"TicketCreate\",\n" +
      "    \"Account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"Fee\": \"12\",\n" +
      "    \"Sequence\": 1,\n" +
      "    \"Flags\": 2147483648,\n" +
      "    \"TicketCount\": 200\n" +
      "}";

    assertCanSerializeAndDeserialize(ticketCreate, json);
  }
}
