package org.xrpl.xrpl4j.crypto.core.jackson.modules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.google.common.annotations.VisibleForTesting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey.PublicKeyDeserializer;

/**
 * Unit tests for {@link PublicKeyDeserializer}.
 */
class PublicKeyDeserializerTest {

  private static final String HEX_PUBLIC_KEY = "027535A4E90B2189CF9885563F45C4F454B3BFAB21930089C3878A9427B4D648D9";
  private PublicKeyDeserializerForTesting deserializer;

  @BeforeEach
  void setUp() {
    deserializer = new PublicKeyDeserializerForTesting();
  }

  @Test
  void testDeserialize() {
    PublicKey expected = PublicKey.fromBase16EncodedPublicKey(HEX_PUBLIC_KEY);
    PublicKey publicKey = deserializer._deserialize(HEX_PUBLIC_KEY, mock(DeserializationContext.class));
    assertThat(publicKey).isEqualTo(expected);
  }

  static class PublicKeyDeserializerForTesting extends PublicKeyDeserializer {

    @Override
    @VisibleForTesting
    public PublicKey _deserialize(String publicKey, DeserializationContext deserializationContext) {
      return super._deserialize(publicKey, deserializationContext);
    }
  }
}