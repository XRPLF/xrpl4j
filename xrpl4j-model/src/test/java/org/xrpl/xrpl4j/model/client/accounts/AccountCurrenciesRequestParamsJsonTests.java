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

public class AccountCurrenciesRequestParamsJsonTests extends AbstractJsonTest {

  @Test
  @Deprecated
  public void oldLedgerIndexStillWorks() throws JsonProcessingException, JSONException {
    AccountCurrenciesRequestParams params = AccountCurrenciesRequestParams.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .ledgerIndex(LedgerIndex.VALIDATED)
      .build();

    String json = "{\n" +
      "        \"account\": \"rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\",\n" +
      "        \"ledger_index\": \"validated\",\n" +
      "        \"strict\": true\n" +
      "    }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  @Deprecated
  public void oldNumericalLedgerIndexStillWorks() throws JsonProcessingException, JSONException {
    AccountCurrenciesRequestParams params = AccountCurrenciesRequestParams.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.ONE))
      .build();

    String json = "{\n" +
      "        \"account\": \"rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\",\n" +
      "        \"ledger_index\": 1,\n" +
      "        \"strict\": true\n" +
      "    }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  @Deprecated
  public void oldLedgerHashStillWorks() throws JsonProcessingException, JSONException {
    AccountCurrenciesRequestParams params = AccountCurrenciesRequestParams.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .ledgerHash(Hash256.of("6B1011EF3BC3ED619B15979EF75C1C60D9181F3DDE641AD3019318D3900CEE2E"))
      .build();

    String json = "{\n" +
      "        \"account\": \"rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\",\n" +
      "        \"ledger_hash\": \"6B1011EF3BC3ED619B15979EF75C1C60D9181F3DDE641AD3019318D3900CEE2E\",\n" +
      "        \"strict\": true\n" +
      "    }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testWithLedgerIndexShortcut() throws JsonProcessingException, JSONException {
    AccountCurrenciesRequestParams params = AccountCurrenciesRequestParams.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .ledgerSpecifier(LedgerSpecifier.VALIDATED)
      .build();

    String json = "{\n" +
      "        \"account\": \"rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\",\n" +
      "        \"ledger_index\": \"validated\",\n" +
      "        \"strict\": true\n" +
      "    }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  void testWithLedgerHash() throws JsonProcessingException, JSONException {
    AccountCurrenciesRequestParams params = AccountCurrenciesRequestParams.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .ledgerSpecifier(LedgerSpecifier.of(
        Hash256.of("6B1011EF3BC3ED619B15979EF75C1C60D9181F3DDE641AD3019318D3900CEE2E")
      ))
      .build();

    String json = "{\n" +
      "        \"account\": \"rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\",\n" +
      "        \"ledger_hash\": \"6B1011EF3BC3ED619B15979EF75C1C60D9181F3DDE641AD3019318D3900CEE2E\",\n" +
      "        \"strict\": true\n" +
      "    }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  void testWithLedgerIndex() throws JsonProcessingException, JSONException {
    AccountCurrenciesRequestParams params = AccountCurrenciesRequestParams.builder()
      .account(Address.of("rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH"))
      .ledgerSpecifier(LedgerSpecifier.of(LedgerIndex.of(UnsignedInteger.ONE)))
      .build();

    String json = "{\n" +
      "        \"account\": \"rN7n7otQDd6FczFgLdSqtcsAUxDkw6fzRH\",\n" +
      "        \"ledger_index\": 1,\n" +
      "        \"strict\": true\n" +
      "    }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testMinimal() throws JsonProcessingException, JSONException {
    AccountCurrenciesRequestParams params = AccountCurrenciesRequestParams.builder()
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
