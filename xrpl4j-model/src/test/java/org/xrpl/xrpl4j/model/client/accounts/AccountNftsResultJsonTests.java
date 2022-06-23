package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.NfTokenId;

import java.util.ArrayList;
import java.util.List;

public class AccountNftsResultJsonTests extends AbstractJsonTest {

  @Test
  public void testMinimalJson() throws JsonProcessingException, JSONException {

    NfTokenObject obj = NfTokenObject.builder()
      .nfTokenId(NfTokenId.of("000100001E962F495F07A990F4ED55ACCFEEF365DBAA76B6A048C0A200000007"))
      .build();

    List<NfTokenObject> list = new ArrayList<>();
    list.add(obj);

    AccountNftsResult result = AccountNftsResult.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .status("success")
      .validated(true)
      .accountNfts(list)
      .build();

    String json = "{\n" +
      "        \"account\": \"rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\",\n" +
      "        \"account_nfts\": [{\n" +
      "            \"NFTokenID\": \"000100001E962F495F07A990F4ED55ACCFEEF365DBAA76B6A048C0A200000007\"\n" +
      "        }],\n" +
      "        \"status\": \"success\",\n" +
      "        \"validated\": true\n" +
      "    }";

    assertCanSerializeAndDeserialize(result, json);
  }
}
