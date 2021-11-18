package org.xrpl.xrpl4j.crypto.core.jackson.modules;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.core.keys.PublicKey.PublicKeySerializer;

import java.io.IOException;

/**
 * Unit tests for {@link PublicKeySerializer}.
 */
class PublicKeySerializerTest {

  private static final String HEX_PUBLIC_KEY = "027535A4E90B2189CF9885563F45C4F454B3BFAB21930089C3878A9427B4D648D9";
  private PublicKeySerializer serializer;

  @BeforeEach
  void setUp() {
    serializer = new PublicKeySerializer();
  }

  @Test
  void testDeserialize() throws IOException {
    JsonGenerator jsonGeneratorMock = mock(JsonGenerator.class);
    PublicKey expected = PublicKey.fromBase16EncodedPublicKey(HEX_PUBLIC_KEY);

    serializer.serialize(expected, jsonGeneratorMock, mock(SerializerProvider.class));
    verify(jsonGeneratorMock).writeString(HEX_PUBLIC_KEY);
  }
}