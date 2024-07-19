package org.xrpl.xrpl4j.model.jackson.modules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.transactions.OracleUri;

import java.io.IOException;

/**
 * Unit tests for {@link OracleUriDeserializer}.
 */
class OracleUriDeserializerTest {

  private OracleUriDeserializer deserializer;

  @BeforeEach
  void setUp() {
    deserializer = new OracleUriDeserializer();
  }

  @Test
  void testDeserialize() throws IOException {
    JsonParser mockJsonParser = mock(JsonParser.class);

    OracleUri expected = OracleUri.of("foo");
    when(mockJsonParser.getText()).thenReturn("foo");
    OracleUri oracleUri = deserializer.deserialize(mockJsonParser, mock(DeserializationContext.class));
    assertThat(oracleUri).isEqualTo(expected);

    expected = OracleUri.of("");
    when(mockJsonParser.getText()).thenReturn("");
    oracleUri = deserializer.deserialize(mockJsonParser, mock(DeserializationContext.class));
    assertThat(oracleUri).isEqualTo(expected);
  }

}