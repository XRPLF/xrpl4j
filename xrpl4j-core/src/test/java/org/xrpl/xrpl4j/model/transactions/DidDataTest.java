package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.immutables.value.Value;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

public class DidDataTest {

  private final DidData EMPTY_DID_DATA = DidData.of("");

  ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void testNull() {
    assertThrows(NullPointerException.class, () -> DidData.of(null));
  }

  @Test
  void testEquality() {
    AssertionsForClassTypes.assertThat(EMPTY_DID_DATA).isEqualTo(EMPTY_DID_DATA);
    AssertionsForClassTypes.assertThat(EMPTY_DID_DATA).isNotEqualTo(new Object());
    AssertionsForClassTypes.assertThat(EMPTY_DID_DATA.equals(null)).isFalse();
  }

  @Test
  void testToString() {
    DidData didData = DidData.of("");
    assertThat(didData.toString()).isEqualTo("");

    DidData didDataMax = DidData.of("ABCDEFG");
    assertThat(didDataMax.toString()).isEqualTo("ABCDEFG");
  }

  @Test
  void testJson() throws JsonProcessingException, JSONException {
    DidData didData = DidData.of("ABCDEF");
    DidDataWrapper wrapper = DidDataWrapper.of(didData);

    String json = "{\"value\": \"ABCDEF\"}";
    assertSerializesAndDeserializes(wrapper, json);
  }

  @Test
  void testEmptyValueJson() throws JSONException, JsonProcessingException {
    DidData didData = DidData.of("");
    DidDataWrapper wrapper = DidDataWrapper.of(didData);

    String json = "{\"value\": \"\"}";
    assertSerializesAndDeserializes(wrapper, json);
  }

  private void assertSerializesAndDeserializes(DidDataWrapper wrapper, String json)
    throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    DidDataWrapper deserialized = objectMapper.readValue(serialized, DidDataWrapper.class);
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
