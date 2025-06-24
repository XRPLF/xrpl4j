package org.xrpl.xrpl4j.model.client.ledger.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.ledger.OfferLedgerEntryParams;
import org.xrpl.xrpl4j.model.transactions.Address;

public class OfferLedgerEntryParamsJsonTest extends AbstractJsonTest {

  @Test
  public void testOfferLedgerEntryParams() throws JSONException, JsonProcessingException {
    OfferLedgerEntryParams offerLedgerEntryParams = OfferLedgerEntryParams.builder()
      .account(Address.of("rK2vwKgQqXahHWUvi9VVTQsYe6gze5n1os"))
      .seq(UnsignedInteger.ONE)
      .build();

    String json = "{" +
      "  \"account\": \"rK2vwKgQqXahHWUvi9VVTQsYe6gze5n1os\"," +
      "  \"seq\": 1" +
      "}";

    assertCanSerializeAndDeserialize(offerLedgerEntryParams, json, OfferLedgerEntryParams.class);
  }
}