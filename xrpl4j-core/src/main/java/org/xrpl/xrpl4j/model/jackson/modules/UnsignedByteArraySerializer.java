package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

import java.io.IOException;

/**
 * A FasterXML serializer for {@link UnsignedByteArray}.
 */
public class UnsignedByteArraySerializer extends StdSerializer<UnsignedByteArray> {

  /**
   * No-args Constructor.
   */
  public UnsignedByteArraySerializer() {
    super(UnsignedByteArray.class);
  }

  @Override
  public void serialize(UnsignedByteArray value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeString(value.hexValue());
  }
}