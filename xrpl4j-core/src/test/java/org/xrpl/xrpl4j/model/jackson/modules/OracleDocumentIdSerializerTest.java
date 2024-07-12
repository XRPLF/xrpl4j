package org.xrpl.xrpl4j.model.jackson.modules;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.transactions.OracleDocumentId;

import java.io.IOException;

/**
 * Unit tests for {@link OracleDocumentIdSerializer}.
 */
class OracleDocumentIdSerializerTest {

  private OracleDocumentIdSerializer serializer;

  @BeforeEach
  void setUp() {
    serializer = new OracleDocumentIdSerializer();
  }

  @Test
  void testSerialize() throws IOException {
    JsonGenerator jsonGeneratorMock = mock(JsonGenerator.class);

    OracleDocumentId expected = OracleDocumentId.of(UnsignedInteger.ZERO);
    serializer.serialize(expected, jsonGeneratorMock, mock(SerializerProvider.class));
    verify(jsonGeneratorMock).writeString("0");

    expected = OracleDocumentId.of(UnsignedInteger.MAX_VALUE);
    serializer.serialize(expected, jsonGeneratorMock, mock(SerializerProvider.class));
    verify(jsonGeneratorMock).writeString("ffffffff");
  }

}