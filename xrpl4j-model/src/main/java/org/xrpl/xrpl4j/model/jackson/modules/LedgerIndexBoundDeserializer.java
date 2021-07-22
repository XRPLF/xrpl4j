package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.primitives.UnsignedLong;
import org.xrpl.xrpl4j.model.client.specifiers.LedgerIndexBound;

import java.io.IOException;

/**
 * Custom Jackson deserializer for {@link LedgerIndexBound}s.
 */
public class LedgerIndexBoundDeserializer extends StdDeserializer<LedgerIndexBound> {

  protected LedgerIndexBoundDeserializer() {
    super(LedgerIndexBound.class);
  }

  @Override
  public LedgerIndexBound deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
    return LedgerIndexBound.of(jsonParser.getValueAsLong());
  }
}
