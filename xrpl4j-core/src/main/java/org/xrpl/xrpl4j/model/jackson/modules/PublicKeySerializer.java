package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;

import java.io.IOException;
import java.util.Objects;

/**
 * A custom Jackson serializer that serializes a {@link PublicKey} to a hex-string.
 */
public class PublicKeySerializer extends StdSerializer<PublicKey> {

  /**
   * No-args Constructor.
   */
  public PublicKeySerializer() {
    super(PublicKey.class, false);
  }

  @Override
  public void serialize(PublicKey value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeString(this.valueToString(value));
  }

  private String valueToString(final PublicKey publicKey) {
    Objects.requireNonNull(publicKey);
    return publicKey.base16Value();
  }
}