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
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
      .ledgerSpecifier(LedgerSpecifier.VALIDATED)
      .build();

    assertThat(params.ledgerSpecifier().equals(LedgerSpecifier.VALIDATED));

    String json = "{" +
      "  \"source_account\": \"" + SOURCE_ACCOUNT.value() + "\"," +
      "  \"destination_account\": \"" + DESTINATION_ACCOUNT.value() + "\"," +
      "  \"ledger_index\": \"validated\"" +
      "}";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testToFromJsonWithLedgerIndexCurrent() throws JSONException, JsonProcessingException {
    DepositAuthorizedRequestParams params = DepositAuthorizedRequestParams.builder()
      .sourceAccount(SOURCE_ACCOUNT)
      .destinationAccount(DESTINATION_ACCOUNT)
      .ledgerSpecifier(LedgerSpecifier.CURRENT)
      .build();
    assertThat(params.ledgerSpecifier().equals(LedgerSpecifier.CURRENT));

    String json = "{" +
      "  \"source_account\": \"" + SOURCE_ACCOUNT.value() + "\"," +
      "  \"destination_account\": \"" + DESTINATION_ACCOUNT.value() + "\"," +
      "  \"ledger_index\": \"current\"" +
      "}";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testToFromJsonWithLedgerIndexClosed() throws JSONException, JsonProcessingException {
    DepositAuthorizedRequestParams params = DepositAuthorizedRequestParams.builder()
      .sourceAccount(SOURCE_ACCOUNT)
      .destinationAccount(DESTINATION_ACCOUNT)
      .ledgerSpecifier(LedgerSpecifier.CLOSED)
      .build();

    assertThat(params.ledgerSpecifier().equals(LedgerSpecifier.CLOSED));

    String json = "{" +
      "  \"source_account\": \"" + SOURCE_ACCOUNT.value() + "\"," +
      "  \"destination_account\": \"" + DESTINATION_ACCOUNT.value() + "\"," +
      "  \"ledger_index\": \"closed\"" +
      "}";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testToFromJsonWithLedgerHash() throws JSONException, JsonProcessingException {
    DepositAuthorizedRequestParams params = DepositAuthorizedRequestParams.builder()
      .sourceAccount(SOURCE_ACCOUNT)
      .destinationAccount(DESTINATION_ACCOUNT)
      .ledgerSpecifier(LedgerSpecifier.of(LEDGER_HASH))
      .build();
    assertThat(params.ledgerSpecifier().equals(LedgerSpecifier.CURRENT));

    String json = "{" +
      "  \"source_account\": \"" + SOURCE_ACCOUNT.value() + "\"," +
      "  \"destination_account\": \"" + DESTINATION_ACCOUNT.value() + "\"," +
      "  \"ledger_hash\": \"" + LEDGER_HASH.value() + "\"" +
      "  }";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testToFromJsonWithLedgerIndex() throws JSONException, JsonProcessingException {
    DepositAuthorizedRequestParams params = DepositAuthorizedRequestParams.builder()
      .sourceAccount(SOURCE_ACCOUNT)
      .destinationAccount(DESTINATION_ACCOUNT)
      .ledgerSpecifier(LedgerSpecifier.of(LedgerIndex.of(UnsignedInteger.ONE)))
      .build();
    assertThat(params.ledgerSpecifier().equals(LedgerSpecifier.CURRENT));

    String json = "{" +
      "  \"source_account\": \"" + SOURCE_ACCOUNT.value() + "\"," +
      "  \"destination_account\": \"" + DESTINATION_ACCOUNT.value() + "\"," +
      "  \"ledger_index\": 1" +
      "}";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testToFromJsonWithCredentialIds() throws JSONException, JsonProcessingException {
    List<Hash256> credentials = Arrays.asList(
      Hash256.of("A182EFBD154C9E80195082F86C1C8952FC0760A654B886F61BB0A59803B4387B"),
      Hash256.of("B9D3E1F5A72C1D58B6305290C3D8A1EE4F8D6F12ABCD1234567890ABCDEF1234")
    );

    DepositAuthorizedRequestParams params = DepositAuthorizedRequestParams.builder()
      .sourceAccount(SOURCE_ACCOUNT)
      .destinationAccount(DESTINATION_ACCOUNT)
      .credentials(credentials)
      .ledgerSpecifier(LedgerSpecifier.of(LedgerIndex.of(UnsignedInteger.ONE)))
      .build();

    assertThat(params.ledgerSpecifier().equals(LedgerSpecifier.of(LedgerIndex.of(UnsignedInteger.ONE))));

    String json = "{" +
      "  \"source_account\": \"" + SOURCE_ACCOUNT.value() + "\"," +
      "  \"destination_account\": \"" + DESTINATION_ACCOUNT.value() + "\"," +
      "  \"credentials\": [" +
      "    \"A182EFBD154C9E80195082F86C1C8952FC0760A654B886F61BB0A59803B4387B\"," +
      "    \"B9D3E1F5A72C1D58B6305290C3D8A1EE4F8D6F12ABCD1234567890ABCDEF1234\"" +
      "  ]," +
      "  \"ledger_index\": 1" +
      "}";

    assertCanSerializeAndDeserialize(params, json);
  }

  @Test
  public void testMoreThanEightCredentials() {
    List<Hash256> moreThanEight = IntStream.range(0, 9)
      .mapToObj(i ->
        Hash256.of("7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF" + i))
      .collect(Collectors.toList());

    assertThatThrownBy(() -> DepositAuthorizedRequestParams.builder()
      .sourceAccount(SOURCE_ACCOUNT)
      .destinationAccount(DESTINATION_ACCOUNT)
      .credentials(moreThanEight)
      .ledgerSpecifier(LedgerSpecifier.of(LedgerIndex.of(UnsignedInteger.ONE)))
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("credentials should have less than or equal to 8 items.");
  }

  @Test
  public void testDuplicateCredentials() {
    List<Hash256> randomIds = IntStream.range(0, 8)
      .mapToObj(i -> Hash256.of("7C221D901192C74AA7AC60786B1B01A88E922BE267E5B5B4FA64D214C5067FF" + i))
      .collect(Collectors.toList());

    randomIds.set(0, randomIds.get(1));

    assertThatThrownBy(() -> DepositAuthorizedRequestParams.builder()
      .sourceAccount(SOURCE_ACCOUNT)
      .destinationAccount(DESTINATION_ACCOUNT)
      .credentials(randomIds)
      .ledgerSpecifier(LedgerSpecifier.of(LedgerIndex.of(UnsignedInteger.ONE)))
      .build()
    ).isInstanceOf(IllegalArgumentException.class)
      .hasMessage("credentials should have unique values.");
  }

  @Test
  public void testDefaultValues() {
    DepositAuthorizedRequestParams params = DepositAuthorizedRequestParams.builder()
      .sourceAccount(SOURCE_ACCOUNT)
      .destinationAccount(DESTINATION_ACCOUNT)
      .build();
    assertThat(params.sourceAccount().equals(SOURCE_ACCOUNT));
    assertThat(params.destinationAccount().equals(DESTINATION_ACCOUNT));
    assertThat(params.ledgerSpecifier().equals(LedgerSpecifier.CURRENT));
  }
}
