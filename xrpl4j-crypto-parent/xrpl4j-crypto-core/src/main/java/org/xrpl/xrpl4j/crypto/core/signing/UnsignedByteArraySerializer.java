package org.xrpl.xrpl4j.crypto.core.signing;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

import java.io.IOException;

/**
 * A FasterXML serializer for {@link UnsignedByteArray}.
 *
 * @deprecated This class will go away once {@link UnsignedByteArray} is moved into the core module.
 */
public class UnsignedByteArraySerializer extends StdSerializer<UnsignedByteArray> {

  public UnsignedByteArraySerializer() {
    super(UnsignedByteArray.class);
  }

  @Override
  public void serialize(UnsignedByteArray value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeString(value.hexValue());
  }
}