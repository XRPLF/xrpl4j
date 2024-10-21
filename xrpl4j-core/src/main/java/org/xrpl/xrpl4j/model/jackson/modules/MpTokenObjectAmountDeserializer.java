package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.model.transactions.MpTokenObjectAmount;

import java.io.IOException;

/**
 * Custom Jackson deserializer for {@link MpTokenObjectAmount}s.
 */
public class MpTokenObjectAmountDeserializer extends StdDeserializer<MpTokenObjectAmount> {

  /**
   * No-args constructor.
   */
  public MpTokenObjectAmountDeserializer() {
    super(MpTokenObjectAmount.class);
  }

  @Override
  public MpTokenObjectAmount deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    // sfMaximumAmount is an STUInt64s, which in JSON is represented as a hex-encoded String.
    return MpTokenObjectAmount.of(UnsignedLong.valueOf(jsonParser.getText(), 16));
  }
}
