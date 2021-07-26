package org.xrpl.xrpl4j.model.ledger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;

public class DepositPreAuthObjectJsonTests extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {
    DepositPreAuthObject object = DepositPreAuthObject.builder()
        .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
        .authorize(Address.of("rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de"))
        .ownerNode("0000000000000000")
        .previousTransactionId(Hash256.of("3E8964D5A86B3CD6B9ECB33310D4E073D64C865A5B866200AD2B7E29F8326702"))
        .previousTransactionLedgerSequence(UnsignedInteger.valueOf(7))
        .index(Hash256.of("4A255038CC3ADCC1A9C91509279B59908251728D0DAADB248FFE297D0F7E068C"))
        .build();

    String json = "{\n" +
        "  \"LedgerEntryType\" : \"DepositPreauth\",\n" +
        "  \"Account\" : \"rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8\",\n" +
        "  \"Authorize\" : \"rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de\",\n" +
        "  \"Flags\" : 0,\n" +
        "  \"OwnerNode\" : \"0000000000000000\",\n" +
        "  \"PreviousTxnID\" : \"3E8964D5A86B3CD6B9ECB33310D4E073D64C865A5B866200AD2B7E29F8326702\",\n" +
        "  \"PreviousTxnLgrSeq\" : 7,\n" +
        "  \"index\" : \"4A255038CC3ADCC1A9C91509279B59908251728D0DAADB248FFE297D0F7E068C\"\n" +
        "}";

    assertCanSerializeAndDeserialize(object, json);
  }
}
