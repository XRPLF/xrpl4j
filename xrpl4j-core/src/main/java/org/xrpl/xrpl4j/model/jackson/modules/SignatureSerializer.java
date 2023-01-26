package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.xrpl.xrpl4j.crypto.core.signing.Signature;

import java.io.IOException;

/**
 * Custom Jackson serializer for {@link Signature}es.
 */
public class SignatureSerializer extends JsonSerializer<Signature> {
  @Override
  public void serialize(Signature signature, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeString(signature.base16Value());
  }
}
