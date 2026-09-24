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

public class DidDocumentTest {

  private static final DidDocument EMPTY_DID_DOCUMENT = DidDocument.of("");

  ObjectMapper objectMapper = ObjectMapperFactory.create();

  @Test
  void testNull() {
    assertThrows(NullPointerException.class, () -> DidDocument.of(null));
  }

  @Test
  void testEquality() {
    AssertionsForClassTypes.assertThat(EMPTY_DID_DOCUMENT).isEqualTo(EMPTY_DID_DOCUMENT);
    AssertionsForClassTypes.assertThat(EMPTY_DID_DOCUMENT).isNotEqualTo(new Object());
    AssertionsForClassTypes.assertThat(EMPTY_DID_DOCUMENT.equals(null)).isFalse();
  }

  @Test
  void testToString() {
    assertThat(EMPTY_DID_DOCUMENT.toString()).isEqualTo("");

    DidDocument didDocumentMax = DidDocument.of("ABCDEFG");
    assertThat(didDocumentMax.toString()).isEqualTo("ABCDEFG");
  }

  @Test
  void testJson() throws JsonProcessingException, JSONException {
    DidDocument didDocument = DidDocument.of("ABCDEF");
    DidDocumentWrapper wrapper = DidDocumentWrapper.of(didDocument);

    String json = "{\"value\": \"ABCDEF\"}";
    assertSerializesAndDeserializes(wrapper, json);
  }

  @Test
  void testEmptyValueJson() throws JSONException, JsonProcessingException {
    DidDocument didDocument = DidDocument.of("");
    DidDocumentWrapper wrapper = DidDocumentWrapper.of(didDocument);

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
