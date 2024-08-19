package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.primitives.UnsignedInteger;
import org.xrpl.xrpl4j.model.transactions.OracleDocumentId;

import java.io.IOException;

/**
 * Custom Jackson deserializer for {@link OracleDocumentId}s.
 */
public class OracleDocumentIdDeserializer extends StdDeserializer<OracleDocumentId> {

  /**
   * No-args constructor.
   */
  public OracleDocumentIdDeserializer() {
    super(OracleDocumentId.class);
  }

  @Override
  public OracleDocumentId deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    return OracleDocumentId.of(UnsignedInteger.valueOf(jsonParser.getLongValue()));
  }
}
