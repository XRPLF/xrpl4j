package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.xrpl.xrpl4j.model.transactions.OracleProvider;

import java.io.IOException;

/**
 * A custom Jackson deserializer to deserialize {@link OracleProvider}s from a hex string in JSON.
 */
public class OracleProviderDeserializer extends StdDeserializer<OracleProvider> {

  /**
   * No-args constructor.
   */
  public OracleProviderDeserializer() {
    super(OracleProvider.class);
  }

  @Override
  public OracleProvider deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    return OracleProvider.of(jsonParser.getText());
  }

}
