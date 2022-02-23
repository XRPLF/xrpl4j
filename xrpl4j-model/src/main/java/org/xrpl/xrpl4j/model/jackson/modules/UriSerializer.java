package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import org.xrpl.xrpl4j.model.transactions.Uri;

import java.io.IOException;

/**
 * Custom Jackson serializer for {@link Uri}s.
 */
public class UriSerializer extends StdScalarSerializer<Uri> {

  /**
   * No-args constructor.
   */
  public UriSerializer() {
    super(Uri.class, false);
  }

  @Override
  public void serialize(Uri uri, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeString(uri.toString());
  }

}