package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.model.transactions.MptMaximumAmount;

import java.io.IOException;

/**
 * Custom Jackson deserializer for {@link MptMaximumAmount}s.
 */
public class MptMaximumAmountDeserializer extends StdDeserializer<MptMaximumAmount> {

  /**
   * No-args constructor.
   */
  public MptMaximumAmountDeserializer() {
    super(MptMaximumAmount.class);
  }

  @Override
  public MptMaximumAmount deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    // sfMaximumAmount is an STUInt64s, which in JSON is represented as a hex-encoded String.
    return MptMaximumAmount.of(UnsignedLong.valueOf(jsonParser.getText(), 16));
  }
}
