package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import org.xrpl.xrpl4j.model.transactions.NfTokenUri;

import java.io.IOException;

/**
 * Custom Jackson serializer for {@link NfTokenUri}s.
 */
public class NfTokenUriSerializer extends StdScalarSerializer<NfTokenUri> {

  /**
   * No-args constructor.
   */
  public NfTokenUriSerializer() {
    super(NfTokenUri.class, false);
  }

  @Override
  public void serialize(NfTokenUri uri, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeString(uri.value());
  }

}