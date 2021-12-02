package org.xrpl.xrpl4j.model.client.path;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.accounts.AccountChannelsRequestParams;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.IssuedCurrencyAmount;
import org.xrpl.xrpl4j.model.transactions.Marker;

public class RipplePathFindRequestParamsJsonTests extends AbstractJsonTest {

  @Test
  @Deprecated
  public void oldLedgerHashStillWorks() throws JsonProcessingException, JSONException {
    RipplePathFindRequestParams params = RipplePathFindRequestParams.builder()
      .ledgerHash(Hash256.of("6B1011EF3BC3ED619B15979EF75C1C60D9181F3DDE641AD3019318D3900CEE2E"))
      .destinationAccount(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .destinationAmount(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B"))
        .value("0.001")
        .build())
      .sourceAccount(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .addSourceCurrencies(
        PathCurrency.of("XRP"),
        PathCurrency.of("USD")
      )
      .build();

    String json = "{\n" +
      "            \"ledger_hash\": \"6B1011EF3BC3ED619B15979EF75C1C60D9181F3DDE641AD3019318D3900CEE2E\",\n" +
      "            \"destination_account\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\",\n" +
      "            \"destination_amount\": {\n" +
      "                \"currency\": \"USD\",\n" +
      "                \"issuer\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B\",\n" +
      "                \"value\": \"0.001\"\n" +
      "            },\n" +
      "            \"source_account\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\",\n" +
      "            \"source_currencies\": [\n" +
      "                {\n" +
      "                    \"currency\": \"XRP\"\n" +
      "                },\n" +
      "                {\n" +
      "                    \"currency\": \"USD\"\n" +
      "                }\n" +
      "            ]\n" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  @Deprecated
  public void oldLedgerIndexStillWorks() throws JsonProcessingException, JSONException {
    RipplePathFindRequestParams params = RipplePathFindRequestParams.builder()
      .ledgerIndex(LedgerIndex.VALIDATED)
      .destinationAccount(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .destinationAmount(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B"))
        .value("0.001")
        .build())
      .sourceAccount(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .addSourceCurrencies(
        PathCurrency.of("XRP"),
        PathCurrency.of("USD")
      )
      .build();

    String json = "{\n" +
      "            \"ledger_index\": \"validated\",\n" +
      "            \"destination_account\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\",\n" +
      "            \"destination_amount\": {\n" +
      "                \"currency\": \"USD\",\n" +
      "                \"issuer\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B\",\n" +
      "                \"value\": \"0.001\"\n" +
      "            },\n" +
      "            \"source_account\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\",\n" +
      "            \"source_currencies\": [\n" +
      "                {\n" +
      "                    \"currency\": \"XRP\"\n" +
      "                },\n" +
      "                {\n" +
      "                    \"currency\": \"USD\"\n" +
      "                }\n" +
      "            ]\n" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  @Deprecated
  public void oldNumericalLedgerIndexStillWorks() throws JsonProcessingException, JSONException {
    RipplePathFindRequestParams params = RipplePathFindRequestParams.builder()
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.ONE))
      .destinationAccount(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .destinationAmount(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B"))
        .value("0.001")
        .build())
      .sourceAccount(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .addSourceCurrencies(
        PathCurrency.of("XRP"),
        PathCurrency.of("USD")
      )
      .build();

    String json = "{\n" +
      "            \"ledger_index\": 1,\n" +
      "            \"destination_account\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\",\n" +
      "            \"destination_amount\": {\n" +
      "                \"currency\": \"USD\",\n" +
      "                \"issuer\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B\",\n" +
      "                \"value\": \"0.001\"\n" +
      "            },\n" +
      "            \"source_account\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\",\n" +
      "            \"source_currencies\": [\n" +
      "                {\n" +
      "                    \"currency\": \"XRP\"\n" +
      "                },\n" +
      "                {\n" +
      "                    \"currency\": \"USD\"\n" +
      "                }\n" +
      "            ]\n" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testJson() throws JsonProcessingException, JSONException {
    RipplePathFindRequestParams params = RipplePathFindRequestParams.builder()
      .destinationAccount(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .destinationAmount(IssuedCurrencyAmount.builder()
        .currency("USD")
        .issuer(Address.of("rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B"))
        .value("0.001")
        .build())
      .sourceAccount(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .addSourceCurrencies(
        PathCurrency.of("XRP"),
        PathCurrency.of("USD")
      )
      .build();

    String json = "{\n" +
      "            \"ledger_index\": \"current\",\n" +
      "            \"destination_account\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\",\n" +
      "            \"destination_amount\": {\n" +
      "                \"currency\": \"USD\",\n" +
      "                \"issuer\": \"rvYAfWj5gh67oV6fW32ZzP3Aw4Eubs59B\",\n" +
      "                \"value\": \"0.001\"\n" +
      "            },\n" +
      "            \"source_account\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\",\n" +
      "            \"source_currencies\": [\n" +
      "                {\n" +
      "                    \"currency\": \"XRP\"\n" +
      "                },\n" +
      "                {\n" +
      "                    \"currency\": \"USD\"\n" +
      "                }\n" +
      "            ]\n" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);
  }
}
