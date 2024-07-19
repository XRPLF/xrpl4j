package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.UnsignedLong;
import org.assertj.core.api.Assertions;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

/**
 * Unit test for {@link PriceData}.
 */
class PriceDataTest {

  ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void builder() {
    PriceData priceData = PriceData.builder()
      .assetPrice(AssetPrice.of(UnsignedLong.ONE))
      .baseAsset("baseAsset")
      .quoteAsset("quoteAsset")
      .build();

    assertThat(priceData.assetPrice().isPresent()).isTrue();
    priceData.assetPrice().ifPresent((assetPrice) -> assertThat(assetPrice.value()).isEqualTo(UnsignedLong.ONE));

    assertThat(priceData.baseAsset()).isEqualTo("baseAsset");
    assertThat(priceData.quoteAsset()).isEqualTo("quoteAsset");
  }

  @Test
  void testToString() {
    PriceData priceData = PriceData.builder()
      .assetPrice(AssetPrice.of(UnsignedLong.ONE))
      .baseAsset("baseAsset")
      .quoteAsset("quoteAsset")
      .build();
    assertThat(priceData.toString()).isEqualTo("PriceData{baseAsset=baseAsset, quoteAsset=quoteAsset, assetPrice=1}");
  }

  @Test
  void testJson() throws JsonProcessingException, JSONException {
    PriceData priceData = PriceData.builder()
      .assetPrice(AssetPrice.of(UnsignedLong.ONE))
      .baseAsset("baseAsset")
      .quoteAsset("quoteAsset")
      .build();
    PriceDataWrapper wrapper = PriceDataWrapper.of(priceData);

    String json = "{\"PriceData\":{\"BaseAsset\":\"baseAsset\",\"QuoteAsset\":\"quoteAsset\",\"AssetPrice\":\"1\"}}";
    assertSerializesAndDeserializes(wrapper, json);
  }

  private void assertSerializesAndDeserializes(
    PriceDataWrapper wrapper,
    String json
  ) throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    PriceDataWrapper deserialized = objectMapper.readValue(serialized, PriceDataWrapper.class);
    Assertions.assertThat(deserialized).isEqualTo(wrapper);
  }
}