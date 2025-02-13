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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.TransactionType;
import org.xrpl.xrpl4j.model.transactions.UnknownTransaction;
import org.xrpl.xrpl4j.model.transactions.UnlModify;

import java.io.IOException;

/**
 * Custom deserializer for {@link Transaction}s, which deserializes to a specific {@link Transaction} type based on the
 * TransactionType JSON field.
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
    ObjectMapper objectMapper = (ObjectMapper) jsonParser.getCodec();
    ObjectNode objectNode = objectMapper.readTree(jsonParser);

    TransactionType transactionType = TransactionType.forValue(objectNode.get("TransactionType").asText());
    Class<? extends Transaction> transactionTypeClass = Transaction.typeMap.inverse().get(transactionType);

    // If the transaction is of type `UnknownTransaction`, the `TransactionType` property must _not_ be removed. Thi
    // is so that the derived functions related to `TransactionType` in that class operate properly. However, for all
    // _other_ transaction types, the `TransactionType` property _must_ be removed so that it doesn't errantly show up
    // in the `unknownTransactionType` map. This approach works because every subclass of `Transaction` has a derived
    // Java method that specifies the type (thus allowing us to ignore this fiele in the general case).
    if (!UnknownTransaction.class.isAssignableFrom(transactionTypeClass)) {
      objectNode.remove("TransactionType");
    }

    // If the transaction is of type `UnlModify`, then remove the `Account` property. This is because the JSON returned
    // by the rippled/clio API v1 has a bug where the account value is often an empty string. For this particular
    // transaction type (i.e., `UnlModify`) the Java value for the account is always set to ACCOUNT_ZERO via a default
    // method, so we remove this value from the JSON before deserialization because (1) it's not needed (the default
    // method handles population in Java) and (2) if not removed, this field will end up in the `unknownFields`
    // property, which is incorrect.
    if (UnlModify.class.isAssignableFrom(transactionTypeClass)) {
//    if (objectNode.get("Account").isEmpty()) {
      if (objectNode.has("Account")) {
        //objectNode.remove("Account");
      }
    }

    // TODO: Verify, and document if keeping.
    if (UnlModify.class.isAssignableFrom(transactionTypeClass)) {
      objectNode.remove("Account");
    }

    return objectMapper.treeToValue(objectNode, transactionTypeClass);
  }
}
