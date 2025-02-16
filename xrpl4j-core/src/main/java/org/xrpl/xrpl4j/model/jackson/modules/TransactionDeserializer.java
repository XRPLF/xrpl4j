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
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.TransactionType;
import org.xrpl.xrpl4j.model.transactions.UnlModify;

import java.io.IOException;

/**
 * Custom deserializer for instances of {@link Transaction} that deserialize to a specific {@link Transaction} type
 * based on the`TransactionType` JSON field
 */
public class TransactionDeserializer extends StdDeserializer<Transaction> {

  /**
   * No-args constructor.
   */
  protected TransactionDeserializer() {
    super(Transaction.class);
  }

  @Override
  public Transaction deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    final ObjectMapper objectMapper = (ObjectMapper) jsonParser.getCodec();
    final ObjectNode objectNode = objectMapper.readTree(jsonParser);

    final JsonNode transactionTypeJsonNode = objectNode.get("TransactionType");
    final TransactionType transactionType = TransactionType.forValue(transactionTypeJsonNode.asText());
    final Class<? extends Transaction> transactionTypeClass = Transaction.typeMap.inverse().get(transactionType);

    // Remove the `Account` property from any incoming `UnlModify` JSON about to be deserialized. This is because the 
    // JSON returned by the rippled/clio API v1 has a bug where the account value in `UnlModify` transactions is an 
    // empty string. In this case, the Java value for the `Account` property is always set to ACCOUNT_ZERO via a default
    // method, so this property is removed from incoming JSON before deserialization because it's not needed (the default
    // method handles population in Java) and if not removed, this field will end up in the `unknownFields`
    // map of the ultimate Java object, which is incorrect.
    if (UnlModify.class.isAssignableFrom(transactionTypeClass)) {
      objectNode.remove("Account");
    }

    return objectMapper.treeToValue(objectNode, transactionTypeClass);
  }
}
