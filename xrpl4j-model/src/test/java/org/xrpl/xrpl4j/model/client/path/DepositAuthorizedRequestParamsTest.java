package org.xrpl.xrpl4j.model.client.path;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;

/**
 * Unit tests for {@link DepositAuthorizedRequestParams}.
 */
public class DepositAuthorizedRequestParamsTest extends AbstractJsonTest {

  @Test
  public void testToFromJsonWithLedgerIndex() throws JSONException, JsonProcessingException {
    DepositAuthorizedRequestParams params = DepositAuthorizedRequestParams.builder()
      .sourceAccount(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .destinationAccount(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .ledgerIndex(LedgerIndex.VALIDATED)
      .build();

    assertThat(params.ledgerIndex().equals(LedgerIndex.VALIDATED));

    String json = "{\n" +
      "            \"source_account\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\"," +
      "            \"destination_account\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\"," +
      "            \"ledger_index\": \"validated\"" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);

    params = DepositAuthorizedRequestParams.builder()
      .sourceAccount(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .destinationAccount(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .ledgerIndex(LedgerIndex.CURRENT)
      .build();
    assertThat(params.ledgerIndex().equals(LedgerIndex.CURRENT));

    json = "{\n" +
      "            \"source_account\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\"," +
      "            \"destination_account\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\"," +
      "            \"ledger_index\": \"current\"" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testToFromJsonWithLedgerHash() throws JSONException, JsonProcessingException {
    DepositAuthorizedRequestParams params = DepositAuthorizedRequestParams.builder()
      .sourceAccount(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .destinationAccount(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .ledgerHash(Hash256.of("abcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcd"))
      .build();
    assertThat(params.ledgerIndex().equals(LedgerIndex.CURRENT));

    String json = "{\n" +
      "            \"source_account\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\"," +
      "            \"destination_account\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\"," +
      "            \"ledger_hash\": \"abcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcd\"," +
      "            \"ledger_index\": \"current\"" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testToFromJsonWithBothLedgerHashAndLedgerIndex() throws JSONException, JsonProcessingException {
    DepositAuthorizedRequestParams params = DepositAuthorizedRequestParams.builder()
      .sourceAccount(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .destinationAccount(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .ledgerHash(Hash256.of("abcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcd"))
      .ledgerIndex(LedgerIndex.CURRENT)
      .build();
    assertThat(params.ledgerIndex().equals(LedgerIndex.CURRENT));

    String json = "{\n" +
      "            \"source_account\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\"," +
      "            \"destination_account\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\"," +
      "            \"ledger_hash\": \"abcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcd\"," +
      "            \"ledger_index\": \"current\"" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);

    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      DepositAuthorizedRequestParams.builder()
        .sourceAccount(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
        .destinationAccount(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
        .ledgerHash(Hash256.of("abcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcd"))
        .ledgerIndex(LedgerIndex.VALIDATED)
        .build();
    });
  }

  @Test
  public void testDefaultValues() {
    DepositAuthorizedRequestParams params = DepositAuthorizedRequestParams.builder()
      .sourceAccount(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .destinationAccount(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
      .build();
    assertThat(params.sourceAccount().equals(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59")));
    assertThat(params.destinationAccount().equals(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59")));
    assertThat(params.ledgerIndex().equals(LedgerIndex.CURRENT));
    assertThat(params.ledgerHash()).isEmpty();
  }

  @Test
  public void testParamsWithBothHashAndIndex() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      DepositAuthorizedRequestParams.builder()
        .sourceAccount(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
        .destinationAccount(Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59"))
        .ledgerHash(Hash256.of("abcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcd"))
        .ledgerIndex(LedgerIndex.VALIDATED)
        .build();
    });
  }
}