package org.xrpl.xrpl4j.model.jackson.modules;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.FromStringDeserializer;
import org.xrpl.xrpl4j.crypto.core.keys.ImmutablePublicKey;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;

import java.io.IOException;

/**
 * A custom Jackson deserializer to deserialize {@link PublicKey}s from a hex string in JSON.
 */
public class PublicKeyDeserializer extends FromStringDeserializer<ImmutablePublicKey> {

  /**
   * No-args constructor.
   */
  public PublicKeyDeserializer() {
    super(PublicKey.class);
  }

  @Override
  protected ImmutablePublicKey _deserialize(String publicKey, DeserializationContext ctxt) {
    return (ImmutablePublicKey) PublicKey.fromBase16EncodedPublicKey(publicKey);
  }

}
