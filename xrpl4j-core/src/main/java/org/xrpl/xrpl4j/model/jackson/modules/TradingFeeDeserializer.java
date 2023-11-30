package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.primitives.UnsignedInteger;
import org.xrpl.xrpl4j.model.transactions.TradingFee;

import java.io.IOException;

/**
 * Custom Jackson deserializer for {@link TradingFee}s.
 */
public class TradingFeeDeserializer extends StdDeserializer<TradingFee> {

  /**
   * No-args constructor.
   */
  public TradingFeeDeserializer() {
    super(TradingFee.class);
  }

  @Override
  public TradingFee deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    return TradingFee.of(UnsignedInteger.valueOf(jsonParser.getLongValue()));
  }
}
