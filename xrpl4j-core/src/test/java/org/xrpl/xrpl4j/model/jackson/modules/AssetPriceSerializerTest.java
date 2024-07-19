package org.xrpl.xrpl4j.model.jackson.modules;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.primitives.UnsignedLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xrpl.xrpl4j.model.transactions.AssetPrice;

import java.io.IOException;

/**
 * Unit test for {@link AssetPriceSerializer}.
 */
class AssetPriceSerializerTest {

  private AssetPriceSerializer serializer;

  @BeforeEach
  void setUp() {
    serializer = new AssetPriceSerializer();
  }

  @Test
  void testSerialize() throws IOException {
    JsonGenerator jsonGeneratorMock = mock(JsonGenerator.class);

    AssetPrice expected = AssetPrice.of(UnsignedLong.ZERO);
    serializer.serialize(expected, jsonGeneratorMock, mock(SerializerProvider.class));
    verify(jsonGeneratorMock).writeString("0");

    expected = AssetPrice.of(UnsignedLong.MAX_VALUE);
    serializer.serialize(expected, jsonGeneratorMock, mock(SerializerProvider.class));
    verify(jsonGeneratorMock).writeString("ffffffffffffffff");
  }
}