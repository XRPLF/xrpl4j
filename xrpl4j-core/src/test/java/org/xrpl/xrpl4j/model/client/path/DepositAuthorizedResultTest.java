package org.xrpl.xrpl4j.model.client.path;

/*-
 * ========================LICENSE_START=================================
 * xrpl4j :: model
 * %%
 * Copyright (C) 2020 - 2022 XRPL Foundation and its contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
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
  public void testJsonWithIndexAndNoCurrentIndex() throws JsonProcessingException, JSONException {
    DepositAuthorizedResult result = DepositAuthorizedResult.builder()
      .sourceAccount(DESTINATION_ACCOUNT)
      .destinationAccount(DESTINATION_ACCOUNT)
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.ONE))
      .status("success")
      .depositAuthorized(true)
      .validated(true)
      .build();

    String json = "{\n" +
      " \"deposit_authorized\":true," +
      " \"source_account\": \"" + SOURCE_ACCOUNT.value() + "\"," +
      " \"destination_account\": \"" + DESTINATION_ACCOUNT.value() + "\"," +
      " \"ledger_index\": 1," +
      " \"status\":\"success\"," +
      " \"deposit_authorized\": true," +
      " \"validated\":true" +
      "}";

    assertCanSerializeAndDeserialize(result, json);
  }

  @Test
  public void testJsonWithNoIndexAndCurrentIndex() throws JsonProcessingException, JSONException {
    DepositAuthorizedResult result = DepositAuthorizedResult.builder()
      .sourceAccount(DESTINATION_ACCOUNT)
      .destinationAccount(DESTINATION_ACCOUNT)
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.valueOf(9)))
      .status("success")
      .depositAuthorized(true)
      .validated(true)
      .build();

    String json = "{\n" +
      " \"deposit_authorized\":true," +
      " \"source_account\": \"" + SOURCE_ACCOUNT.value() + "\"," +
      " \"destination_account\": \"" + DESTINATION_ACCOUNT.value() + "\"," +
      " \"ledger_current_index\":9," +
      " \"status\":\"success\"," +
      " \"deposit_authorized\": true," +
      " \"validated\":true" +
      "}";

    assertCanSerializeAndDeserialize(result, json);
  }

  @Test
  public void testDefaultValues() {
    DepositAuthorizedResult result = DepositAuthorizedResult.builder()
      .sourceAccount(SOURCE_ACCOUNT)
      .destinationAccount(DESTINATION_ACCOUNT)
      .depositAuthorized(true)
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.ONE))
      .build();
    assertThat(result.sourceAccount()).isEqualTo(DESTINATION_ACCOUNT);
    assertThat(result.destinationAccount()).isEqualTo(DESTINATION_ACCOUNT);
    assertThat(result.ledgerIndex()).isNotEmpty().get().isEqualTo(LedgerIndex.of(UnsignedInteger.ONE));
    assertThat(result.ledgerHash()).isEmpty();
    assertThat(result.depositAuthorized()).isTrue();
  }

  @Test
  void testWithHash() {
    DepositAuthorizedResult result = DepositAuthorizedResult.builder()
      .sourceAccount(SOURCE_ACCOUNT)
      .ledgerHash(LEDGER_HASH)
      .destinationAccount(DESTINATION_ACCOUNT)
      .depositAuthorized(true)
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.ONE))
      .build();

    assertThat(result.ledgerHash()).isNotEmpty().get().isEqualTo(result.ledgerHashSafe());
  }

  @Test
  void testWithoutHash() {
    DepositAuthorizedResult result = DepositAuthorizedResult.builder()
      .sourceAccount(SOURCE_ACCOUNT)
      .destinationAccount(DESTINATION_ACCOUNT)
      .depositAuthorized(true)
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.ONE))
      .build();

    assertThat(result.ledgerHash()).isEmpty();
    assertThrows(
      IllegalStateException.class,
      result::ledgerHashSafe
    );
  }

  @Test
  void testWithLedgerIndex() {
    DepositAuthorizedResult result = DepositAuthorizedResult.builder()
      .sourceAccount(SOURCE_ACCOUNT)
      .destinationAccount(DESTINATION_ACCOUNT)
      .depositAuthorized(true)
      .ledgerIndex(LedgerIndex.of(UnsignedInteger.ONE))
      .build();

    assertThat(result.ledgerIndex()).isNotEmpty().get().isEqualTo(result.ledgerIndexSafe());
    assertThat(result.ledgerCurrentIndex()).isEmpty();
    assertThrows(
      IllegalStateException.class,
      result::ledgerCurrentIndexSafe
    );
  }

  @Test
  void testWithLedgerCurrentIndex() {
    DepositAuthorizedResult result = DepositAuthorizedResult.builder()
      .sourceAccount(SOURCE_ACCOUNT)
      .destinationAccount(DESTINATION_ACCOUNT)
      .depositAuthorized(true)
      .ledgerCurrentIndex(LedgerIndex.of(UnsignedInteger.ONE))
      .build();

    assertThat(result.ledgerIndex()).isEmpty();
    assertThat(result.ledgerCurrentIndex()).isNotEmpty().get().isEqualTo(result.ledgerCurrentIndexSafe());
    assertThrows(
      IllegalStateException.class,
      result::ledgerIndexSafe
    );
  }
}
