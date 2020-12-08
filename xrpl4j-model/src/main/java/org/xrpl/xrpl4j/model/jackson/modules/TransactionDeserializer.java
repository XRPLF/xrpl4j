package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import org.xrpl.xrpl4j.model.transactions.TransactionType;

import java.io.IOException;

/**
 * Custom deserializer for {@link Transaction}s, which deserializes to a specific {@link Transaction} type
 * based on the TransactionType JSON field.
 */
public class TransactionDeserializer extends StdDeserializer<Transaction> {

  protected TransactionDeserializer() {
    super(Transaction.class);
  }

  @Override
  public Transaction deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    ObjectMapper objectMapper = (ObjectMapper) jsonParser.getCodec();
    ObjectNode objectNode = objectMapper.readTree(jsonParser);

    TransactionType transactionType = TransactionType.forValue(objectNode.get("TransactionType").asText());
    return objectMapper.treeToValue(objectNode, Transaction.typeMap.inverse().get(transactionType));
  }
}
