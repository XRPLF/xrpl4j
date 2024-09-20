package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.xrpl.xrpl4j.model.transactions.OracleUri;

import java.io.IOException;

/**
 * A custom Jackson deserializer to deserialize {@link OracleUri}s from a hex string in JSON.
 */
public class OracleUriDeserializer extends StdDeserializer<OracleUri> {

  /**
   * No-args constructor.
   */
  public OracleUriDeserializer() {
    super(OracleUri.class);
  }

  @Override
  public OracleUri deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    return OracleUri.of(jsonParser.getText());
  }

}
