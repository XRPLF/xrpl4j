package org.xrpl.xrpl4j.model.jackson.modules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.transactions.OracleProvider;

import java.io.IOException;

/**
 * Unit test for {@link OracleProviderDeserializer}.
 */
class OracleProviderDeserializerTest {

  private OracleProviderDeserializer deserializer;

  @BeforeEach
  void setUp() {
    deserializer = new OracleProviderDeserializer();
  }

  @Test
  void testDeserialize() throws IOException {
    JsonParser mockJsonParser = mock(JsonParser.class);

    OracleProvider expected = OracleProvider.of("foo");
    when(mockJsonParser.getText()).thenReturn("foo");
    OracleProvider assetPrice = deserializer.deserialize(mockJsonParser, mock(DeserializationContext.class));
    assertThat(assetPrice).isEqualTo(expected);

    expected = OracleProvider.of("");
    when(mockJsonParser.getText()).thenReturn("");
    assetPrice = deserializer.deserialize(mockJsonParser, mock(DeserializationContext.class));
    assertThat(assetPrice).isEqualTo(expected);
  }

}