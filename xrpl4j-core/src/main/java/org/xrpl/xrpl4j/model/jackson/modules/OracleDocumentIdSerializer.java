package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import org.xrpl.xrpl4j.model.transactions.OracleDocumentId;

import java.io.IOException;

/**
 * Custom Jackson serializer for {@link OracleDocumentId}s.
 */
public class OracleDocumentIdSerializer extends StdScalarSerializer<OracleDocumentId> {

  /**
   * No-args constructor.
   */
  public OracleDocumentIdSerializer() {
    super(OracleDocumentId.class, false);
  }

  @Override
  public void serialize(
    OracleDocumentId oracleDocumentId,
    JsonGenerator gen,
    SerializerProvider provider
  ) throws IOException {
    gen.writeNumber(oracleDocumentId.value().longValue());
  }
}
