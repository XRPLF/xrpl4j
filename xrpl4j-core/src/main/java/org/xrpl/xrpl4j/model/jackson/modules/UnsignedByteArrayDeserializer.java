package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;

import java.io.IOException;

/**
 * A FasterXML deserializer for {@link UnsignedByteArray}.
 */
public class UnsignedByteArrayDeserializer extends StdDeserializer<UnsignedByteArray> {

  /**
   * No-args Constructor.
   */
  public UnsignedByteArrayDeserializer() {
    super(UnsignedByteArray.class);
  }

  @Override
  public UnsignedByteArray deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    return UnsignedByteArray.fromHex(jsonParser.getValueAsString());
  }
}