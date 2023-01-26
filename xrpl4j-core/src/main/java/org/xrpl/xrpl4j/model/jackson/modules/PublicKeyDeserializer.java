package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;

import java.io.IOException;

/**
 * A custom Jackson deserializer to deserialize {@link PublicKey}s from a hex string in JSON.
 */
public class PublicKeyDeserializer extends StdDeserializer<PublicKey> {

  /**
   * No-args constructor.
   */
  public PublicKeyDeserializer() {
    super(PublicKey.class);
  }

  @Override
  public PublicKey deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    return PublicKey.fromBase16EncodedPublicKey(jsonParser.getText());
  }

}
