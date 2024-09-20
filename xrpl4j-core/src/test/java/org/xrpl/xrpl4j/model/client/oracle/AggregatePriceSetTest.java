package org.xrpl.xrpl4j.model.client.oracle;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.primitives.UnsignedLong;
import org.assertj.core.api.Assertions;
import org.immutables.value.Value;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

import java.math.BigDecimal;

/**
 * Unit tests for {@link AggregatePriceSet}.
 */
class AggregatePriceSetTest {

  private final ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void testBigDecimalFields() {
    AggregatePriceSet set = AggregatePriceSet.builder()
      .meanString("1234.5678")
      .size(UnsignedLong.ONE)
      .standardDeviationString("345678.23496")
      .build();

    assertThat(set.mean()).isEqualTo(BigDecimal.valueOf(1234.5678));
    assertThat(set.standardDeviation()).isEqualTo(BigDecimal.valueOf(345678.23496));
  }

  @Test
  void testJson() throws JSONException, JsonProcessingException {
    AggregatePriceSet priceSet =
      AggregatePriceSet.builder()
        .meanString("200")
        .size(UnsignedLong.ONE)
        .standardDeviationString("1.00")
        .build();
    AggregatePriceSetWrapper wrapper = AggregatePriceSetWrapper.of(priceSet);

    String json = "{\"value\":{\"mean\":\"200\",\"size\":1,\"standard_deviation\":\"1.00\"}}";
    assertSerializesAndDeserializes(wrapper, json);
  }

  private void assertSerializesAndDeserializes(AggregatePriceSetWrapper wrapper, String json)
    throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    AggregatePriceSetWrapper deserialized = objectMapper.readValue(serialized, AggregatePriceSetWrapper.class);
    Assertions.assertThat(deserialized).isEqualTo(wrapper);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableAggregatePriceSetWrapper.class)
  @JsonDeserialize(as = ImmutableAggregatePriceSetWrapper.class)
  interface AggregatePriceSetWrapper {

    static AggregatePriceSetWrapper of(AggregatePriceSet aggregatePriceSet) {
      return ImmutableAggregatePriceSetWrapper.builder().value(aggregatePriceSet).build();
    }

    AggregatePriceSet value();

  }
}