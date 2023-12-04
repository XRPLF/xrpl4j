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

public class DidDocumentTest {

  ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void testToString() {
    DidDocument count = DidDocument.of("");
    assertThat(count.toString()).isEqualTo("");

    DidDocument countMax = DidDocument.of("ABCDEFG");
    assertThat(countMax.toString()).isEqualTo("ABCDEFG");
  }

  @Test
  void testJson() throws JsonProcessingException, JSONException {
    DidDocument count = DidDocument.of("ABCDEF");
    DidDocumentWrapper wrapper = DidDocumentWrapper.of(count);

    String json = "{\"value\": \"ABCDEF\"}";
    assertSerializesAndDeserializes(wrapper, json);
  }

  @Test
  void testEmptyValueJson() throws JSONException, JsonProcessingException {
    DidDocument count = DidDocument.of("");
    DidDocumentWrapper wrapper = DidDocumentWrapper.of(count);

    String json = "{\"value\": \"\"}";
    assertSerializesAndDeserializes(wrapper, json);
  }

  private void assertSerializesAndDeserializes(
    DidDocumentWrapper wrapper,
    String json
  ) throws JsonProcessingException, JSONException {
    String serialized = objectMapper.writeValueAsString(wrapper);
    JSONAssert.assertEquals(json, serialized, JSONCompareMode.STRICT);
    DidDocumentWrapper deserialized = objectMapper.readValue(
      serialized, DidDocumentWrapper.class
    );
    Assertions.assertThat(deserialized).isEqualTo(wrapper);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableDidDocumentWrapper.class)
  @JsonDeserialize(as = ImmutableDidDocumentWrapper.class)
  interface DidDocumentWrapper {

    static DidDocumentWrapper of(DidDocument value) {
      return ImmutableDidDocumentWrapper.builder().value(value).build();
    }

    DidDocument value();

  }
}
