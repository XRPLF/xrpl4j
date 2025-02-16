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
 * Custom deserializer for {@link Transaction}s, which deserializes to a specific {@link Transaction} type based on the
 * TransactionType JSON field.
 */
public class TransactionDeserializer<T extends Transaction> extends StdDeserializer<T> {

  /**
   * No-args constructor.
   */
  protected TransactionDeserializer() {
    super(Transaction.class);
  }

  @Override
  public T deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    ObjectMapper objectMapper = (ObjectMapper) jsonParser.getCodec();
    ObjectNode objectNode = objectMapper.readTree(jsonParser);

    JsonNode node = objectNode.get("TransactionType");
    TransactionType transactionType = TransactionType.forValue(node.asText());
    @SuppressWarnings("unchecked")
    Class<T> transactionTypeClass = (Class<T>) Transaction.typeMap.inverse().get(transactionType);

    // Remove the `Account` property from any incoming UnlModify JSON about to be deserialized. This is because the JSON
    // returned by the rippled/clio API v1 has a bug where the account value is an empty string. For this particular
    // For `UnlModify` only, the Java value for the Account is always set to ACCOUNT_ZERO via a default
    // method, so this value is removed from the JSON before deserialization because it's not needed (the default
    // method handles population in Java) and if not removed, this field will end up in the `unknownFields`
    // map of the ultimate Java object, which is incorrect.
    if (UnlModify.class.isAssignableFrom(transactionTypeClass)) {
      objectNode.remove("Account");
    }

    return objectMapper.treeToValue(objectNode, transactionTypeClass);
  }
}
