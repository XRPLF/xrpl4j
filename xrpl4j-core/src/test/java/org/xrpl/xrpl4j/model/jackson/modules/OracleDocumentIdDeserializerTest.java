package org.xrpl.xrpl4j.model.jackson.modules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.google.common.primitives.UnsignedInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.transactions.OracleDocumentId;

import java.io.IOException;

/**
 * Unit test for {@link OracleDocumentIdDeserializer}.
 */
class OracleDocumentIdDeserializerTest {

  private OracleDocumentIdDeserializer deserializer;

  @BeforeEach
  void setUp() {
    deserializer = new OracleDocumentIdDeserializer();
  }

  @Test
  void testDeserialize() throws IOException {
    JsonParser mockJsonParser = mock(JsonParser.class);

    when(mockJsonParser.getLongValue()).thenReturn(0L);
    OracleDocumentId expected = OracleDocumentId.of(UnsignedInteger.ZERO);
    OracleDocumentId oracleDocumentId = deserializer.deserialize(mockJsonParser, mock(DeserializationContext.class));
    assertThat(oracleDocumentId).isEqualTo(expected);

    when(mockJsonParser.getLongValue()).thenReturn(4294967295L);
    expected = OracleDocumentId.of(UnsignedInteger.MAX_VALUE);
    oracleDocumentId = deserializer.deserialize(mockJsonParser, mock(DeserializationContext.class));
    assertThat(oracleDocumentId).isEqualTo(expected);
  }

}