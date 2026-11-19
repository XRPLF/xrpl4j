package org.xrpl.xrpl4j.model.transactions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

  private static final DidUri EMPTY_DID_URI = DidUri.of("");

  ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void testNull() {
    assertThrows(NullPointerException.class, () -> DidUri.of(null));
  }

  @Test
  void testEquality() {
    assertThat(EMPTY_DID_URI).isEqualTo(EMPTY_DID_URI);
    assertThat(EMPTY_DID_URI).isNotEqualTo(new Object());
  }

  @Test
  void testToString() {
    assertThat(EMPTY_DID_URI.toString()).isEqualTo("");
    assertThat(EMPTY_DID_URI.equals(null)).isFalse();

    DidUri didUriMax = DidUri.of("ABCDEFG");
    assertThat(didUriMax.toString()).isEqualTo("ABCDEFG");
  }

  @Test
  void testJson() throws JsonProcessingException, JSONException {
    DidUri didUri = DidUri.of("ABCDEF");
    DidUriWrapper wrapper = DidUriWrapper.of(didUri);

    String json = "{\"value\": \"ABCDEF\"}";
    assertSerializesAndDeserializes(wrapper, json);
  }

  @Test
  void testEmptyValueJson() throws JSONException, JsonProcessingException {
    DidUri didUri = DidUri.of("");
    DidUriWrapper wrapper = DidUriWrapper.of(didUri);

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
