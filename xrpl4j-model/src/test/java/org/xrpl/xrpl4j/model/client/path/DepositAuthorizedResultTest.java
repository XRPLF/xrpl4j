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
 * Unit tests for {@link DepositAuthorizedResult}.
 */
public class DepositAuthorizedResultTest extends AbstractJsonTest {

  private static final Address SOURCE_ACCOUNT = Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59");
  private static final Address DESTINATION_ACCOUNT = Address.of("r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59");
  public static final Hash256 LEDGER_HASH = Hash256
    .of("abcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcd");

  @Test
  public void testToFromJsonWithLedgerIndexValidated() throws JSONException, JsonProcessingException {
    // With VALIDATED LedgerIndex
    DepositAuthorizedResult params = DepositAuthorizedResult.builder()
      .sourceAccount(DESTINATION_ACCOUNT)
      .destinationAccount(DESTINATION_ACCOUNT)
      .ledgerIndex(LedgerIndex.VALIDATED)
      .depositAuthorized(true)
      .ledgerCurrentIndex(LedgerIndex.of("123"))
      .validated(true)
      .build();

    assertThat(params.ledgerIndex().equals(LedgerIndex.VALIDATED));

    String json = "{\n" +
      "            \"source_account\": \"" + SOURCE_ACCOUNT.value() + "\"," +
      "            \"destination_account\": \"" + DESTINATION_ACCOUNT.value() + "\"," +
      "            \"ledger_index\": \"validated\"," +
      "            \"deposit_authorized\": true," +
      "            \"ledger_current_index\": 123," +
      "            \"validated\": true" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testToFromJsonWithLedgerIndexCurrent() throws JSONException, JsonProcessingException {
    // With VALIDATED LedgerIndex
    DepositAuthorizedResult params = DepositAuthorizedResult.builder()
      .sourceAccount(DESTINATION_ACCOUNT)
      .destinationAccount(DESTINATION_ACCOUNT)
      .ledgerIndex(LedgerIndex.CURRENT)
      .depositAuthorized(true)
      .ledgerCurrentIndex(LedgerIndex.of("123"))
      .validated(true)
      .build();

    assertThat(params.ledgerIndex().equals(LedgerIndex.CURRENT));

    String json = "{\n" +
      "            \"source_account\": \"" + SOURCE_ACCOUNT.value() + "\"," +
      "            \"destination_account\": \"" + DESTINATION_ACCOUNT.value() + "\"," +
      "            \"ledger_index\": \"current\"," +
      "            \"deposit_authorized\": true," +
      "            \"ledger_current_index\": 123," +
      "            \"validated\": true" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testToFromJsonWithLedgerIndexNumeric() throws JSONException, JsonProcessingException {
    // With VALIDATED LedgerIndex
    DepositAuthorizedResult params = DepositAuthorizedResult.builder()
      .sourceAccount(SOURCE_ACCOUNT)
      .destinationAccount(DESTINATION_ACCOUNT)
      .ledgerIndex(LedgerIndex.of("987"))
      .depositAuthorized(true)
      .ledgerCurrentIndex(LedgerIndex.of("123"))
      .validated(true)
      .build();

    assertThat(params.ledgerIndex().equals(LedgerIndex.of("987")));

    String json = "{\n" +
      "            \"source_account\": \"" + SOURCE_ACCOUNT.value() + "\"," +
      "            \"destination_account\": \"" + DESTINATION_ACCOUNT.value() + "\"," +
      "            \"ledger_index\": 987," +
      "            \"deposit_authorized\": true," +
      "            \"ledger_current_index\": 123," +
      "            \"validated\": true" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testToFromJsonWithLedgerHash() throws JSONException, JsonProcessingException {
    DepositAuthorizedResult params = DepositAuthorizedResult.builder()
      .sourceAccount(SOURCE_ACCOUNT)
      .destinationAccount(DESTINATION_ACCOUNT)
      .ledgerHash(LEDGER_HASH)
      .depositAuthorized(true)
      .ledgerCurrentIndex(LedgerIndex.of("123"))
      .validated(true)
      .build();
    assertThat(params.ledgerIndex().equals(LedgerIndex.CURRENT));

    String json = "{\n" +
      "            \"source_account\": \"" + SOURCE_ACCOUNT.value() + "\"," +
      "            \"destination_account\": \"" + DESTINATION_ACCOUNT.value() + "\"," +
      "            \"ledger_hash\": \"" + LEDGER_HASH + "\"," +
      "            \"deposit_authorized\": true," +
      "            \"ledger_current_index\": 123," +
      "            \"validated\": true" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testToFromJsonWithBothLedgerHashAndLedgerIndex() throws JSONException, JsonProcessingException {
    DepositAuthorizedResult params = DepositAuthorizedResult.builder()
      .sourceAccount(SOURCE_ACCOUNT)
      .destinationAccount(DESTINATION_ACCOUNT)
      .ledgerIndex(LedgerIndex.CURRENT)
      .ledgerHash(LEDGER_HASH)
      .depositAuthorized(true)
      .ledgerCurrentIndex(LedgerIndex.of("123"))
      .validated(true)
      .build();
    assertThat(params.ledgerIndex().equals(LedgerIndex.CURRENT));

    String json = "{\n" +
      "            \"source_account\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\"," +
      "            \"destination_account\": \"r9cZA1mLK5R5Am25ArfXFmqgNwjZgnfk59\"," +
      "            \"ledger_index\": \"current\"," +
      "            \"ledger_hash\": \"abcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcd\"," +
      "            \"deposit_authorized\": true," +
      "            \"ledger_current_index\": 123," +
      "            \"validated\": true" +
      "        }";

    assertCanSerializeAndDeserialize(params, json);

    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      DepositAuthorizedResult.builder()
        .sourceAccount(SOURCE_ACCOUNT)
        .destinationAccount(DESTINATION_ACCOUNT)
        .ledgerIndex(LedgerIndex.VALIDATED)
        .ledgerHash(LEDGER_HASH)
        .depositAuthorized(true)
        .ledgerCurrentIndex(LedgerIndex.of("123"))
        .validated(true)
        .build();
    });
  }

  @Test
  public void testDefaultValues() {
    DepositAuthorizedResult params = DepositAuthorizedResult.builder()
      .sourceAccount(SOURCE_ACCOUNT)
      .destinationAccount(DESTINATION_ACCOUNT)
      .depositAuthorized(true)
      .build();
    assertThat(params.sourceAccount().equals(DESTINATION_ACCOUNT));
    assertThat(params.destinationAccount().equals(DESTINATION_ACCOUNT));
    assertThat(params.ledgerIndex().equals(LedgerIndex.CURRENT));
    assertThat(params.ledgerHash()).isEmpty();
    assertThat(params.depositAuthorized()).isTrue();
  }

  @Test
  public void testParamsWithBothHashAndIndex() {
    Assertions.assertThrows(IllegalStateException.class, () -> {
      DepositAuthorizedResult.builder()
        .sourceAccount(SOURCE_ACCOUNT)
        .destinationAccount(DESTINATION_ACCOUNT)
        .ledgerHash(LEDGER_HASH)
        .ledgerIndex(LedgerIndex.VALIDATED)
        .build();
    });
  }
}