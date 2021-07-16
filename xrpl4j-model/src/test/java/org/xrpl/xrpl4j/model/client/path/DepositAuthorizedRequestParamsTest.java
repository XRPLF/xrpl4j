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

  private static final Address SOURCE_ACCOUNT = Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk58");
  private static final Address DESTINATION_ACCOUNT = Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59");
  public static final Hash256 LEDGER_HASH = Hash256
    .of("abcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcd");

  @Test
  public void testToFromJsonWithLedgerIndexValidated() throws JSONException, JsonProcessingException {
    DepositAuthorizedRequestParams params = DepositAuthorizedRequestParams.builder()
      .sourceAccount(SOURCE_ACCOUNT)
      .destinationAccount(DESTINATION_ACCOUNT)
      .ledgerIndex(LedgerIndex.VALIDATED)
      .build();

    assertThat(params.ledgerIndex().equals(LedgerIndex.VALIDATED));

    String json = "{\n" +
      "            \"source_account\": \"" + SOURCE_ACCOUNT.value() + "\"," +
      "            \"destination_account\": \"" + DESTINATION_ACCOUNT.value() + "\"," +
      "            \"ledger_index\": \"validated\"" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testToFromJsonWithLedgerIndexCurrent() throws JSONException, JsonProcessingException {
    DepositAuthorizedRequestParams params = DepositAuthorizedRequestParams.builder()
      .sourceAccount(SOURCE_ACCOUNT)
      .destinationAccount(DESTINATION_ACCOUNT)
      .ledgerIndex(LedgerIndex.CURRENT)
      .build();
    assertThat(params.ledgerIndex().equals(LedgerIndex.CURRENT));

    String json = "{\n" +
      "            \"source_account\": \"" + SOURCE_ACCOUNT.value() + "\"," +
      "            \"destination_account\": \"" + DESTINATION_ACCOUNT.value() + "\"," +
      "            \"ledger_index\": \"current\"" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testToFromJsonWithLedgerHash() throws JSONException, JsonProcessingException {
    DepositAuthorizedRequestParams params = DepositAuthorizedRequestParams.builder()
      .sourceAccount(SOURCE_ACCOUNT)
      .destinationAccount(DESTINATION_ACCOUNT)
      .ledgerHash(LEDGER_HASH)
      .build();
    assertThat(params.ledgerIndex().equals(LedgerIndex.CURRENT));

    String json = "{\n" +
      "            \"source_account\": \"" + SOURCE_ACCOUNT.value() + "\"," +
      "            \"destination_account\": \"" + DESTINATION_ACCOUNT.value() + "\"," +
      "            \"ledger_hash\": \"" + LEDGER_HASH.value() + "\"," +
      "            \"ledger_index\": \"current\"" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testToFromJsonWithBothLedgerHashAndLedgerIndex() throws JSONException, JsonProcessingException {
    DepositAuthorizedRequestParams params = DepositAuthorizedRequestParams.builder()
      .sourceAccount(SOURCE_ACCOUNT)
      .destinationAccount(DESTINATION_ACCOUNT)
      .ledgerHash(LEDGER_HASH)
      .ledgerIndex(LedgerIndex.CURRENT)
      .build();
    assertThat(params.ledgerIndex().equals(LedgerIndex.CURRENT));

    String json = "{\n" +
      "            \"source_account\": \"" + SOURCE_ACCOUNT.value() + "\"," +
      "            \"destination_account\": \"" + DESTINATION_ACCOUNT.value() + "\"," +
      "            \"ledger_hash\": \"" + LEDGER_HASH.value() + "\"," +
      "            \"ledger_index\": \"current\"" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);

    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      DepositAuthorizedRequestParams.builder()
        .sourceAccount(SOURCE_ACCOUNT)
        .destinationAccount(DESTINATION_ACCOUNT)
        .ledgerHash(LEDGER_HASH)
        .ledgerIndex(LedgerIndex.VALIDATED)
        .build();
    });
  }

  @Test
  public void testDefaultValues() {
    DepositAuthorizedRequestParams params = DepositAuthorizedRequestParams.builder()
      .sourceAccount(SOURCE_ACCOUNT)
      .destinationAccount(DESTINATION_ACCOUNT)
      .build();
    assertThat(params.sourceAccount().equals(SOURCE_ACCOUNT));
    assertThat(params.destinationAccount().equals(DESTINATION_ACCOUNT));
    assertThat(params.ledgerIndex().equals(LedgerIndex.CURRENT));
    assertThat(params.ledgerHash()).isEmpty();
  }

  @Test
  public void testParamsWithBothHashAndIndex() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      DepositAuthorizedRequestParams.builder()
        .sourceAccount(SOURCE_ACCOUNT)
        .destinationAccount(DESTINATION_ACCOUNT)
        .ledgerHash(LEDGER_HASH)
        .ledgerIndex(LedgerIndex.VALIDATED)
        .build();
    });
  }
}