package com.ripple.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.ripple.xrpl4j.model.transactions.TransactionType;

import java.io.IOException;

public class TransactionTypeSerializer extends StdScalarSerializer<TransactionType> {

  public TransactionTypeSerializer() {
    super(TransactionType.class, false);
  }

  @Override
  public void serialize(TransactionType transactionType, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeString(transactionType.value());
  }
}
