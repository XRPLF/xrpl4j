package org.xrpl.xrpl4j.model.ledger;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.AbstractJsonTest;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Credential;
import org.xrpl.xrpl4j.model.transactions.CredentialType;
import org.xrpl.xrpl4j.model.transactions.CredentialWrapper;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.util.Collections;
import java.util.List;

public class DepositPreAuthObjectJsonTests extends AbstractJsonTest {

  @Test
  public void testJson() throws JsonProcessingException, JSONException {
    DepositPreAuthObject object = DepositPreAuthObject.builder()
      .account(Address.of("rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8"))
      .authorize(Address.of("rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de"))
      .ownerNode("0000000000000000")
      .previousTransactionId(Hash256.of("3E8964D5A86B3CD6B9ECB33310D4E073D64C865A5B866200AD2B7E29F8326702"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(7))
      .index(Hash256.of("4A255038CC3ADCC1A9C91509279B59908251728D0DAADB248FFE297D0F7E068C"))
      .build();

    String json = "{\n" +
      "  \"LedgerEntryType\" : \"DepositPreauth\",\n" +
      "  \"Account\" : \"rsUiUMpnrgxQp24dJYZDhmV4bE3aBtQyt8\",\n" +
      "  \"Authorize\" : \"rEhxGqkqPPSxQ3P25J66ft5TwpzV14k2de\",\n" +
      "  \"Flags\" : 0,\n" +
      "  \"OwnerNode\" : \"0000000000000000\",\n" +
      "  \"PreviousTxnID\" : \"3E8964D5A86B3CD6B9ECB33310D4E073D64C865A5B866200AD2B7E29F8326702\",\n" +
      "  \"PreviousTxnLgrSeq\" : 7,\n" +
      "  \"index\" : \"4A255038CC3ADCC1A9C91509279B59908251728D0DAADB248FFE297D0F7E068C\"\n" +
      "}";

    assertCanSerializeAndDeserialize(object, json);
  }

  @Test
  public void testJsonWithAuthorizeCredential() throws JsonProcessingException, JSONException {
    List<CredentialWrapper> credentials = Collections.singletonList(
      CredentialWrapper
        .builder()
        .credential(
          Credential.builder()
            .credentialType(CredentialType.of("6D795F63726564656E7469616C"))
            .issuer(Address.of("rK2vwKgQqXahHWUvi9VVTQsYe6gze5n1os"))
            .build())
        .build()
    );

    DepositPreAuthObject object = DepositPreAuthObject.builder()
      .account(Address.of("rK2vwKgQqXahHWUvi9VVTQsYe6gze5n1os"))
      .authorizeCredentials(credentials)
      .ownerNode("0")
      .previousTransactionId(Hash256.of("3D4665AE6874D7E4E34B45E906FF970CD820EB42B1DBD238588E845466D1CE61"))
      .previousTransactionLedgerSequence(UnsignedInteger.valueOf(3113101))
      .index(Hash256.of("7587E9C2F7CFCD74D924614C2FFBAD6D590590052B69CCA2BA25DAEBB678067A"))
      .build();

    String json = "{\n" +
      " \"Account\": \"rK2vwKgQqXahHWUvi9VVTQsYe6gze5n1os\",\n" +
      " \"AuthorizeCredentials\": [\n" +
      " {\n" +
      " \"Credential\": {\n" +
      " \"CredentialType\": \"6D795F63726564656E7469616C\",\n" +
      " \"Issuer\": \"rK2vwKgQqXahHWUvi9VVTQsYe6gze5n1os\"\n" +
      " }\n" +
      " }\n" +
      " ],\n" +
      " \"Flags\": 0,\n" +
      " \"LedgerEntryType\": \"DepositPreauth\",\n" +
      " \"OwnerNode\": \"0\",\n" +
      " \"PreviousTxnID\": \"3D4665AE6874D7E4E34B45E906FF970CD820EB42B1DBD238588E845466D1CE61\",\n" +
      " \"PreviousTxnLgrSeq\": 3113101,\n" +
      " \"index\": \"7587E9C2F7CFCD74D924614C2FFBAD6D590590052B69CCA2BA25DAEBB678067A\"\n" +
      " }";

    assertCanSerializeAndDeserialize(object, json);
  }
}
