package org.xrpl.xrpl4j.crypto.core.jackson.modules;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.xrpl.xrpl4j.crypto.core.TestConstants.ED_PUBLIC_KEY_HEX;

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


  private PublicKeySerializer serializer;

  @BeforeEach
  void setUp() {
    serializer = new PublicKeySerializer();
  }

  @Test
  void testDeserialize() throws IOException {
    JsonGenerator jsonGeneratorMock = mock(JsonGenerator.class);
    PublicKey expected = PublicKey.fromBase16EncodedPublicKey(ED_PUBLIC_KEY_HEX);

    serializer.serialize(expected, jsonGeneratorMock, mock(SerializerProvider.class));
    verify(jsonGeneratorMock).writeString(ED_PUBLIC_KEY_HEX);
  }
}