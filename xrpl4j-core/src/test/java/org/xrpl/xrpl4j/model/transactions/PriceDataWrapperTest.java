package org.xrpl.xrpl4j.model.transactions;

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
 * Unit tests for {@link PriceDataWrapper}.
 */
class PriceDataWrapperTest {

  ObjectMapper objectMapper = ObjectMapperFactory.create();

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