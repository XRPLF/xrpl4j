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

public class DidUriTest {

  ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void testToString() {
    DidUri count = DidUri.of("");
    assertThat(count.toString()).isEqualTo("");

    DidUri countMax = DidUri.of("ABCDEFG");
    assertThat(countMax.toString()).isEqualTo("ABCDEFG");
  }

  @Test
  void testJson() throws JsonProcessingException, JSONException {
    DidUri count = DidUri.of("ABCDEF");
    DidUriWrapper wrapper = DidUriWrapper.of(count);

    String json = "{\"value\": \"ABCDEF\"}";
    assertSerializesAndDeserializes(wrapper, json);
  }

  @Test
  void testEmptyValueJson() throws JSONException, JsonProcessingException {
    DidUri count = DidUri.of("");
    DidUriWrapper wrapper = DidUriWrapper.of(count);

    String json = "{\"value\": \"\"}";
    assertSerializesAndDeserializes(wrapper, json);
  }

  private void assertSerializesAndDeserializes(
    DidUriWrapper wrapper,
    String json
  ) throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    DidUriWrapper deserialized = objectMapper.readValue(
      serialized, DidUriWrapper.class
    );
    Assertions.assertThat(deserialized).isEqualTo(wrapper);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableDidUriWrapper.class)
  @JsonDeserialize(as = ImmutableDidUriWrapper.class)
  interface DidUriWrapper {

    static DidUriWrapper of(DidUri value) {
      return ImmutableDidUriWrapper.builder().value(value).build();
    }

    DidUri value();

  }
}
