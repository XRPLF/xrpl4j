package org.xrpl.xrpl4j.model.jackson.modules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.transactions.AssetPrice;

import java.io.IOException;

/**
 * Unit tests for {@link AssetPriceDeserializer}.
 */
class AssetPriceDeserializerTest {

  private AssetPriceDeserializer deserializer;

  @BeforeEach
  void setUp() {
    deserializer = new AssetPriceDeserializer();
  }

  @Test
  void testDeserialize() throws IOException {
    JsonParser mockJsonParser = mock(JsonParser.class);

    AssetPrice expected = AssetPrice.of(UnsignedLong.ZERO);
    when(mockJsonParser.getText()).thenReturn("0");
    AssetPrice assetPrice = deserializer.deserialize(mockJsonParser, mock(DeserializationContext.class));
    assertThat(assetPrice).isEqualTo(expected);

    expected = AssetPrice.of(UnsignedLong.MAX_VALUE);
    when(mockJsonParser.getText()).thenReturn("ffffffffffffffff");
    assetPrice = deserializer.deserialize(mockJsonParser, mock(DeserializationContext.class));
    assertThat(assetPrice).isEqualTo(expected);
  }

}