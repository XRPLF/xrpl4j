package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.model.transactions.MpTokenNumericAmount;

import java.io.IOException;

/**
 * Custom Jackson deserializer for {@link MpTokenNumericAmount}s.
 */
public class MpTokenNumericAmountDeserializer extends StdDeserializer<MpTokenNumericAmount> {

  /**
   * No-args constructor.
   */
  public MpTokenNumericAmountDeserializer() {
    super(MpTokenNumericAmount.class);
  }

  @Override
  public MpTokenNumericAmount deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    // sfMaximumAmount is an STUInt64, which in JSON is normally represented in base 16, but sfMaximumAmount is
    // in base 10
    return MpTokenNumericAmount.of(UnsignedLong.valueOf(jsonParser.getText()));
  }
}
