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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.model.client.accounts.AccountTransactionsTransaction;
import org.xrpl.xrpl4j.model.client.accounts.XrplWebSocketTransaction;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.transactions.Hash256;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.io.IOException;
import java.util.Optional;

/**
 * Custom Jackson Deserializer for {@link AccountTransactionsTransaction}s. This is necessary because Jackson
 * does not deserialize {@link com.fasterxml.jackson.annotation.JsonUnwrapped} fields intelligently.
 */
public class XrplWebSocketTransactionDeserializer extends StdDeserializer<XrplWebSocketTransaction> {

  /**
   * No-args constructor.
   */
  public XrplWebSocketTransactionDeserializer() {
    super(AccountTransactionsTransaction.class);
  }

  @Override
  public XrplWebSocketTransaction deserialize(
    JsonParser jsonParser,
    DeserializationContext ctxt
  ) throws IOException {
    ObjectMapper objectMapper = (ObjectMapper) jsonParser.getCodec();
    JsonNode node = objectMapper.readTree(jsonParser);

    Transaction transaction = objectMapper.readValue(node.toString(), Transaction.class);
    String hash = node.get("hash").asText();
    return XrplWebSocketTransaction.builder()
      .transaction(transaction)
      .hash(Hash256.of(hash))
      .build();
  }
}
