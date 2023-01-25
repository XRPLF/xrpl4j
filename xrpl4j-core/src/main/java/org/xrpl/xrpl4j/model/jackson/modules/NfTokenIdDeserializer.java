package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.xrpl.xrpl4j.model.transactions.NfTokenId;

import java.io.IOException;

/**
 * Custom Jackson deserializer for {@link NfTokenId}s.
 */
public class NfTokenIdDeserializer extends StdDeserializer<NfTokenId> {
  /**
   * No-args constructor.
   */
  public NfTokenIdDeserializer() {
    super(NfTokenId.class);
  }

  @Override
  public NfTokenId deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    return NfTokenId.of(jsonParser.getText());
  }
}
