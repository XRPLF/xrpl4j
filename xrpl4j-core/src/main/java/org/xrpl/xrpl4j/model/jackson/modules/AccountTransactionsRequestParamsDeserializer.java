package org.xrpl.xrpl4j.model.jackson.modules;

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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.primitives.UnsignedInteger;
import org.xrpl.xrpl4j.model.client.accounts.AccountTransactionsRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.ImmutableAccountTransactionsRequestParams;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerIndexBound;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Marker;

import java.io.IOException;
import java.util.Optional;

/**
 * Custom deserializer for {@link AccountTransactionsRequestParams}. This is needed because Jackson cannot deserialize
 * {@link com.fasterxml.jackson.annotation.JsonUnwrapped} values if they are wrapped in an {@link Optional}, as is the
 * case with {@link AccountTransactionsRequestParams#ledgerSpecifier()}.
 */
public class AccountTransactionsRequestParamsDeserializer extends StdDeserializer<AccountTransactionsRequestParams> {

  /**
   * No-args constructor.
   */
  public AccountTransactionsRequestParamsDeserializer() {
    super(AccountTransactionsRequestParams.class);
  }

  @Override
  public AccountTransactionsRequestParams deserialize(
    JsonParser jsonParser,
    DeserializationContext ctxt
  ) throws IOException {
    ObjectMapper objectMapper = (ObjectMapper) jsonParser.getCodec();
    JsonNode node = objectMapper.readTree(jsonParser);

    AccountTransactionsRequestParams params = ImmutableAccountTransactionsRequestParams.builder()
      .account(Address.of(node.get("account").asText()))
      .ledgerIndexMinimum(
        node.has("ledger_index_min") ?
          LedgerIndexBound.of(node.get("ledger_index_min").asLong()) :
          null
      )
      .ledgerIndexMaximum(
        node.has("ledger_index_max") ?
          LedgerIndexBound.of(node.get("ledger_index_max").asLong()) :
          null
      )
      .forward(node.get("forward").asBoolean())
      .limit(
        Optional.ofNullable(node.get("limit"))
          .map(JsonNode::asLong)
          .map(UnsignedInteger::valueOf)
      )
      .marker(
        Optional.ofNullable(node.get("marker"))
          .map(JsonNode::toString)
          .map(markerString -> {
            try {
              return objectMapper.readValue(markerString, Marker.class);
            } catch (JsonProcessingException e) {
              return null;
            }
          })
      )
      .build();

    LedgerSpecifier ledgerSpecifier = null;

    final JsonNode ledgerHash = node.get("ledger_hash");
    if (ledgerHash != null) {
      ledgerSpecifier = LedgerSpecifier.of(Hash256.of(ledgerHash.asText()));
    } else if (node.has("ledger_index")) {
      final JsonNode ledgerIndex = node.get("ledger_index");
      if (ledgerIndex.isNumber()) {
        ledgerSpecifier = LedgerSpecifier.of(LedgerIndex.of(UnsignedInteger.valueOf(ledgerIndex.asInt())));
      } else {
        switch (ledgerIndex.asText()) {
          case "validated":
            ledgerSpecifier = LedgerSpecifier.VALIDATED;
            break;
          case "current":
            ledgerSpecifier = LedgerSpecifier.CURRENT;
            break;
          case "closed":
            ledgerSpecifier = LedgerSpecifier.CLOSED;
            break;
          default:
            throw new JsonParseException(
              jsonParser,
              "Unrecognized LedgerIndex shortcut '" + ledgerIndex.toString() + "'."
            );
        }
      }
    }

    return ImmutableAccountTransactionsRequestParams.builder().from(params)
      .ledgerSpecifier(Optional.ofNullable(ledgerSpecifier))
      .build();
  }
}
