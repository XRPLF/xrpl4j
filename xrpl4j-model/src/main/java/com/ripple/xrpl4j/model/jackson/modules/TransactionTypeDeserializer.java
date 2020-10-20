package com.ripple.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.ripple.xrpl4j.model.transactions.TransactionType;

import java.io.IOException;

public class TransactionTypeDeserializer extends StdDeserializer<TransactionType> {

  public TransactionTypeDeserializer() {
    super(TransactionType.class);
  }

  @Override
  public TransactionType deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    return TransactionType.of(jsonParser.getText());
  }
}
