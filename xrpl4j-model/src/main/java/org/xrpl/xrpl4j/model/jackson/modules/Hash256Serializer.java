package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import org.xrpl.xrpl4j.model.transactions.Hash256;

import java.io.IOException;

/**
 * Custom Jackson serializer for {@link Hash256}s.
 */
public class Hash256Serializer extends StdScalarSerializer<Hash256> {

  public Hash256Serializer() {
    super(Hash256.class, false);
  }

  @Override
  public void serialize(Hash256 hash256, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeString(hash256.value());
  }
}
