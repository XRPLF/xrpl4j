package org.xrpl.xrpl4j.model.jackson.modules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.xrpl.xrpl4j.crypto.core.TestConstants.ED_PUBLIC_KEY_HEX;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.google.common.annotations.VisibleForTesting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.core.keys.ImmutablePublicKey;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;

import java.io.IOException;

/**
 * Unit tests for {@link PublicKeyDeserializer}.
 */
class PublicKeyDeserializerTest {

  private PublicKeyDeserializerForTesting deserializer;

  @BeforeEach
  void setUp() {
    deserializer = new PublicKeyDeserializerForTesting();
  }

  @Test
  void testDeserialize() {
    PublicKey expected = PublicKey.fromBase16EncodedPublicKey(ED_PUBLIC_KEY_HEX);
    PublicKey publicKey = deserializer._deserialize(ED_PUBLIC_KEY_HEX, mock(DeserializationContext.class));
    assertThat(publicKey).isEqualTo(expected);
  }

  static class PublicKeyDeserializerForTesting extends PublicKeyDeserializer {

    @Override
    @VisibleForTesting
    public ImmutablePublicKey _deserialize(String publicKey, DeserializationContext deserializationContext) {
      return super._deserialize(publicKey, deserializationContext);
    }
  }
}