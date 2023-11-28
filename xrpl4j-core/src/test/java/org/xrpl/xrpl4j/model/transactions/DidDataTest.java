package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.assertj.core.api.Assertions;
import org.immutables.value.Value;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

public class DidDataTest {

  ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void testToString() {
    DidData count = DidData.of("");
    assertThat(count.toString()).isEqualTo("");

    DidData countMax = DidData.of("ABCDEFG");
    assertThat(countMax.toString()).isEqualTo("ABCDEFG");
  }

  @Test
  void testJson() throws JsonProcessingException, JSONException {
    DidData count = DidData.of("ABCDEF");
    DidDataWrapper wrapper = DidDataWrapper.of(count);

    String json = "{\"value\": \"ABCDEF\"}";
    assertSerializesAndDeserializes(wrapper, json);
  }

  @Test
  void testEmptyValueJson() throws JSONException, JsonProcessingException {
    DidData count = DidData.of("");
    DidDataWrapper wrapper = DidDataWrapper.of(count);

    String json = "{\"value\": \"\"}";
    assertSerializesAndDeserializes(wrapper, json);
  }

  private void assertSerializesAndDeserializes(
    DidDataWrapper wrapper,
    String json
  ) throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    DidDataWrapper deserialized = objectMapper.readValue(
      serialized, DidDataWrapper.class
    );
    Assertions.assertThat(deserialized).isEqualTo(wrapper);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableDidDataWrapper.class)
  @JsonDeserialize(as = ImmutableDidDataWrapper.class)
  interface DidDataWrapper {

    static DidDataWrapper of(DidData value) {
      return ImmutableDidDataWrapper.builder().value(value).build();
    }

    DidData value();

  }
}
