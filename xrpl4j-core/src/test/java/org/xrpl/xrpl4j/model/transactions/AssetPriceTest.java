package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedLong;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.immutables.value.Value;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

/**
 * Unit tests for {@link AssetPrice}.
 */
public class AssetPriceTest {

  private static final AssetPrice ZERO_ASSET_PRICE = AssetPrice.of(UnsignedLong.ZERO);

  ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void testNull() {
    assertThrows(NullPointerException.class, () -> AssetPrice.of(null));
  }

  @Test
  void testEquality() {
    AssertionsForClassTypes.assertThat(ZERO_ASSET_PRICE).isEqualTo(ZERO_ASSET_PRICE);
    AssertionsForClassTypes.assertThat(ZERO_ASSET_PRICE).isNotEqualTo(new Object());
    AssertionsForClassTypes.assertThat(ZERO_ASSET_PRICE.equals(null)).isFalse();
  }

  @Test
  void testToString() {
    AssetPrice count = AssetPrice.of(UnsignedLong.ZERO);
    assertThat(count.toString()).isEqualTo("0");

    AssetPrice countMax = AssetPrice.of(UnsignedLong.MAX_VALUE);
    assertThat(countMax.toString()).isEqualTo("18446744073709551615");
  }

  @Test
  void testJson() throws JsonProcessingException, JSONException {
    AssetPrice count = AssetPrice.of(UnsignedLong.valueOf(1000));
    AssetPriceWrapper wrapper = AssetPriceWrapper.of(count);

    String json = "{\"value\": \"3e8\"}";
    assertSerializesAndDeserializes(wrapper, json);
  }

  @Test
  void testMaxJson() throws JSONException, JsonProcessingException {
    AssetPrice count = AssetPrice.of(UnsignedLong.MAX_VALUE);
    AssetPriceWrapper wrapper = AssetPriceWrapper.of(count);

    String json = "{\"value\": \"ffffffffffffffff\"}";
    assertSerializesAndDeserializes(wrapper, json);
  }

  private void assertSerializesAndDeserializes(
    AssetPriceWrapper wrapper,
    String json
  ) throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    AssetPriceWrapper deserialized = objectMapper.readValue(
      serialized, AssetPriceWrapper.class
    );
    Assertions.assertThat(deserialized).isEqualTo(wrapper);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableAssetPriceWrapper.class)
  @JsonDeserialize(as = ImmutableAssetPriceWrapper.class)
  interface AssetPriceWrapper {

    static AssetPriceWrapper of(AssetPrice count) {
      return ImmutableAssetPriceWrapper.builder().value(count).build();
    }

    AssetPrice value();

  }
}
