package com.ripple.xrpl4j.model.transactions.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.ripple.xrpl4j.model.transactions.AccountSet;
import com.ripple.xrpl4j.model.transactions.AccountSet.AccountSetFlag;
import com.ripple.xrpl4j.model.transactions.Address;
import com.ripple.xrpl4j.model.transactions.XrpCurrencyAmount;
import org.json.JSONException;
import org.junit.Test;

public class AccountSetJsonTests extends AbstractJsonTest {

  @Test
  public void fullyPopulatedAccountSet() throws JSONException, JsonProcessingException {
    AccountSet accountSet = AccountSet.builder()
      .account(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
      .fee(XrpCurrencyAmount.of("12"))
      .sequence(UnsignedInteger.valueOf(5))
      .domain("6578616D706C652E636F6D")
      .setFlag(AccountSetFlag.ACCOUNT_TXN_ID)
      .messageKey("03AB40A0490F9B7ED8DF29D246BF2D6269820A0EE7742ACDD457BEA7C7D0931EDB")
      .transferRate(UnsignedInteger.valueOf(1000000001))
      .tickSize(UnsignedInteger.valueOf(15))
      .clearFlag(AccountSetFlag.DEFAULT_RIPPLE)
      .emailHash("f9879d71855b5ff21e4963273a886bfc")
      .build();

    String json = "{\n" +
      "    \"TransactionType\":\"AccountSet\",\n" +
      "    \"Account\":\"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
      "    \"Fee\":\"12\",\n" +
      "    \"Sequence\":5,\n" +
      "    \"Flags\":2147483648,\n" +
      "    \"Domain\":\"6578616D706C652E636F6D\",\n" +
      "    \"SetFlag\":5,\n" +
      "    \"MessageKey\":\"03AB40A0490F9B7ED8DF29D246BF2D6269820A0EE7742ACDD457BEA7C7D0931EDB\",\n" +
      "    \"TransferRate\":1000000001,\n" +
      "    \"TickSize\":15,\n" +
      "    \"ClearFlag\":8,\n" +
      "    \"EmailHash\":\"f9879d71855b5ff21e4963273a886bfc\"\n" +
      "}";

    assertCanSerializeAndDeserialize(accountSet, json);
  }
}
