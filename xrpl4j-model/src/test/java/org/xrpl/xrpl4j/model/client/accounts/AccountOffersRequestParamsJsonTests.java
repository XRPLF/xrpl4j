package org.xrpl.xrpl4j.model.client.accounts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Marker;

public class AccountOffersRequestParamsJsonTests extends AbstractJsonTest {

  public static final Hash256 HASH_256 = Hash256.of("6B1011EF3BC3ED619B15979EF75C1C60D9181F3DDE641AD3019318D3900CEE2E");

  @Test
  @Deprecated
  public void oldLedgerIndexStillWorks() throws JsonProcessingException, JSONException {
    AccountOffersRequestParams params = AccountOffersRequestParams.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .ledgerIndex(LedgerIndex.VALIDATED)
      .limit(UnsignedInteger.valueOf(100L))
      .marker(Marker.of("marker"))
      .build();

    String json = "{\n" +
      "        \"account\": \"rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\",\n" +
      "        \"ledger_index\": \"validated\",\n" +
      "        \"limit\": 100,\n" +
      "        \"marker\": \"marker\",\n" +
      "        \"strict\": true\n" +
      "    }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  @Deprecated
  public void oldNumericalLedgerIndexStillWorks() throws JsonProcessingException, JSONException {
    AccountOffersRequestParams params = AccountOffersRequestParams.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.ONE))
      .limit(UnsignedInteger.valueOf(100L))
      .marker(Marker.of("marker"))
      .build();

    String json = "{\n" +
      "        \"account\": \"rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\",\n" +
      "        \"ledger_index\": 1,\n" +
      "        \"limit\": 100,\n" +
      "        \"marker\": \"marker\",\n" +
      "        \"strict\": true\n" +
      "    }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  @Deprecated
  public void oldNLedgerHashStillWorks() throws JsonProcessingException, JSONException {
    AccountOffersRequestParams params = AccountOffersRequestParams.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .ledgerHash(HASH_256)
      .limit(UnsignedInteger.valueOf(100L))
      .marker(Marker.of("marker"))
      .build();

    String json = "{\n" +
      "        \"account\": \"rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\",\n" +
      "        \"ledger_hash\": \"" + HASH_256 + "\",\n" +
      "        \"limit\": 100,\n" +
      "        \"marker\": \"marker\",\n" +
      "        \"strict\": true\n" +
      "    }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testWithLedgerIndexShortcut() throws JsonProcessingException, JSONException {
    AccountOffersRequestParams params = AccountOffersRequestParams.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .ledgerSpecifier(LedgerSpecifier.VALIDATED)
      .limit(UnsignedInteger.valueOf(100L))
      .marker(Marker.of("marker"))
      .build();

    String json = "{\n" +
      "        \"account\": \"rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\",\n" +
      "        \"ledger_index\": \"validated\",\n" +
      "        \"limit\": 100,\n" +
      "        \"marker\": \"marker\",\n" +
      "        \"strict\": true\n" +
      "    }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testWithLedgerHash() throws JsonProcessingException, JSONException {
    AccountOffersRequestParams params = AccountOffersRequestParams.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .ledgerSpecifier(LedgerSpecifier.of(HASH_256))
      .limit(UnsignedInteger.valueOf(100L))
      .marker(Marker.of("marker"))
      .build();

    String json = "{\n" +
      "        \"account\": \"rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\",\n" +
      "        \"ledger_hash\": \"" + HASH_256 + "\",\n" +
      "        \"limit\": 100,\n" +
      "        \"marker\": \"marker\",\n" +
      "        \"strict\": true\n" +
      "    }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testWithLedgerIndex() throws JsonProcessingException, JSONException {
    AccountOffersRequestParams params = AccountOffersRequestParams.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .ledgerSpecifier(LedgerSpecifier.of(LedgerIndex.of(UnsignedInteger.ONE)))
      .limit(UnsignedInteger.valueOf(100L))
      .marker(Marker.of("marker"))
      .build();

    String json = "{\n" +
      "        \"account\": \"rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\",\n" +
      "        \"ledger_index\": 1,\n" +
      "        \"limit\": 100,\n" +
      "        \"marker\": \"marker\",\n" +
      "        \"strict\": true\n" +
      "    }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testMinimal() throws JsonProcessingException, JSONException {
    AccountOffersRequestParams params = AccountOffersRequestParams.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .build();

    String json = "{\n" +
      "        \"account\": \"rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\",\n" +
      "        \"ledger_index\": \"current\",\n" +
      "        \"strict\": true\n" +
      "    }";

    assertCanSerializeAndDeserialize(params, json);
  }
}
