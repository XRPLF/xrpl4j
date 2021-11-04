package org.xrpl.xrpl4j.crypto.core.signing;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

import java.io.IOException;

/**
 * A FasterXML serializer for {@link UnsignedByteArray}.
 *
 * @deprecated This class will go away once {@link UnsignedByteArray} is moved into the core module.
 */
public class UnsignedByteArrayDeserializer extends StdDeserializer<UnsignedByteArray> {

  public UnsignedByteArrayDeserializer() {
    super(UnsignedByteArray.class);
  }

  @Override
  public UnsignedByteArray deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    return UnsignedByteArray.fromHex(p.getValueAsString());
  }
}