package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.xrpl.xrpl4j.model.transactions.Marker;

import java.io.IOException;

/**
 * Custom Jackson serializer for {@link Marker}s.
 */
public class MarkerSerializer extends StdSerializer<Marker> {

  /**
   * No-args constructor.
   */
  protected MarkerSerializer() {
    super(Marker.class);
  }

  @Override
  public void serialize(Marker value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    if (value.value().startsWith("{")) {
      gen.writeRawValue(value.value());
    } else {
      gen.writeString(value.value());
    }
  }
}
