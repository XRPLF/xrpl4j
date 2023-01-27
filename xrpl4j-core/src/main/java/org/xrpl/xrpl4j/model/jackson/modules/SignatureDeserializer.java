package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.signing.Signature;

import java.io.IOException;

/**
 * Deserializes signature string value to an object of type {@link Signature}.
 */
public class SignatureDeserializer extends JsonDeserializer<Signature> {

  @Override
  public Signature deserialize(JsonParser jsonParser, DeserializationContext ctxt)
    throws IOException {
    JsonNode node = jsonParser.getCodec().readTree(jsonParser);
    return Signature.builder().value(UnsignedByteArray.fromHex(node.asText())).build();
  }
}