package com.ripple.xrpl4j.model.transactions.json;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.model.transactions.AccountDelete;
import com.ripple.xrpl4j.model.transactions.Address;
import com.ripple.xrpl4j.model.transactions.Flags;
import com.ripple.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

public class AccountDeleteJsonTests extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {
    AccountDelete accountDelete = AccountDelete.builder()
      .account(Address.of("rWYkbWkCeg8dP6rXALnjgZSjjLyih5NXm"))
      .fee(XrpCurrencyAmount.of("5000000"))
      .sequence(UnsignedInteger.valueOf(2470665))
      .flags(Flags.of(2147483648L))
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
