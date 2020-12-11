package org.xrpl.xrpl4j.model.transactions.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.AccountDelete;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

public class AccountDeleteJsonTests extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {
    AccountDelete accountDelete = AccountDelete.builder()
        .account(Address.of("rWYkbWkCeg8dP6rXALnjgZSjjLyih5NXm"))
        .fee(XrpCurrencyAmount.ofDrops(5000000))
        .sequence(UnsignedInteger.valueOf(2470665))
        .destination(Address.of("rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe"))
        .destinationTag(UnsignedInteger.valueOf(13))
        .build();

    String json = "{\n" +
        "    \"TransactionType\": \"AccountDelete\",\n" +
        "    \"Account\": \"rWYkbWkCeg8dP6rXALnjgZSjjLyih5NXm\",\n" +
        "    \"Destination\": \"rPT1Sjq2YGrBMTttX4GZHjKu9dyfzbpAYe\",\n" +
        "    \"DestinationTag\": 13,\n" +
        "    \"Fee\": \"5000000\",\n" +
        "    \"Sequence\": 2470665,\n" +
        "    \"Flags\": 2147483648\n" +
        "}";

    assertCanSerializeAndDeserialize(accountDelete, json);
  }
}
