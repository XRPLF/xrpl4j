package org.xrpl.xrpl4j.model.transactions;

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

public class XChainCountTest {

  ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void testToString() {
    XChainCount count = XChainCount.of(UnsignedLong.ZERO);
    assertThat(count.toString()).isEqualTo("0");

    XChainCount countMax = XChainCount.of(UnsignedLong.MAX_VALUE);
    assertThat(countMax.toString()).isEqualTo("18446744073709551615");
  }

  @Test
  void testJson() throws JsonProcessingException, JSONException {
    XChainCount count = XChainCount.of(UnsignedLong.valueOf(1000));
    XChainCountWrapper wrapper = XChainCountWrapper.of(count);

    String json = "{\"count\": \"3e8\"}";
    assertSerializesAndDeserializes(wrapper, json);
  }

  @Test
  void testMaxJson() throws JSONException, JsonProcessingException {
    XChainCount count = XChainCount.of(UnsignedLong.MAX_VALUE);
    XChainCountWrapper wrapper = XChainCountWrapper.of(count);

    String json = "{\"count\": \"ffffffffffffffff\"}";
    assertSerializesAndDeserializes(wrapper, json);
  }

  private void assertSerializesAndDeserializes(
    XChainCountWrapper wrapper,
    String json
  ) throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    XChainCountWrapper deserialized = objectMapper.readValue(
      serialized, XChainCountWrapper.class
    );
    Assertions.assertThat(deserialized).isEqualTo(wrapper);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableXChainCountWrapper.class)
  @JsonDeserialize(as = ImmutableXChainCountWrapper.class)
  interface XChainCountWrapper {

    static XChainCountWrapper of(XChainCount count) {
      return ImmutableXChainCountWrapper.builder().count(count).build();
    }

    XChainCount count();

  }
}
