package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import org.xrpl.xrpl4j.model.transactions.TradingFee;

import java.io.IOException;

/**
 * Custom Jackson serializer for {@link TradingFee}s.
 */
public class TradingFeeSerializer extends StdScalarSerializer<TradingFee> {

  /**
   * No-args constructor.
   */
  public TradingFeeSerializer() {
    super(TradingFee.class, false);
  }

  @Override
  public void serialize(TradingFee tradingFee, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeNumber(tradingFee.value().longValue());
  }
}
