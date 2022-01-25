package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.primitives.UnsignedInteger;
import org.xrpl.xrpl4j.model.transactions.TransferFee;

import java.io.IOException;

/**
 * Custom Jackson deserializer for {@link UnsignedInteger}s.
 */
public class TransferFeeDeserializer  extends StdDeserializer<TransferFee> {

  /**
   * No-args constructor.
   */
  public TransferFeeDeserializer() {
    super(TransferFee.class);
  }

  @Override
  public TransferFee deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    return TransferFee.of(UnsignedInteger.valueOf(jsonParser.getText()));
  }
}
