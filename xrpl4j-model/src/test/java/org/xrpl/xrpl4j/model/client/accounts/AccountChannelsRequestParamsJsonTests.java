package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Marker;

public class AccountChannelsRequestParamsJsonTests extends AbstractJsonTest {

  @Test
  public void testWithStringMarker() throws JsonProcessingException, JSONException {
    AccountChannelsRequestParams params = AccountChannelsRequestParams.builder()
        .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .destinationAccount(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
        .ledgerIndex(LedgerIndex.VALIDATED)
        .ledgerHash(Hash256.of("6B1011EF3BC3ED619B15979EF75C1C60D9181F3DDE641AD3019318D3900CEE2E"))
        .limit(UnsignedInteger.valueOf(20))
        .marker(Marker.of("marker1"))
        .build();

    String json = "{\n" +
        "        \"account\": \"rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\",\n" +
        "        \"destination_account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
        "        \"ledger_hash\": \"6B1011EF3BC3ED619B15979EF75C1C60D9181F3DDE641AD3019318D3900CEE2E\",\n" +
        "        \"limit\": 20,\n" +
        "        \"marker\": \"marker1\",\n" +
        "        \"ledger_index\": \"validated\"\n" +
        "    }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testWithJsonMarker() throws JsonProcessingException, JSONException {
    AccountChannelsRequestParams params = AccountChannelsRequestParams.builder()
        .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
        .destinationAccount(Address.of("rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn"))
        .ledgerIndex(LedgerIndex.VALIDATED)
        .ledgerHash(Hash256.of("6B1011EF3BC3ED619B15979EF75C1C60D9181F3DDE641AD3019318D3900CEE2E"))
        .limit(UnsignedInteger.valueOf(20))
        .marker(Marker.of("{\"marker\":\"1\"}"))
        .build();

    String json = "{\n" +
        "        \"account\": \"rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\",\n" +
        "        \"destination_account\": \"rf1BiGeXwwQoi8Z2ueFYTEXSwuJYfV2Jpn\",\n" +
        "        \"ledger_hash\": \"6B1011EF3BC3ED619B15979EF75C1C60D9181F3DDE641AD3019318D3900CEE2E\",\n" +
        "        \"limit\": 20,\n" +
        "        \"marker\": {\"marker\": \"1\"},\n" +
        "        \"ledger_index\": \"validated\"\n" +
        "    }";

    assertCanSerializeAndDeserialize(params, json);
  }
}
