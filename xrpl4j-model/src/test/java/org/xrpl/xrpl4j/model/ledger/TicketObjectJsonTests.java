package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;

class TicketObjectJsonTests extends AbstractJsonTest {

  @Test
  void testJson() throws JSONException, JsonProcessingException {
    TicketObject ticketObject = TicketObject.builder()
      .account(Address.of("rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de"))
      .ownerNode("0000000000000000")
      .previousTransactionId(Hash256.of("F19AD4577212D3BEACA0F75FE1BA1644F2E854D46E8D62E9C95D18E9708CBFB1"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(4))
      .ticketSequence(UnsignedInteger.valueOf(3))
      .build();

    String json = "{\n" +
      "  \"Account\" : \"rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de\",\n" +
      "  \"Flags\" : 0,\n" +
      "  \"LedgerEntryType\" : \"Ticket\",\n" +
      "  \"OwnerNode\" : \"0000000000000000\",\n" +
      "  \"PreviousTxnID\" : \"F19AD4577212D3BEACA0F75FE1BA1644F2E854D46E8D62E9C95D18E9708CBFB1\",\n" +
      "  \"PreviousTxnLgrSeq\" : 4,\n" +
      "  \"TicketSequence\" : 3\n" +
      "}";

    assertCanSerializeAndDeserialize(ticketObject, json);
  }
}
